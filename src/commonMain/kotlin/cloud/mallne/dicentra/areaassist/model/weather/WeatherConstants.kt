package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import io.ktor.openapi.*
import kotlinx.serialization.json.JsonObject

object WeatherConstants {
    private fun stringSchema(format: String? = null) =
        JsonSchema(type = JsonType.STRING, format = format)

    private val brightSkyAlertResponseSchema = JsonSchema(
        type = JsonType.OBJECT,
        properties = mapOf(
            "alerts" to ReferenceOr.value(
                JsonSchema(
                    type = JsonType.ARRAY,
                    items = ReferenceOr.value(
                        JsonSchema(
                            type = JsonType.OBJECT,
                            properties = mapOf(
                                "id" to ReferenceOr.value(JsonSchema(type = JsonType.INTEGER)),
                                "alert_id" to ReferenceOr.value(stringSchema()),
                                "status" to ReferenceOr.value(stringSchema()),
                                "effective" to ReferenceOr.value(stringSchema("date-time")),
                                "onset" to ReferenceOr.value(stringSchema("date-time")),
                                "expires" to ReferenceOr.value(stringSchema("date-time")),
                                "category" to ReferenceOr.value(stringSchema()),
                                "response_type" to ReferenceOr.value(stringSchema()),
                                "urgency" to ReferenceOr.value(stringSchema()),
                                "severity" to ReferenceOr.value(stringSchema()),
                                "certainty" to ReferenceOr.value(stringSchema()),
                                "event_code" to ReferenceOr.value(JsonSchema(type = JsonType.INTEGER)),
                                "event_en" to ReferenceOr.value(stringSchema()),
                                "event_de" to ReferenceOr.value(stringSchema()),
                                "headline_en" to ReferenceOr.value(stringSchema()),
                                "headline_de" to ReferenceOr.value(stringSchema()),
                                "description_en" to ReferenceOr.value(stringSchema()),
                                "description_de" to ReferenceOr.value(stringSchema()),
                                "instruction_en" to ReferenceOr.value(stringSchema()),
                                "instruction_de" to ReferenceOr.value(stringSchema()),
                            ),
                        ),
                    ),
                ),
            ),
            "location" to ReferenceOr.value(
                JsonSchema(
                    type = JsonType.OBJECT,
                    properties = mapOf(
                        "warn_cell_id" to ReferenceOr.value(JsonSchema(type = JsonType.INTEGER)),
                        "name" to ReferenceOr.value(stringSchema()),
                        "name_short" to ReferenceOr.value(stringSchema()),
                    ),
                ),
            ),
        ),
    )

    private fun stringArraySchema() = JsonSchema(
        type = JsonType.ARRAY,
        items = ReferenceOr.value(JsonSchema(type = JsonType.STRING)),
    )

    private fun integerArraySchema() = JsonSchema(
        type = JsonType.ARRAY,
        items = ReferenceOr.value(JsonSchema(type = JsonType.INTEGER)),
    )

    object Path {
        object Parameters {
            const val LAT = "lat"
            const val LON = "lon"
            const val TZ = "tz"
            const val WARN_CELL_ID = "warn_cell_id"
            const val DWD_STATION_ID = "dwd_station_id"
            const val WMO_STATION_ID = "wmo_station_id"
            const val SOURCE_ID = "source_id"
            const val MAX_DIST = "max_dist"
            const val UNITS = "units"
            const val DATE = "date"
            const val LAST_DATE = "last_date"
            const val BBOX = "bbox"
            const val FORMAT = "format"
            const val DISTANCE = "distance"
        }
    }

    val WEATHER_ALERTS = PathItem(
        summary = "Weather Alerts",
        get = Operation.build {
            operationId = "WeatherAlerts"
            `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.WEATHER_SERVICE_WARNING.locator(
                ServiceMethods.GATHER
            )
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSkyAlerts.usable()
            responses {
                response(200) {
                    description = "BrightSky weather alerts and warning-cell location."
                    schema = brightSkyAlertResponseSchema
                }
            }
            parameters {
                query(Path.Parameters.LAT) {
                    description = "Latitude; provide together with lon or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -90.0, maximum = 90.0)
                }
                query(Path.Parameters.LON) {
                    description = "Longitude; provide together with lat or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -180.0, maximum = 180.0)
                }
                query(Path.Parameters.WARN_CELL_ID) {
                    schema = JsonSchema(type = JsonType.INTEGER)
                }
                query(Path.Parameters.TZ) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
    val CURRENT_WEATHER = PathItem(
        summary = "Current Weather",
        get = Operation.build {
            operationId = "CurrentSources"
            `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.WEATHER_SERVICE_CURRENT.locator(
                ServiceMethods.GATHER
            )
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSkyCurrent.usable()
            `x-dicentra-aviator-pluginMaterialization` = mapOf(WeaverPlugin.identity to JsonObject(emptyMap()))
            parameters {
                query(Path.Parameters.LAT) {
                    description = "Latitude; provide together with lon or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -90.0, maximum = 90.0)
                }
                query(Path.Parameters.LON) {
                    description = "Longitude; provide together with lat or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -180.0, maximum = 180.0)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = integerArraySchema()
                }
                query(Path.Parameters.MAX_DIST) {
                    description = "Maximum distance from the requested coordinates in meters (0–500,000)."
                    schema = JsonSchema(type = JsonType.INTEGER, minimum = 0.0, maximum = 500_000.0)
                }
                query(Path.Parameters.UNITS) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.TZ) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
    val WEATHER_RADAR = PathItem(
        summary = "Weather Radar",
        get = Operation.build {
            operationId = "WeatherRadar"
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSkyForecast.usable()
            parameters {
                query(Path.Parameters.LAT) {
                    description = "Latitude; provide together with lon or bbox."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -90.0, maximum = 90.0)
                }
                query(Path.Parameters.LON) {
                    description = "Longitude; provide together with lat or bbox."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -180.0, maximum = 180.0)
                }
                query(Path.Parameters.DATE) {
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.BBOX) {
                    schema = integerArraySchema()
                }
                query(Path.Parameters.DISTANCE) {
                    schema = JsonSchema(type = JsonType.INTEGER)
                }
                query(Path.Parameters.FORMAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.TZ) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
    val WEATHER_SOURCES = PathItem(
        summary = "Weather Sources",
        get = Operation.build {
            operationId = "WeatherSources"
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSky.usable()
            parameters {
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -90.0, maximum = 90.0)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -180.0, maximum = 180.0)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = integerArraySchema()
                }
                query(Path.Parameters.MAX_DIST) {
                    description = "Maximum distance from the requested coordinates in meters (0–500,000)."
                    schema = JsonSchema(type = JsonType.INTEGER, minimum = 0.0, maximum = 500_000.0)
                }
            }
        }
    )
    val WEATHER_SYNOP = PathItem(
        summary = "Weather Synop",
        get = Operation.build {
            operationId = "WeatherSynop"
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSky.usable()
            parameters {
                query(Path.Parameters.DATE) {
                    required = true
                    description = "Timestamp of the first weather record to retrieve (ISO 8601)."
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = integerArraySchema()
                }
                query(Path.Parameters.TZ) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.UNITS) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
    val WEATHER = PathItem(
        summary = "Weather",
        get = Operation.build {
            operationId = "WeatherForecast"
            `x-dicentra-aviator-serviceDelegateCall` = APIs.Services.WEATHER_SERVICE_FORECAST.locator(
                ServiceMethods.GATHER
            )
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions.BrightSkyForecast.usable()
            `x-dicentra-aviator-pluginMaterialization` = mapOf(WeaverPlugin.identity to JsonObject(emptyMap()))
            parameters {
                query(Path.Parameters.DATE) {
                    required = true
                    description = "Timestamp of the first weather record to retrieve (ISO 8601)."
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING, format = "date-time")
                }
                query(Path.Parameters.LAT) {
                    description = "Latitude; provide together with lon or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -90.0, maximum = 90.0)
                }
                query(Path.Parameters.LON) {
                    description = "Longitude; provide together with lat or a station/source ID."
                    schema = JsonSchema(type = JsonType.NUMBER, minimum = -180.0, maximum = 180.0)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = stringArraySchema()
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = integerArraySchema()
                }
                query(Path.Parameters.MAX_DIST) {
                    description = "Maximum distance from the requested coordinates in meters (0–500,000)."
                    schema = JsonSchema(type = JsonType.INTEGER, minimum = 0.0, maximum = 500_000.0)
                }
                query(Path.Parameters.TZ) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.UNITS) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
}
