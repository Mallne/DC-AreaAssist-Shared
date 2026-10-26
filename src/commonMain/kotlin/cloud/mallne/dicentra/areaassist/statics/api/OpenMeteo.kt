package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.weather.WeatherServiceOptions
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import io.ktor.openapi.*

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
                                required = true
                                description = "WGS84 latitude."
                                schema = JsonSchema(type = JsonType.NUMBER)
                            }
                            query("longitude") {
                                required = true
                                description = "WGS84 longitude."
                                schema = JsonSchema(type = JsonType.NUMBER)
                            }
                            query("elevation") { description = "Elevation in meters; nan disables statistical downscaling."; schema = JsonSchema(type = JsonType.NUMBER) }
                            query("current") { description = "Comma-separated current variables: temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m,wind_gusts_10m."; schema = JsonSchema(type = JsonType.STRING) }
                            query("hourly") { description = "Comma-separated hourly variables, e.g. temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,pressure_msl,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,shortwave_radiation,sunshine_duration,weather_code."; schema = JsonSchema(type = JsonType.STRING) }
                            query("daily") { description = "Comma-separated daily variables, e.g. temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_probability_max."; schema = JsonSchema(type = JsonType.STRING) }
                            query("minutely_15") { description = "Comma-separated 15-minute variables."; schema = JsonSchema(type = JsonType.STRING) }
                            query("temperature_unit") { description = "celsius (default) or fahrenheit."; schema = JsonSchema(type = JsonType.STRING) }
                            query("wind_speed_unit") { description = "kmh (default), ms, mph, or kn. AreaAssist requests ms so values match the canonical weather contract."; schema = JsonSchema(type = JsonType.STRING) }
                            query("precipitation_unit") { description = "mm (default) or inch."; schema = JsonSchema(type = JsonType.STRING) }
                            query("timeformat") { description = "iso8601 (default), unixtime, or utc_offset_seconds."; schema = JsonSchema(type = JsonType.STRING) }
                            query("timezone") { description = "IANA timezone or auto. Required when requesting daily variables."; schema = JsonSchema(type = JsonType.STRING) }
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
                                description = "Forecast JSON. Includes coordinates, elevation, timezone metadata, and requested current/hourly/daily blocks with corresponding units."
                                schema = JsonSchema(
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
