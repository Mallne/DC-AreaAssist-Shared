package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import io.ktor.openapi.*

object WeatherConstants {
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
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.WARN_CELL_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
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
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.MAX_DIST) {
                    schema = JsonSchema(type = JsonType.STRING)
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
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.BBOX) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DISTANCE) {
                    schema = JsonSchema(type = JsonType.STRING)
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
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.MAX_DIST) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
            }
        }
    )
    val WEATHER_SYNOP = PathItem(
        summary = "Weather Synop",
        get = Operation.build {
            operationId = "WeatherSynop"
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
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
            `x-dicentra-aviator-serviceOptions` = WeatherServiceOptions(
                serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
            ).usable()
            parameters {
                query(Path.Parameters.DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LAST_DATE) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LAT) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.LON) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.DWD_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.WMO_STATION_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.SOURCE_ID) {
                    schema = JsonSchema(type = JsonType.STRING)
                }
                query(Path.Parameters.MAX_DIST) {
                    schema = JsonSchema(type = JsonType.STRING)
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