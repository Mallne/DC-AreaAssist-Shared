package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.weaver.core.Weaver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class BrightSkyWeatherAlertSchemaTest {
    @Test
    fun preservesLegacyAlertFieldsAndWarningCellLocation() {
        val providerResponse = Json.parseToJsonElement(
            """
            {
              "alerts": [{
                "id": 42,
                "alert_id": "dwd-alert-42",
                "status": "actual",
                "effective": "2026-11-18T09:00:00Z",
                "onset": "2026-11-18T10:00:00Z",
                "expires": "2026-11-18T12:00:00Z",
                "category": "met",
                "response_type": "prepare",
                "urgency": "immediate",
                "severity": "severe",
                "certainty": "likely",
                "event_code": 1234,
                "event_en": "Heavy rain",
                "event_de": "Starkregen",
                "headline_en": "Heavy rain warning",
                "headline_de": "Amtliche Warnung vor Starkregen",
                "description_en": "Heavy rain is expected.",
                "description_de": "Es wird Starkregen erwartet.",
                "instruction_en": "Avoid flooded roads.",
                "instruction_de": "Überflutete Straßen meiden."
              }],
              "location": {
                "warn_cell_id": 123456,
                "name": "Berlin",
                "name_short": "B"
              }
            }
            """.trimIndent(),
        )

        val transformed = Weaver().engine(BrightSkyWeatherSchemas.alertReport).executeElement(providerResponse)
        val report = Json.decodeFromJsonElement<WeatherAlertReport>(transformed)

        assertEquals(
            WeatherAlert(
                id = "dwd-alert-42",
                headline = "Heavy rain warning",
                description = "Heavy rain is expected.",
                severity = WeatherAlertSeverity.Severe,
                onset = Instant.parse("2026-11-18T10:00:00Z"),
                expires = Instant.parse("2026-11-18T12:00:00Z"),
                sourceId = 42,
                status = "actual",
                effective = Instant.parse("2026-11-18T09:00:00Z"),
                category = "met",
                responseType = "prepare",
                urgency = "immediate",
                certainty = "likely",
                eventCode = 1234,
                eventEn = "Heavy rain",
                eventDe = "Starkregen",
                headlineEn = "Heavy rain warning",
                headlineDe = "Amtliche Warnung vor Starkregen",
                descriptionEn = "Heavy rain is expected.",
                descriptionDe = "Es wird Starkregen erwartet.",
                instructionEn = "Avoid flooded roads.",
                instructionDe = "Überflutete Straßen meiden.",
            ),
            report.alerts.single(),
        )
        assertEquals(WeatherAlertLocation(123456, "Berlin", "B"), report.location)
    }
}
