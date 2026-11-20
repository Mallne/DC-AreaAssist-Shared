package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.aviator.orchestration.arazzo.model.ArazzoDocument
import cloud.mallne.dicentra.aviator.orchestration.arazzo.model.arazzoDocument
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/** Portable workflow registration metadata; execution and source resolution are host-owned. */
@Serializable
data class WeatherWorkflowDefinition(
    val documentId: String,
    val workflowId: String,
    val locator: ServiceLocator,
    val options: WeatherServiceOptions,
    val document: JsonObject,
)

/**
 * Bundled Arazzo definitions. Source URIs identify bundled OpenAPI documents; the host resolves
 * them against [sources], not by fetching them over the network. Provider selection, operation
 * input mapping and execution remain the host's responsibility.
 */
object WeatherWorkflows {
    const val BRIGHTSKY_SOURCE = "urn:areaassist:openapi:brightsky"
    const val OPEN_METEO_SOURCE = "urn:areaassist:openapi:open-meteo"

    val sources get() = mapOf(BRIGHTSKY_SOURCE to Brightsky.value, OPEN_METEO_SOURCE to OpenMeteo.value)

    /** Arazzo outputs expose the source response; Weaver's normalized result is separate. */
    val brightSky: JsonObject = document(
        "BrightSky weather",
        "brightsky",
        BRIGHTSKY_SOURCE,
        listOf("current" to "CurrentSources", "forecast" to "WeatherForecast"),
    )

    /** Open-Meteo returns current conditions and forecasts in the same response. */
    val openMeteo: JsonObject = document(
        "Open-Meteo weather",
        "openMeteo",
        OPEN_METEO_SOURCE,
        listOf("current" to "OpenMeteoForecast", "forecast" to "OpenMeteoForecast"),
    )

    val documents: List<JsonObject> get() = listOf(brightSky, openMeteo)

    val definitions: List<WeatherWorkflowDefinition>
        get() = listOf(
            definition("brightsky", "current", APIs.Services.WEATHER_SERVICE_CURRENT, WeatherServiceOptions.BrightSkyCurrent, brightSky),
            definition("brightsky", "forecast", APIs.Services.WEATHER_SERVICE_FORECAST, WeatherServiceOptions.BrightSkyForecast, brightSky),
            definition("open-meteo", "current", APIs.Services.WEATHER_SERVICE_CURRENT, WeatherServiceOptions.OpenMeteo, openMeteo),
            definition("open-meteo", "forecast", APIs.Services.WEATHER_SERVICE_FORECAST, WeatherServiceOptions.OpenMeteo, openMeteo),
        )

    private fun definition(
        documentId: String,
        workflowId: String,
        service: APIs.Services,
        options: WeatherServiceOptions,
        document: JsonObject,
    ) = WeatherWorkflowDefinition(documentId, workflowId, service.locator(ServiceMethods.GATHER), options, document)

    private fun document(
        title: String,
        sourceName: String,
        sourceUri: String,
        workflows: List<Pair<String, String>>,
    ): JsonObject {
        val document = arazzoDocument(title, ParcelConstants.endpointVersion.toString()) {
            source(sourceName, sourceUri)
            workflows.forEach { (id, operation) ->
                workflow(id) {
                    step("weather", "\$sourceDescriptions.$sourceName.$operation") {
                        val parameters = if (sourceName == "openMeteo") {
                            listOf("latitude" to "lat", "longitude" to "lon")
                        } else {
                            listOf("lat" to "lat", "lon" to "lon")
                        }
                        parameters.forEach { (name, input) -> parameter(name, "query", "\$inputs.$input") }
                        if (sourceName == "brightsky" && operation == "WeatherForecast") {
                            parameter("date", "query", "\$inputs.date")
                        }
                        if (sourceName == "openMeteo") {
                            mapOf(
                                "current" to OpenMeteo.CURRENT_VARIABLES,
                                "hourly" to OpenMeteo.HOURLY_VARIABLES,
                                "daily" to OpenMeteo.DAILY_VARIABLES,
                                "wind_speed_unit" to "ms",
                                "timezone" to "auto",
                            ).forEach { (name, value) -> parameter(name, "query", value) }
                        }
                        output("providerResponse", "\$response.body")
                    }
                    output("providerResponse", "\$steps.weather.outputs.providerResponse")
                }
            }
        }
        return Json.encodeToJsonElement(ArazzoDocument.serializer(), document).jsonObject
    }
}
