package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.client.ktor.KtorEngineDefinitionConverter
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.engine.OpenApiOperationDefinitionRequest
import cloud.mallne.dicentra.aviator.koas.typed.routes
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils.optionBundle
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.openapi.OpenApiDoc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApiDefinitionConversionTest {
    @Test
    fun brightSkyAlertsRemainProviderNativeUntilTheirFullContractCanBeMapped() {
        val alertRoute = APIs.brightSky.routes().single { it.operationId == "WeatherAlerts" }
        assertEquals(
            APIs.Services.WEATHER_SERVICE_WARNING.locator(ServiceMethods.GATHER).toString(),
            alertRoute.`x-dicentra-aviator-serviceDelegateCall`,
        )
        val options = optionBundle<WeatherServiceOptions>(
            assertNotNull(alertRoute.`x-dicentra-aviator-serviceOptions`),
            Serialization(),
        )
        assertEquals(WeatherServiceOptions.BrightSkyAlerts, options)
        assertEquals("areaassist-brightsky-alerts-v1", options.weaverSchema?.key)
        assertNull(alertRoute.`x-dicentra-aviator-pluginMaterialization`)
        assertEquals(
            setOf("lat", "lon", "warn_cell_id", "tz"),
            alertRoute.parameter.map { it.name }.toSet(),
        )
        assertTrue(
            APIs.weatherWorkflowDefinitions.none {
                it.locator == APIs.Services.WEATHER_SERVICE_WARNING.locator(ServiceMethods.GATHER)
            },
        )
    }

    @Test
    fun brightSkyWeatherParametersMatchProviderLocationAndRangeContracts() {
        val document = Serialization().parseToJsonElement(
            Serialization().encodeToString(APIs.brightSky),
        ).jsonObject
        val paths = document.getValue("paths").jsonObject

        fun parameter(path: String, name: String) =
            paths.getValue(path).jsonObject.getValue("get").jsonObject.getValue("parameters")
                .jsonArray.map { it.jsonObject }
                .single { it.getValue("name").jsonPrimitive.content == name }

        for (path in listOf("/current_weather", "/weather")) {
            assertFalse(parameter(path, "lat")["required"]?.jsonPrimitive?.content == "true",
                "$path must allow station IDs instead of requiring coordinates")
            assertFalse(parameter(path, "lon")["required"]?.jsonPrimitive?.content == "true",
                "$path must allow station IDs instead of requiring coordinates")
        }
        assertEquals("true", parameter("/weather", "date").getValue("required").jsonPrimitive.content)
        assertEquals("true", parameter("/synop", "date").getValue("required").jsonPrimitive.content)

        for (path in listOf("/current_weather", "/weather", "/sources")) {
            val maxDistance = parameter(path, "max_dist")
            assertEquals("integer", maxDistance.getValue("schema").jsonObject.getValue("type").jsonPrimitive.content)
            assertEquals(0.0, maxDistance.getValue("schema").jsonObject.getValue("minimum").jsonPrimitive.content.toDouble())
            assertEquals(500_000.0, maxDistance.getValue("schema").jsonObject.getValue("maximum").jsonPrimitive.content.toDouble())
        }

        fun schemaType(path: String, name: String) =
            parameter(path, name).getValue("schema").jsonObject.getValue("type").jsonPrimitive.content

        assertEquals("integer", schemaType("/alerts", "warn_cell_id"))
        assertEquals("integer", schemaType("/radar", "distance"))
        assertEquals("array", schemaType("/radar", "bbox"))
        assertEquals("integer", parameter("/radar", "bbox").getValue("schema").jsonObject
            .getValue("items").jsonObject.getValue("type").jsonPrimitive.content)
        for (path in listOf("/radar", "/weather", "/synop")) {
            assertEquals("date-time", parameter(path, "date").getValue("schema").jsonObject
                .getValue("format").jsonPrimitive.content)
            assertEquals("date-time", parameter(path, "last_date").getValue("schema").jsonObject
                .getValue("format").jsonPrimitive.content)
        }
        for (path in listOf("/current_weather", "/weather", "/sources", "/synop")) {
            assertEquals("array", schemaType(path, "dwd_station_id"), "$path DWD station IDs")
            assertEquals("string", parameter(path, "dwd_station_id").getValue("schema").jsonObject
                .getValue("items").jsonObject.getValue("type").jsonPrimitive.content)
            assertEquals("array", schemaType(path, "wmo_station_id"), "$path WMO station IDs")
            assertEquals("array", schemaType(path, "source_id"), "$path source IDs")
            assertEquals("integer", parameter(path, "source_id").getValue("schema").jsonObject
                .getValue("items").jsonObject.getValue("type").jsonPrimitive.content)
        }
    }

    @Test
    fun bundledDocumentsRoundTripAndEveryOperationMaterializes() {
        assertEquals(21, APIs.apis.size, "All bundled API definitions must remain registered")
        val client = HttpClient(MockEngine { error("Definition conversion must not access the network") })
        try {
            val converter = KtorEngineDefinitionConverter(
                KtorAviatorServiceConverter(client, serializers = Serialization.serializers()),
            )
            var operations = 0
            var parcelOperations = 0
            var weatherOperations = 0
            for (original in APIs.apis) {
                val api = Serialization().decodeFromString<OpenApiDoc>(Serialization().encodeToString(original))
                assertEquals(original.`x-dicentra-aviator`, api.`x-dicentra-aviator`, api.info.title)
                val originalRoutes = original.routes()
                val routes = api.routes()
                assertEquals(originalRoutes.map { it.method to it.path }, routes.map { it.method to it.path }, api.info.title)
                for ((source, route) in originalRoutes.zip(routes)) {
                    assertEquals(source.operationId, route.operationId, "${api.info.title} ${route.path}")
                    assertEquals(source.`x-dicentra-aviator-serviceDelegateCall`, route.`x-dicentra-aviator-serviceDelegateCall`)
                    assertEquals(source.`x-dicentra-aviator-pluginMaterialization`, route.`x-dicentra-aviator-pluginMaterialization`)
                    val declaredLocator = route.`x-dicentra-aviator-serviceDelegateCall`
                    val locator = declaredLocator?.let(::ServiceLocator) ?: ServiceLocator("test:unpublished")
                    val definition = converter.convert(OpenApiOperationDefinitionRequest(api, route, locator))
                    assertEquals(locator, definition.locator)
                    assertEquals(route.`x-dicentra-aviator-pluginMaterialization`.orEmpty(), definition.pluginConfiguration)
                    assertEquals(api.servers.orEmpty().map { it.url }, definition.outboundTargets)
                    if (locator == ParcelConstants.locator) {
                        val options = optionBundle<ParcelServiceOptions>(definition.service.options, Serialization())
                        assertEquals(
                            optionBundle<ParcelServiceOptions>(assertNotNull(source.`x-dicentra-aviator-serviceOptions`), Serialization()),
                            options,
                            "${api.info.title}: parcel options changed during serialization",
                        )
                        assertTrue(options.keys.isNotEmpty(), "${api.info.title}: parcel keys missing")
                        assertNotNull(options.boundary, "${api.info.title}: parcel boundary missing")
                        parcelOperations++
                    }
                    if (locator in listOf(
                            APIs.Services.WEATHER_SERVICE_CURRENT.locator(ServiceMethods.GATHER),
                            APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER),
                        )
                    ) {
                        val options = optionBundle<WeatherServiceOptions>(definition.service.options, Serialization())
                        assertEquals(
                            optionBundle<WeatherServiceOptions>(assertNotNull(source.`x-dicentra-aviator-serviceOptions`), Serialization()),
                            options,
                            "${api.info.title}: weather options changed during serialization",
                        )
                        assertNotNull(options.weaverSchema, "${api.info.title}: weather mapping missing")
                        weatherOperations++
                    }
                    operations++
                }
            }
            assertTrue(operations > 0)
            assertTrue(parcelOperations > 0)
            assertEquals(3, weatherOperations)
        } finally {
            client.close()
        }
    }
}
