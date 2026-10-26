package cloud.mallne.dicentra.areaassist.model.weather

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException
import kotlin.time.Instant

class WeatherReportSerializerTest {
    @Test
    fun missingTimezoneDoesNotSilentlyUseTheDeviceTimezone() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString<WeatherReport>("""{"context":{"latitude":48.87,"longitude":2.35},"current":{"type":"available","value":{"observedAt":"2026-10-23T15:00"}},"hourly":{"type":"unsupported"},"daily":{"type":"unsupported"}}""")
        }
    }

    @Test
    fun localWeatherTimesUseTheReportedTimezone() {
        val report = Json.decodeFromString<WeatherReport>("""
            {"context":{"latitude":48.87,"longitude":2.35,"timezone":"Europe/Paris"},
             "current":{"type":"available","value":{"context":{"latitude":48.87,"longitude":2.35},"observedAt":"2026-10-23T15:00","measurements":{"temperatureCelsius":18.0}}},
             "hourly":{"type":"available","value":[{"context":{"latitude":48.87,"longitude":2.35},"timestamp":"2026-10-23T16:00","measurements":{"temperatureCelsius":19.0}}]},
             "daily":{"type":"unsupported"}}
        """.trimIndent())
        assertEquals(Instant.parse("2026-10-23T13:00:00Z"), (report.current as WeatherSection.Available).value.observedAt)
        assertEquals(Instant.parse("2026-10-23T14:00:00Z"), (report.hourly as WeatherSection.Available).value.single().timestamp)
        assertEquals(report, Json.decodeFromString<WeatherReport>(Json.encodeToString(report)))
    }
}
