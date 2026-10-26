package cloud.mallne.dicentra.areaassist.contract

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPluginInstance
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.model.weather.CurrentConditions
import cloud.mallne.dicentra.areaassist.model.weather.HourlyForecast
import cloud.mallne.dicentra.areaassist.model.weather.WeatherCondition
import cloud.mallne.dicentra.areaassist.model.weather.WeatherReport
import cloud.mallne.dicentra.areaassist.model.weather.WeatherSection
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorService
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.client.ktor.io.AvKtorRequest
import cloud.mallne.dicentra.aviator.client.ktor.io.AvKtorResponse
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionStages
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.core.io.NetworkBody
import cloud.mallne.dicentra.aviator.core.io.NetworkChain
import cloud.mallne.dicentra.aviator.core.io.NetworkHeader
import cloud.mallne.dicentra.aviator.plugin.interception.InterceptionPlugin
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.geokit.coordinates.builtin.BuiltInCoordinateReferenceSystems
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpMethod
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.openapi.OpenApiDoc
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import java.time.Instant
import kotlin.test.*

/** Explicitly opt-in: exercises the application's real response mapping, not provider schemata. */
@Tag("live-provider")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LiveApiContractTest {
    private val client = HttpClient(CIO) { install(HttpTimeout) { requestTimeoutMillis = 30_000 } }
    private val crsRegistry = CrsRegistry().apply {
        ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
        ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
        ingest(BuiltInCoordinateReferenceSystems.EPSG25833)
    }
    private val weather = listOf(APIs.Services.WEATHER_SERVICE_CURRENT, APIs.Services.WEATHER_SERVICE_FORECAST)
        .map { it.locator(ServiceMethods.GATHER) }
    private val parcels = APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER)

    @AfterAll
    fun close() = client.close()

    @TestFactory
    fun allDocuments(): List<DynamicNode> = APIs.apis.map { source ->
        // Codex publishes JSON; consume that same representation rather than test-only models.
        val api = Serialization().decodeFromString<OpenApiDoc>(Serialization().encodeToString(source))
        val services = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(api) {
            install(JsonAdapter)
            install(XmlAdapter)
            install(WfsAdapterPlugin(crsRegistry))
            install(EsriAdapterPlugin)
            install(WeaverPlugin) { serviceFilter.addAll(weather) }
            install(InterceptionPlugin) {
                serviceFilter.add(parcels)
                steps {
                    before(AviatorExecutionStages.Requesting) { context ->
                        val service = context.dataHolder as KtorAviatorService
                        val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
                        val options = service.optionBundle<ParcelServiceOptions>()
                        val propertyNames = (options.keys.mapNotNull { it.reference } + wfs.configurationBundle.geometryPointer)
                            .distinct()
                            .joinToString(",") { "${wfs.configurationBundle.nsPrefix}:$it" }
                        val source = context.networkChain.single()
                        val url = URLBuilder(source.url).apply {
                            parameters.clear()
                            parameters.append("service", "WFS")
                            parameters.append("version", "2.0.0")
                            parameters.append("request", "GetFeature")
                            parameters.append("typeNames", wfs.configurationBundle.typeNames)
                            parameters.append("count", "1")
                            parameters.append("SRSName", wfs.configurationBundle.outputCRS)
                            parameters.append("propertyName", propertyNames)
                        }.build()
                        @Suppress("UNCHECKED_CAST")
                        val chains = context.networkChain as MutableList<NetworkChain<AvKtorRequest, AvKtorResponse, NetworkHeader>>
                        chains.clear()
                        chains += NetworkChain(
                            url = url.toString(),
                            request = AvKtorRequest(
                                method = HttpMethod.Get,
                                url = url,
                                outgoingContent = NetworkBody.Empty,
                                headers = object : NetworkHeader {
                                    override var values = RequestParameters()
                                },
                                contentType = ContentType.Application.Xml,
                            ),
                        )
                    }
                }
            }
        }
        dynamicContainer(api.info.title, services.map { service ->
            val contract = if (service.serviceLocator in weather) " → Weaver → shared WeatherReport" else ""
            dynamicTest("${service.route.method.value} ${service.route.path}$contract") {
                runBlocking { withTimeout(90_000) { checkResponse(service) } }
            }
        })
    }

    private suspend fun checkResponse(service: KtorAviatorService) {
        val isParcel = service.serviceLocator == parcels
        assertTrue(service.route.method in listOf(HttpMethod.Get, HttpMethod.Head) || (isParcel && service.route.method == HttpMethod.Post),
            "No safe read-only probe for ${service.route.method.value} ${service.route.path}")
        val now = Instant.now()
        val parameters = RequestParameters(mapOf("lat" to "50.0", "lon" to "10.0",
            "date" to now.toString(), "last_date" to now.plusSeconds(86_400).toString())
            .mapValuesTo(mutableMapOf()) { RequestParameter.Single(it.value) })

        when {
            isParcel -> {
                val context = service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                    requestParams = parameters
                )
                val response = context.networkChain.firstOrNull()?.response
                val diagnostic = "HTTP ${response?.status?.value}: ${response?.content?.decodeToString()?.take(2000)}"
                assertTrue((response?.status?.value ?: 0) in 200..299, diagnostic)
                val result = assertNotNull(context.result, "Parcel response could not be mapped")
                assertTrue(result.isNotEmpty(), "Parcel mapping returned no models. $diagnostic")
                result.forEach {
                    assertTrue(it.parcel.parcelId.isNotBlank(), "Mapped parcel has no ID")
                    assertNotNull(it.parcel.json.geometry, "Mapped parcel has no geometry")
                }
            }
            service.serviceLocator in weather -> {
                val context = service.requestContextful<WeatherReport, NoBody>(requestParams = parameters)
                assertSuccess(context.networkChain.firstOrNull()?.response?.status?.value)
                val result = assertNotNull(context.result, "Response could not be transformed into WeatherReport")
                val transformed = assertNotNull(context.bundle[WeaverPlugin.RESULT], "Weaver produced no mapped response")
                val json = Serialization()
                assertEquals(result, json.decodeFromJsonElement<WeatherReport>(transformed),
                    "Weaver output must decode into the same shared model as the response adapter")
                assertEquals(result, json.decodeFromString<WeatherReport>(json.encodeToString(result)),
                    "The shared weather model must survive serialization without losing mapped data")
                (result.current as? WeatherSection.Available)?.value?.let { observation ->
                    assertMappedCondition(observation.measurements.wmoWeatherCode, observation.condition)
                }
                (result.hourly as? WeatherSection.Available)?.value?.forEach { observation ->
                    assertMappedCondition(observation.measurements.wmoWeatherCode, observation.condition)
                }
                when (service.serviceLocator) {
                    APIs.Services.WEATHER_SERVICE_CURRENT.locator(ServiceMethods.GATHER) ->
                        assertIs<WeatherSection.Available<CurrentConditions>>(result.current, "Current-weather mapping returned no observation")
                    APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER) -> {
                        val hourly = assertIs<WeatherSection.Available<List<HourlyForecast>>>(result.hourly,
                            "Forecast mapping returned no hourly section")
                        assertTrue(hourly.value.isNotEmpty(), "Forecast mapping returned no observations")
                    }
                }
            }
            else -> {
                val context = service.requestContextful<String, NoBody>(requestParams = parameters)
                val response = context.networkChain.firstOrNull()?.response
                assertSuccess(response?.status?.value)
                assertTrue(response?.content?.isNotEmpty() == true, "Provider returned an empty response")
            }
        }
    }

    private fun assertMappedCondition(wmoCode: Int?, condition: WeatherCondition) {
        if (WeatherCondition.fromWmo(wmoCode) != WeatherCondition.Unknown) {
            assertNotEquals(WeatherCondition.Unknown, condition, "Known WMO code $wmoCode lost its condition during mapping")
        }
    }

    private fun assertSuccess(status: Int?) = assertTrue(status != null && status in 200..299, "Provider returned HTTP $status")
}
