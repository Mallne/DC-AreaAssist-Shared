package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.plugin.weaver.toOpenApiSchema
import cloud.mallne.dicentra.areaassist.model.weather.OpenMeteoWeatherSchema
import kotlinx.serialization.builtins.serializer
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import io.ktor.openapi.*

internal val openMeteoProviderResponseSchema = JsonSchema(
    type = JsonType.OBJECT,
    properties = mapOf(
        "latitude" to ReferenceOr.value(JsonSchema(type = JsonType.NUMBER)),
        "longitude" to ReferenceOr.value(JsonSchema(type = JsonType.NUMBER)),
        "elevation" to ReferenceOr.value(JsonSchema(type = JsonType.NUMBER)),
        "generationtime_ms" to ReferenceOr.value(JsonSchema(type = JsonType.NUMBER)),
        "utc_offset_seconds" to ReferenceOr.value(JsonSchema(type = JsonType.INTEGER)),
        "timezone" to ReferenceOr.value(JsonSchema(type = JsonType.STRING)),
        "timezone_abbreviation" to ReferenceOr.value(JsonSchema(type = JsonType.STRING)),
        "current" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
        "current_units" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
        "hourly" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
        "hourly_units" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
        "daily" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
        "daily_units" to ReferenceOr.value(JsonSchema(type = JsonType.OBJECT, additionalProperties = AdditionalProperties.Allowed(true))),
    ),
)

/** Open-Meteo Forecast API v1, documented from its public `/v1/forecast` endpoint. */
object OpenMeteo : ApiObject {
    private const val FORECAST = "/v1/forecast"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server("https://api.open-meteo.com")
        }
        info = OpenApiInfo(
            title = "Open-Meteo Forecast API",
            description = "Global weather forecast API. The request selects current, hourly, and daily variables; response values use metric units.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Creative Commons Attribution 4.0 International (CC BY 4.0)",
                url = "https://creativecommons.org/licenses/by/4.0/",
            ),
        )
    }.copy(
        paths = mapOf(
            FORECAST to ReferenceOr.value(
                PathItem(
                    summary = "Global weather forecast",
                    get = Operation.build {
                        operationId = "OpenMeteoForecast"
                        summary = "Get weather forecast"
                        description = "Returns current, hourly, and daily weather variables for one WGS84 coordinate. Variable lists are comma-separated. Daily variables require a timezone."
                        `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.WEATHER_SERVICE_FORECAST.locator(ServiceMethods.GATHER)
                        `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.OpenMeteo.usable()
                        parameters {
                            query("latitude") {
                                required = false
                                description = "WGS84 latitude."
                                schema = JsonSchema(type = JsonType.NUMBER)
                            }
                            query("lat") { required = true; description = "Provider-neutral latitude; Weaver maps this to latitude."; schema = JsonSchema(type = JsonType.STRING) }
                            query("lon") { required = true; description = "Provider-neutral longitude; Weaver maps this to longitude."; schema = JsonSchema(type = JsonType.STRING) }
                            query("date") { description = "Provider-neutral start date; ignored by this provider."; schema = JsonSchema(type = JsonType.STRING) }
                            query("last_date") { description = "Provider-neutral end date; ignored by this provider."; schema = JsonSchema(type = JsonType.STRING) }
                            query("max_dist") { description = "Provider-neutral maximum distance; ignored by this provider."; schema = JsonSchema(type = JsonType.STRING) }
                            query("tz") { description = "Provider-neutral timezone; ignored by this provider."; schema = JsonSchema(type = JsonType.STRING) }
                            query("units") { description = "Provider-neutral units; ignored by this provider."; schema = JsonSchema(type = JsonType.STRING) }
                            query("longitude") {
                                required = false
                                description = "WGS84 longitude."
                                schema = JsonSchema(type = JsonType.NUMBER)
                            }
                            query("elevation") { description = "Elevation in meters; nan disables statistical downscaling."; schema = JsonSchema(type = JsonType.NUMBER) }
                            query("current") { description = "Comma-separated current variables."; schema = JsonSchema(type = JsonType.STRING, default = GenericElementWrapper("temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,cloud_cover,precipitation,pressure_msl,wind_speed_10m,wind_direction_10m,wind_gusts_10m", String.serializer())) }
                            query("hourly") { description = "Comma-separated hourly variables."; schema = JsonSchema(type = JsonType.STRING, default = GenericElementWrapper("temperature_2m,weather_code,apparent_temperature,dew_point_2m,relative_humidity_2m,cloud_cover,precipitation_probability,precipitation,pressure_msl,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,shortwave_radiation,sunshine_duration", String.serializer())) }
                            query("daily") { description = "Comma-separated daily variables."; schema = JsonSchema(type = JsonType.STRING, default = GenericElementWrapper("temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_probability_max", String.serializer())) }
                            query("minutely_15") { description = "Comma-separated 15-minute variables."; schema = JsonSchema(type = JsonType.STRING) }
                            query("temperature_unit") { description = "celsius (default) or fahrenheit."; schema = JsonSchema(type = JsonType.STRING) }
                            query("wind_speed_unit") { description = "kmh (default), ms, mph, or kn."; schema = JsonSchema(type = JsonType.STRING, default = GenericElementWrapper("ms", String.serializer())) }
                            query("precipitation_unit") { description = "mm (default) or inch."; schema = JsonSchema(type = JsonType.STRING) }
                            query("timeformat") { description = "iso8601 (default), unixtime, or utc_offset_seconds."; schema = JsonSchema(type = JsonType.STRING) }
                            query("timezone") { description = "IANA timezone or auto. Required when requesting daily variables."; schema = JsonSchema(type = JsonType.STRING, default = GenericElementWrapper("auto", String.serializer())) }
                            query("past_days") { description = "Number of past days to include."; schema = JsonSchema(type = JsonType.INTEGER) }
                            query("forecast_days") { description = "Forecast range in days (1–16, model dependent)."; schema = JsonSchema(type = JsonType.INTEGER) }
                            query("past_hours") { description = "Number of past hourly steps to include."; schema = JsonSchema(type = JsonType.INTEGER) }
                            query("forecast_hours") { description = "Number of forecast hourly steps to include."; schema = JsonSchema(type = JsonType.INTEGER) }
                            query("start_date") { description = "Start date in YYYY-MM-DD format."; schema = JsonSchema(type = JsonType.STRING) }
                            query("end_date") { description = "End date in YYYY-MM-DD format."; schema = JsonSchema(type = JsonType.STRING) }
                            query("start_hour") { description = "Start time in ISO-8601 local date-time format."; schema = JsonSchema(type = JsonType.STRING) }
                            query("end_hour") { description = "End time in ISO-8601 local date-time format."; schema = JsonSchema(type = JsonType.STRING) }
                            query("models") { description = "Optional comma-separated model selection."; schema = JsonSchema(type = JsonType.STRING) }
                            query("cell_selection") { description = "land (default), sea, or nearest grid-cell selection."; schema = JsonSchema(type = JsonType.STRING) }
                            query("apikey") { description = "API key for commercial customer endpoints."; schema = JsonSchema(type = JsonType.STRING) }
                        }
                        responses {
                            response(200) {
                                description = "Normalized AreaAssist WeatherReport produced by the attached Weaver transformation."
                                schema = OpenMeteoWeatherSchema.schema.toOpenApiSchema(openMeteoProviderResponseSchema)
                            }
                            response(400) {
                                description = "Invalid query or weather variable. Error body contains `error` and `reason`."
                                schema = JsonSchema(type = JsonType.OBJECT)
                            }
                        }
                    },
                ),
            ),
        ),
    )
}
