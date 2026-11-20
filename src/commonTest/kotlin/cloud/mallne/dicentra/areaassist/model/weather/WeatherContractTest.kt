package cloud.mallne.dicentra.areaassist.model.weather

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class WeatherContractTest {
    @Test
    fun missingUnsupportedEmptyAndFailedSectionsRemainDistinct() {
        val context = WeatherContext(
            latitude = 52.5,
            longitude = 13.4,
            timezone = "Europe/Berlin",
            retrievedAt = Instant.fromEpochMilliseconds(0),
            license = "https://example.test/license",
        )
        val report = WeatherReport(
            context = context,
            current = WeatherSection.Unavailable,
            hourly = WeatherSection.Available(emptyList()),
            daily = WeatherSection.Unsupported,
            alerts = WeatherSection.Failed("provider request failed"),
        )

        assertEquals(WeatherSection.Unavailable, report.current)
        assertEquals(WeatherSection.Available(emptyList()), report.hourly)
        assertEquals(WeatherSection.Unsupported, report.daily)
        assertEquals(WeatherSection.Failed("provider request failed"), report.alerts)
    }

    @Test
    fun amountRetainsItsAccumulationPeriodAndSemantics() {
        val precipitation = WeatherAmount(
            value = 1.2,
            periodMinutes = 60,
            aggregation = WeatherAggregation.Sum,
        )

        assertEquals(60, precipitation.periodMinutes)
        assertEquals(WeatherAggregation.Sum, precipitation.aggregation)
    }

    @Test
    fun normalizedAlertSummaryRoundTrips() {
        val alert = WeatherAlert(
            id = "dwd-123",
            headline = "Heavy rain warning",
            description = "Heavy rain expected.",
            severity = WeatherAlertSeverity.Severe,
            onset = Instant.parse("2026-11-18T10:00:00Z"),
            expires = Instant.parse("2026-11-18T12:00:00Z"),
        )

        assertEquals(alert, Json.decodeFromString<WeatherAlert>(Json.encodeToString(alert)))
    }

    @Test
    fun weatherContextKeepsRequestedAndReturnedCoordinatesDistinct() {
        val context = WeatherContext(
            latitude = 52.53,
            longitude = 13.41,
            requestedLatitude = 52.52,
            requestedLongitude = 13.40,
        )

        val decoded = Json.decodeFromString<WeatherContext>(Json.encodeToString(context))

        assertEquals(52.52, decoded.requestedLatitude)
        assertEquals(13.40, decoded.requestedLongitude)
        assertEquals(52.53, decoded.latitude)
        assertEquals(13.41, decoded.longitude)
    }
}
