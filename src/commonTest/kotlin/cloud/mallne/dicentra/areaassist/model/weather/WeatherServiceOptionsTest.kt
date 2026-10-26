package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.statics.APIs
import io.ktor.openapi.OpenApiDoc
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherServiceOptionsTest {
    @Test
    fun providerMetadataRoundTripsCoverageGeometryAndGlobalMarkerIsEmpty() {
        val geometry = MultiPolygon(
            coordinates = listOf(
                listOf(
                    listOf(Position(1.0, 2.0), Position(3.0, 2.0), Position(3.0, 4.0), Position(1.0, 2.0)),
                ),
            ),
        )
        val options = WeatherServiceOptions(
            coverage = geometry,
        )

        val decoded = Json.decodeFromString<WeatherServiceOptions>(Json.encodeToString(options))

        assertEquals(geometry, decoded.coverage)
        assertEquals(options, decoded)
        assertTrue(WeatherServiceOptions.OpenMeteo.coverage == null)
        assertEquals("areaassist-weather-report-v1", WeatherServiceOptions.OpenMeteo.weaverSchema?.key)
    }

    @Test
    fun builtInWeatherDefinitionCanBeSerializedForCodexDiscovery() {
        val discoveryDocument = APIs.apiOverrideVersion().first { it.info.title == APIs.openMeteo.info.title }

        val encoded = Json.encodeToString(OpenApiDoc.serializer(), discoveryDocument)

        assertTrue(encoded.contains("x-dicentra-weaver-schema"))
        assertTrue(encoded.isNotBlank())
    }
}
