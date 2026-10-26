package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.weaver.core.specification.ObjectType
import cloud.mallne.dicentra.weaver.core.specification.Wol
import cloud.mallne.dicentra.weaver.core.specification.WolAccessorPath
import cloud.mallne.dicentra.weaver.core.specification.weaverSchema

/** BrightSky provider projections into the same [WeatherReport] contract as Open-Meteo. */
object BrightSkyWeatherSchemas {
    private val weather = Wol.path("weather")
    private val sources = Wol.path("sources")
    private val firstSource = sources.element(0)

    val current = build("areaassist-brightsky-current-v1", currentWeather = weather, hourlyWeather = null)
    val forecast = build("areaassist-brightsky-forecast-v1", currentWeather = null, hourlyWeather = weather)

    private fun build(
        key: String,
        currentWeather: WolAccessorPath?,
        hourlyWeather: WolAccessorPath?,
    ) = weaverSchema(key) {
        root("weatherReport", Wol.reference("report"))

        transform("available", "lat") { value(Wol.stringLiteral("available"), ObjectType.String) }
        transform("unsupported", "lat") { value(Wol.stringLiteral("unsupported"), ObjectType.String) }
        transform("unavailable", "lat") { value(Wol.stringLiteral("unavailable"), ObjectType.String) }
        transform("aggregationSum", "lat") { value(Wol.stringLiteral("Sum"), ObjectType.String) }
        transform("aggregationMean", "lat") { value(Wol.stringLiteral("Mean"), ObjectType.String) }
        transform("aggregationInstant", "lat") { value(Wol.stringLiteral("Instant"), ObjectType.String) }
        transform("aggregationMaximum", "lat") { value(Wol.stringLiteral("Maximum"), ObjectType.String) }
        transform("period60", "lat") { value("|60|i|", ObjectType.Number) }
        transform("period10", "lat") { value("|10|i|", ObjectType.Number) }

        transform("context", firstSource.asExpression()) {
            objectValue {
                source("latitude", firstSource.field("lat"))
                source("longitude", firstSource.field("lon"))
            }
        }
        transform("dailyUnsupported", weather.asExpression()) {
            objectValue { use("type", "unsupported") }
        }
        transform("alertsUnsupported", weather.asExpression()) {
            objectValue { use("type", "unsupported") }
        }

        if (currentWeather == null) {
            transform("currentUnavailable", weather.asExpression()) {
                objectValue { use("type", "unavailable") }
            }
        } else {
            transform(
                "currentCondition",
                currentWeather.field("condition").asExpression(),
            ) {
                value(
                    Wol.mapCurrentString(
                        mapOf(
                            "dry" to "Clear",
                            "fog" to "Fog",
                            "rain" to "Rain",
                            "snow" to "Snow",
                            "thunderstorm" to "Thunderstorm",
                        ),
                        "Unknown",
                    ),
                    ObjectType.String,
                )
            }
            transform("currentPrecipitation", currentWeather.field("precipitation_60").asExpression()) {
                objectValue {
                    source(WeatherAmount::value, currentWeather.field("precipitation_60"))
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationSum")
                }
            }
            transform("currentWindValue", currentWeather.field("wind_speed_10").asExpression()) {
                value(Wol.divideCurrent(3.6), ObjectType.Number)
            }
            transform("currentWind", currentWeather.field("wind_speed_10").asExpression()) {
                objectValue {
                    use(WeatherAmount::value, "currentWindValue")
                    use(WeatherAmount::periodMinutes, "period10")
                    use(WeatherAmount::aggregation, "aggregationInstant")
                }
            }
            transform("currentGustValue", currentWeather.field("wind_gust_speed_10").asExpression()) {
                value(Wol.divideCurrent(3.6), ObjectType.Number)
            }
            transform("currentGust", currentWeather.field("wind_gust_speed_10").asExpression()) {
                objectValue {
                    use(WeatherAmount::value, "currentGustValue")
                    use(WeatherAmount::periodMinutes, "period10")
                    use(WeatherAmount::aggregation, "aggregationMaximum")
                }
            }
            transform("currentRadiationValue", currentWeather.field("solar_60").asExpression()) {
                value(Wol.multiplyCurrent(1000.0), ObjectType.Number)
            }
            transform("currentRadiation", currentWeather.field("solar_60").asExpression()) {
                objectValue {
                    use(WeatherAmount::value, "currentRadiationValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationMean")
                }
            }
            transform("currentSunshineValue", currentWeather.field("sunshine_60").asExpression()) {
                value(Wol.multiplyCurrent(60.0), ObjectType.Number)
            }
            transform("currentSunshine", currentWeather.field("sunshine_60").asExpression()) {
                objectValue {
                    use(WeatherAmount::value, "currentSunshineValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationSum")
                }
            }
            transform("currentMeasurements", currentWeather.asExpression()) {
                objectValue {
                    source("temperatureCelsius", currentWeather.field("temperature"))
                    source("cloudCoverPercent", currentWeather.field("cloud_cover"))
                    source("dewPointCelsius", currentWeather.field("dew_point"))
                    source("pressureHectopascals", currentWeather.field("pressure_msl"))
                    source("relativeHumidityPercent", currentWeather.field("relative_humidity"))
                    source("visibilityMeters", currentWeather.field("visibility"))
                    source("windDirectionDegrees", currentWeather.field("wind_direction_10"))
                    use("precipitationMillimeters", "currentPrecipitation")
                    use("windSpeedMetersPerSecond", "currentWind")
                    use("windGustMetersPerSecond", "currentGust")
                    use("shortwaveRadiationWattsPerSquareMeter", "currentRadiation")
                    use("sunshineSeconds", "currentSunshine")
                }
            }
            transform("currentConditions", currentWeather.asExpression()) {
                objectValue {
                    use("context", "context")
                    source("observedAt", currentWeather.field("timestamp"), ObjectType.String)
                    use("measurements", "currentMeasurements")
                    use("condition", "currentCondition")
                }
            }
            transform("currentSection", currentWeather.asExpression()) {
                objectValue {
                    use("type", "available")
                    use("value", "currentConditions")
                }
            }
        }

        if (hourlyWeather == null) {
            transform("hourlyUnavailable", weather.asExpression()) {
                objectValue { use("type", "unavailable") }
            }
        } else {
            transform("forecastCondition", hourlyWeather.index("index").field("condition").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                value(
                    Wol.mapCurrentString(
                        mapOf(
                            "dry" to "Clear",
                            "fog" to "Fog",
                            "rain" to "Rain",
                            "snow" to "Snow",
                            "thunderstorm" to "Thunderstorm",
                        ),
                        "Unknown",
                    ),
                    ObjectType.String,
                )
            }
            transform("forecastPrecipitation", hourlyWeather.index("index").field("precipitation").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    source(WeatherAmount::value, hourlyWeather.index("index").field("precipitation"))
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationSum")
                }
            }
            transform("forecastWindValue", hourlyWeather.index("index").field("wind_speed").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                value(Wol.divideCurrent(3.6), ObjectType.Number)
            }
            transform("forecastWind", hourlyWeather.index("index").field("wind_speed").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    use(WeatherAmount::value, "forecastWindValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationInstant")
                }
            }
            transform("forecastGustValue", hourlyWeather.index("index").field("wind_gust_speed").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                value(Wol.divideCurrent(3.6), ObjectType.Number)
            }
            transform("forecastGust", hourlyWeather.index("index").field("wind_gust_speed").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    use(WeatherAmount::value, "forecastGustValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationMaximum")
                }
            }
            transform("forecastRadiationValue", hourlyWeather.index("index").field("solar").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                value(Wol.multiplyCurrent(1000.0), ObjectType.Number)
            }
            transform("forecastRadiation", hourlyWeather.index("index").field("solar").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    use(WeatherAmount::value, "forecastRadiationValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationMean")
                }
            }
            transform("forecastSunshineValue", hourlyWeather.index("index").field("sunshine").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                value(Wol.multiplyCurrent(60.0), ObjectType.Number)
            }
            transform("forecastSunshine", hourlyWeather.index("index").field("sunshine").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    use(WeatherAmount::value, "forecastSunshineValue")
                    use(WeatherAmount::periodMinutes, "period60")
                    use(WeatherAmount::aggregation, "aggregationSum")
                }
            }
            transform("hourlyMeasurements", hourlyWeather.index("index").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    source("temperatureCelsius", hourlyWeather.index("index").field("temperature"))
                    source("cloudCoverPercent", hourlyWeather.index("index").field("cloud_cover"))
                    source("dewPointCelsius", hourlyWeather.index("index").field("dew_point"))
                    source("relativeHumidityPercent", hourlyWeather.index("index").field("relative_humidity"))
                    source("precipitationProbabilityPercent", hourlyWeather.index("index").field("precipitation_probability"))
                    source("pressureHectopascals", hourlyWeather.index("index").field("pressure_msl"))
                    source("visibilityMeters", hourlyWeather.index("index").field("visibility"))
                    source("windDirectionDegrees", hourlyWeather.index("index").field("wind_direction"))
                    use("precipitationMillimeters", "forecastPrecipitation")
                    use("windSpeedMetersPerSecond", "forecastWind")
                    use("windGustMetersPerSecond", "forecastGust")
                    use("shortwaveRadiationWattsPerSquareMeter", "forecastRadiation")
                    use("sunshineSeconds", "forecastSunshine")
                }
            }
            transform("hourlyItem", hourlyWeather.index("index").field("timestamp").asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                objectValue {
                    use("context", "context")
                    source("timestamp", hourlyWeather.index("index").field("timestamp"), ObjectType.String)
                    use("measurements", "hourlyMeasurements", mapOf("index" to "##index##"))
                    use("condition", "forecastCondition")
                }
            }
            transform("hourlyList", hourlyWeather.asExpression(), parameters = mapOf("index" to ObjectType.Number)) {
                array("{#hourlyItem#(index=##index##)}")
            }
            transform("hourlySection", hourlyWeather.asExpression()) {
                objectValue {
                    use("type", "available")
                    use("value", "hourlyList")
                }
            }
        }

        transform("report", weather.asExpression()) {
            objectValue {
                use("context", "context")
                use("current", if (currentWeather == null) "currentUnavailable" else "currentSection")
                use("hourly", if (hourlyWeather == null) "hourlyUnavailable" else "hourlySection")
                use("daily", "dailyUnsupported")
                use("alerts", "alertsUnsupported")
            }
        }
    }
}
