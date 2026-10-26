package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.weaver.core.specification.ObjectType
import cloud.mallne.dicentra.weaver.core.specification.Wol
import cloud.mallne.dicentra.weaver.core.specification.weaverSchema

/** Open-Meteo's provider-specific response projection into [WeatherReport]. */
object OpenMeteoWeatherSchema {
    private val root = Wol.path("latitude")
    private val current = Wol.path("current")
    private val hourly = Wol.path("hourly")
    private val daily = Wol.path("daily")

    val schema = weaverSchema("areaassist-weather-report-v1") {
        root("weatherReport", Wol.reference("report"))

        transform("availableTag", root.asExpression()) { value(Wol.stringLiteral("available"), ObjectType.String) }
        transform("unsupportedTag", root.asExpression()) { value(Wol.stringLiteral("unsupported"), ObjectType.String) }
        transform("aggregationInstant", root.asExpression()) { value(Wol.stringLiteral("Instant"), ObjectType.String) }
        transform("aggregationMean", root.asExpression()) { value(Wol.stringLiteral("Mean"), ObjectType.String) }
        transform("aggregationSum", root.asExpression()) { value(Wol.stringLiteral("Sum"), ObjectType.String) }
        transform("aggregationMaximum", root.asExpression()) { value(Wol.stringLiteral("Maximum"), ObjectType.String) }
        // WeatherAmount.periodMinutes is an Int; Number literals otherwise serialize as 60.0.
        transform("period60", root.asExpression()) { value("|60|i|", ObjectType.Number) }

        transform("context", root.asExpression()) {
            objectValue {
                source("latitude", Wol.path("latitude"))
                source("longitude", Wol.path("longitude"))
                source("timezone", Wol.path("timezone"), ObjectType.String)
            }
        }

        transform("currentPrecipitation", current.field("precipitation").asExpression()) {
            objectValue {
                source(WeatherAmount::value, current.field("precipitation"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationSum")
            }
        }
        transform("currentWindSpeed", current.field("wind_speed_10m").asExpression()) {
            objectValue {
                source(WeatherAmount::value, current.field("wind_speed_10m"))
                use(WeatherAmount::aggregation, "aggregationInstant")
            }
        }
        transform("currentWindGusts", current.field("wind_gusts_10m").asExpression()) {
            objectValue {
                source(WeatherAmount::value, current.field("wind_gusts_10m"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationMaximum")
            }
        }
        transform("currentMeasurements", current.asExpression()) {
            objectValue {
                source("temperatureCelsius", current.field("temperature_2m"))
                source("wmoWeatherCode", current.field("weather_code"), coercion = "i")
                source("apparentTemperatureCelsius", current.field("apparent_temperature"))
                source("relativeHumidityPercent", current.field("relative_humidity_2m"))
                source("cloudCoverPercent", current.field("cloud_cover"))
                source("pressureHectopascals", current.field("pressure_msl"))
                source("windDirectionDegrees", current.field("wind_direction_10m"))
                use("precipitationMillimeters", "currentPrecipitation")
                use("windSpeedMetersPerSecond", "currentWindSpeed")
                use("windGustMetersPerSecond", "currentWindGusts")
            }
        }
        transform("currentConditions", current.asExpression()) {
            objectValue {
                use("context", "context")
                source("observedAt", current.field("time"), ObjectType.String)
                use("measurements", "currentMeasurements")
            }
        }
        transform("currentSection", current.asExpression()) {
            objectValue {
                use("type", "availableTag")
                use("value", "currentConditions")
            }
        }

        transform("hourlyPrecipitation", hourly.field("precipitation").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source(WeatherAmount::value, hourly.field("precipitation").index("index"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationSum")
            }
        }
        transform("hourlyWindSpeed", hourly.field("wind_speed_10m").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source(WeatherAmount::value, hourly.field("wind_speed_10m").index("index"))
                use(WeatherAmount::aggregation, "aggregationInstant")
            }
        }
        transform("hourlyWindGusts", hourly.field("wind_gusts_10m").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source(WeatherAmount::value, hourly.field("wind_gusts_10m").index("index"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationMaximum")
            }
        }
        transform("hourlyRadiation", hourly.field("shortwave_radiation").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source(WeatherAmount::value, hourly.field("shortwave_radiation").index("index"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationMean")
            }
        }
        transform("hourlySunshine", hourly.field("sunshine_duration").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source(WeatherAmount::value, hourly.field("sunshine_duration").index("index"))
                use(WeatherAmount::periodMinutes, "period60")
                use(WeatherAmount::aggregation, "aggregationSum")
            }
        }
        transform("hourlyMeasurements", hourly.asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source("temperatureCelsius", hourly.field("temperature_2m").index("index"))
                source("wmoWeatherCode", hourly.field("weather_code").index("index"), coercion = "i")
                source("apparentTemperatureCelsius", hourly.field("apparent_temperature").index("index"))
                source("dewPointCelsius", hourly.field("dew_point_2m").index("index"))
                source("relativeHumidityPercent", hourly.field("relative_humidity_2m").index("index"))
                source("cloudCoverPercent", hourly.field("cloud_cover").index("index"))
                source("precipitationProbabilityPercent", hourly.field("precipitation_probability").index("index"))
                source("pressureHectopascals", hourly.field("pressure_msl").index("index"))
                source("visibilityMeters", hourly.field("visibility").index("index"))
                source("windDirectionDegrees", hourly.field("wind_direction_10m").index("index"))
                use("precipitationMillimeters", "hourlyPrecipitation")
                use("windSpeedMetersPerSecond", "hourlyWindSpeed")
                use("windGustMetersPerSecond", "hourlyWindGusts")
                use("shortwaveRadiationWattsPerSquareMeter", "hourlyRadiation")
                use("sunshineSeconds", "hourlySunshine")
            }
        }
        transform("hourlyItem", hourly.field("time").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                use("context", "context")
                source("timestamp", hourly.field("time").index("index"), ObjectType.String)
                use("measurements", "hourlyMeasurements", mapOf("index" to "##index##"))
            }
        }
        transform("hourlyList", hourly.field("time").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            array("{#hourlyItem#(index=##index##)}")
        }
        transform("hourlySection", hourly.asExpression()) {
            objectValue {
                use("type", "availableTag")
                use("value", "hourlyList")
            }
        }

        transform("dailyItem", daily.field("time").index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            objectValue {
                source("date", daily.field("time").index("index"), ObjectType.String)
                source("minimumTemperatureCelsius", daily.field("temperature_2m_min").index("index"))
                source("maximumTemperatureCelsius", daily.field("temperature_2m_max").index("index"))
                source("precipitationSumMillimeters", daily.field("precipitation_sum").index("index"))
                source("precipitationProbabilityMaximumPercent", daily.field("precipitation_probability_max").index("index"))
            }
        }
        transform("dailyList", daily.field("time").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
            array("{#dailyItem#(index=##index##)}")
        }
        transform("dailySection", daily.asExpression()) {
            objectValue {
                use("type", "availableTag")
                use("value", "dailyList")
            }
        }
        transform("unsupportedSection", root.asExpression()) {
            objectValue { use("type", "unsupportedTag") }
        }
        transform("report", root.asExpression()) {
            objectValue {
                use("context", "context")
                use("current", "currentSection")
                use("hourly", "hourlySection")
                use("daily", "dailySection")
                use("alerts", "unsupportedSection")
            }
        }
    }
}
