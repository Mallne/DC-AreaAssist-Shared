package cloud.mallne.dicentra.areaassist.model.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** Provider-independent weather data contract. Measurement property names state their canonical units. */
@Serializable
data class WeatherContext(
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
    val retrievedAt: Instant? = null,
    val license: String? = null,
    val requestedLatitude: Double? = null,
    val requestedLongitude: Double? = null,
)

@Serializable
data class WeatherMeasurements(
    val temperatureCelsius: Double? = null,
    val wmoWeatherCode: Int? = null,
    val apparentTemperatureCelsius: Double? = null,
    val dewPointCelsius: Double? = null,
    val relativeHumidityPercent: Double? = null,
    val cloudCoverPercent: Double? = null,
    val precipitationMillimeters: WeatherAmount? = null,
    val precipitationByInterval: WeatherIntervalAmounts? = null,
    val precipitationProbabilityPercent: Double? = null,
    val pressureHectopascals: Double? = null,
    val visibilityMeters: Double? = null,
    val windSpeedMetersPerSecond: WeatherAmount? = null,
    val windSpeedByInterval: WeatherIntervalAmounts? = null,
    val windDirectionDegrees: Double? = null,
    val windGustMetersPerSecond: WeatherAmount? = null,
    val windGustByInterval: WeatherIntervalAmounts? = null,
    val shortwaveRadiationWattsPerSquareMeter: WeatherAmount? = null,
    val shortwaveRadiationByInterval: WeatherIntervalAmounts? = null,
    val sunshineSeconds: WeatherAmount? = null,
    val sunshineByInterval: WeatherIntervalAmounts? = null,
)

/** A value whose period and aggregation meaning must not be discarded during provider translation. */
@Serializable
data class WeatherAmount(
    val value: Double?,
    val periodMinutes: Int? = null,
    val aggregation: WeatherAggregation,
)

/** Preserves BrightSky's supported 10/30/60-minute values without replacing the primary value. */
@Serializable
data class WeatherIntervalAmounts(
    val minute10: WeatherAmount? = null,
    val minute30: WeatherAmount? = null,
    val minute60: WeatherAmount? = null,
)

@Serializable
enum class WeatherAggregation {
    Instant,
    Mean,
    Sum,
    Maximum,
}

@Serializable
enum class WeatherCondition {
    Clear,
    MainlyClear,
    PartlyCloudy,
    Overcast,
    Fog,
    Drizzle,
    Rain,
    Snow,
    Showers,
    Thunderstorm,
    Unknown;

    companion object {
        /** WMO weather interpretation codes, independent of the upstream provider. */
        fun fromWmo(code: Int?): WeatherCondition = when (code) {
            0 -> Clear
            1 -> MainlyClear
            2 -> PartlyCloudy
            3 -> Overcast
            45, 48 -> Fog
            51, 53, 55, 56, 57 -> Drizzle
            61, 63, 65, 66, 67 -> Rain
            71, 73, 75, 77, 85, 86 -> Snow
            80, 81, 82 -> Showers
            95, 96, 99 -> Thunderstorm
            else -> Unknown
        }
    }
}

@Serializable
data class CurrentConditions(
    val context: WeatherContext,
    val observedAt: Instant,
    val measurements: WeatherMeasurements,
    val condition: WeatherCondition = WeatherCondition.fromWmo(measurements.wmoWeatherCode),
)

@Serializable
data class HourlyForecast(
    val context: WeatherContext,
    val timestamp: Instant,
    val measurements: WeatherMeasurements,
    val condition: WeatherCondition = WeatherCondition.fromWmo(measurements.wmoWeatherCode),
)

@Serializable
data class DailyWeatherSummary(
    val date: String,
    val minimumTemperatureCelsius: Double? = null,
    val maximumTemperatureCelsius: Double? = null,
    val precipitationSumMillimeters: Double? = null,
    val precipitationProbabilityMaximumPercent: Double? = null,
)

/** Provider-neutral result container; each capability reports its own availability. */
@Serializable(with = WeatherReportSerializer::class)
data class WeatherReport(
    val context: WeatherContext,
    val current: WeatherSection<CurrentConditions>,
    val hourly: WeatherSection<List<HourlyForecast>>,
    val daily: WeatherSection<List<DailyWeatherSummary>>,
    val alerts: WeatherSection<List<WeatherAlert>> = WeatherSection.Unsupported,
)

/**
 * Normalized alert fields plus optional source details needed to preserve the BrightSky alert
 * contract, including its bilingual text and CAP metadata.
 */
@Serializable
data class WeatherAlert(
    val id: String? = null,
    val headline: String,
    val description: String? = null,
    val severity: WeatherAlertSeverity? = null,
    val onset: Instant? = null,
    val expires: Instant? = null,
    /** BrightSky's numeric response id, distinct from its string alert_id. */
    val sourceId: Int? = null,
    val status: String? = null,
    val effective: Instant? = null,
    val category: String? = null,
    val responseType: String? = null,
    val urgency: String? = null,
    val certainty: String? = null,
    val eventCode: Int? = null,
    val eventEn: String? = null,
    val eventDe: String? = null,
    val headlineEn: String? = null,
    val headlineDe: String? = null,
    val descriptionEn: String? = null,
    val descriptionDe: String? = null,
    val instructionEn: String? = null,
    val instructionDe: String? = null,
)

@Serializable
data class WeatherAlertLocation(
    val warnCellId: Int,
    val name: String,
    val nameShort: String,
)

/** Shared equivalent of the legacy BrightSky alerts-and-location response envelope. */
@Serializable
data class WeatherAlertReport(
    val alerts: List<WeatherAlert>,
    val location: WeatherAlertLocation,
)

@Serializable
enum class WeatherAlertSeverity {
    @SerialName("minor")
    Minor,
    @SerialName("moderate")
    Moderate,
    @SerialName("severe")
    Severe,
    @SerialName("extreme")
    Extreme,
    @SerialName("unknown")
    Unknown,
}

/** Missing/unsupported data is distinct from a successful empty result. */
@Serializable
sealed interface WeatherSection<out T> {
    @Serializable
    @SerialName("loading")
    data object Loading : WeatherSection<Nothing>

    @Serializable
    @SerialName("available")
    data class Available<T>(val value: T) : WeatherSection<T>

    @Serializable
    @SerialName("unsupported")
    data object Unsupported : WeatherSection<Nothing>

    @Serializable
    @SerialName("unavailable")
    data object Unavailable : WeatherSection<Nothing>

    @Serializable
    @SerialName("failed")
    data class Failed(val reason: String? = null) : WeatherSection<Nothing>
}
