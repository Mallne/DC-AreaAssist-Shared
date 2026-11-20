package cloud.mallne.dicentra.areaassist.model.weather

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalTime::class)
class WeatherSourceTest {
    @Test
    fun sourceSerializesSharedProvenanceAndOptionalStationAndModelDetails() {
        val source = WeatherSource(
            id = 7,
            observationType = WeatherObservationType.Current,
            provenance = WeatherSourceProvenance.Observation,
            firstRecord = Instant.parse("2026-11-18T09:00:00Z"),
            lastRecord = Instant.parse("2026-11-18T10:00:00Z"),
            latitude = 52.52,
            longitude = 13.41,
            elevation = 34.0,
            distance = 0.0,
            providerName = "Open-Meteo",
            attribution = "CC BY 4.0",
            modelName = "Example weather model",
        )

        val encoded = Json.encodeToString(WeatherSource.serializer(), source)
        val decoded = Json.decodeFromString(WeatherSource.serializer(), encoded)

        assertEquals(source, decoded)
        assertEquals(null, decoded.stationName)
        assertEquals("CC BY 4.0", decoded.attribution)
        assertEquals("Example weather model", decoded.modelName)
        assertEquals(
            WeatherSourceProvenance.Model,
            Json.decodeFromString<WeatherSource>(
                Json.encodeToString(source.copy(provenance = WeatherSourceProvenance.Model)),
            ).provenance,
        )
        assertEquals(
            "\"forecast\"",
            Json.encodeToString(WeatherObservationType.serializer(), WeatherObservationType.Forecast),
        )
    }

    @Test
    fun legacySourcePayloadDecodesWithoutOptionalProvenanceFields() {
        val legacy = """
            {
              "id": 0,
              "observation_type": "current",
              "first_record": "2026-11-18T09:00:00Z",
              "last_record": "2026-11-18T10:00:00Z",
              "lat": 52.52,
              "lon": 13.41,
              "height": 0.0,
              "distance": 0.0
            }
        """.trimIndent()

        val source = Json.decodeFromString<WeatherSource>(legacy)

        assertEquals(null, source.attribution)
        assertEquals(null, source.modelName)
        assertEquals(null, source.provenance)
        assertEquals(WeatherObservationType.Current, source.observationType)
    }
}
