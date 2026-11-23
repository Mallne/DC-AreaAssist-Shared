package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils.optionBundle
import cloud.mallne.dicentra.aviator.koas.typed.routes
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
        assertTrue(WeatherServiceOptions.OpenMeteo.isGlobalDefault)
        assertEquals(false, WeatherServiceOptions.BrightSky.isGlobalDefault)
        assertEquals("areaassist-weather-report-v1", WeatherServiceOptions.OpenMeteo.weaverSchema?.key)
        assertEquals("open-meteo", WeatherServiceOptions.OpenMeteo.providerId)
        assertEquals("areaassist-weather-report-v1", WeatherServiceOptions.OpenMeteo.contractVersion)
        assertEquals("brightsky", WeatherServiceOptions.BrightSky.providerId)
        assertEquals(
            "open-meteo-request-parameters-v1",
            WeatherServiceOptions.OpenMeteo.requestParametersSchema?.key,
        )
        assertEquals(WeatherSourceProvenance.Model, WeatherServiceOptions.OpenMeteo.sourceProvenance)
        assertEquals(WeatherSourceProvenance.Observation, WeatherServiceOptions.BrightSkyCurrent.sourceProvenance)
        assertEquals(WeatherSourceProvenance.Forecast, WeatherServiceOptions.BrightSkyForecast.sourceProvenance)
        assertEquals("areaassist-brightsky-alerts-v1", WeatherServiceOptions.BrightSkyAlerts.weaverSchema?.key)
    }

    @Test
    fun legacyProviderOptionsDecodeWithAdditiveMetadataDefaults() {
        val legacy = Json.decodeFromString<WeatherServiceOptions>(
            """{"coverage":null,"priority":0}""",
        )

        assertEquals(null, legacy.providerId)
        assertEquals(null, legacy.contractVersion)
        assertEquals(null, legacy.sourceProvenance)
        assertEquals(null, legacy.requestParametersSchema)
        assertEquals(false, legacy.isGlobalDefault)
    }

    @Test
    fun builtInWeatherDefinitionCanBeSerializedForCodexDiscovery() {
        val discoveryDocument = APIs.apiOverrideVersion().first { it.info.title == APIs.openMeteo.info.title }

        val encoded = Json.encodeToString(OpenApiDoc.serializer(), discoveryDocument)

        assertTrue(encoded.contains("x-dicentra-weaver-schema"))
        assertTrue(encoded.isNotBlank())
    }

    @Test
    fun openMeteoDefinitionCarriesModelProvenanceInItsServiceConfiguration() {
        val operation = APIs.openMeteo.routes().single { it.operationId == "OpenMeteoForecast" }
        val configuredOptions = optionBundle<WeatherServiceOptions>(
            checkNotNull(operation.`x-dicentra-aviator-serviceOptions`),
            Json,
        )

        assertEquals(WeatherSourceProvenance.Model, configuredOptions.sourceProvenance)
        assertEquals("open-meteo", configuredOptions.providerId)
        assertEquals("areaassist-weather-report-v1", configuredOptions.contractVersion)
        assertTrue(configuredOptions.isGlobalDefault)
        val encoded = Json.encodeToString(OpenApiDoc.serializer(), APIs.openMeteo)
        assertTrue(encoded.contains("\"default\":\"auto\""))
        assertTrue(encoded.contains("\"default\":\"ms\""))
        assertEquals(
            "open-meteo-request-parameters-v1",
            configuredOptions.requestParametersSchema?.key,
        )
    }
}
