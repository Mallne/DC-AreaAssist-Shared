package cloud.mallne.dicentra.areaassist.model.weather

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
}
