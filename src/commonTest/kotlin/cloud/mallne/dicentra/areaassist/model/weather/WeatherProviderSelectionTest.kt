package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.model.areas.AdministrativeArea
import cloud.mallne.dicentra.areaassist.statics.APIs
import io.ktor.openapi.OpenApiDoc
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position
import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherProviderSelectionTest {
    private val germanyLikeArea = object : AdministrativeArea {
        override val id = "TEST"
        override val boundary = MultiPolygon(
            coordinates = listOf(
                listOf(
                    listOf(
                        Position(5.0, 47.0),
                        Position(15.0, 47.0),
                        Position(15.0, 55.0),
                        Position(5.0, 55.0),
                        Position(5.0, 47.0),
                    ),
                ),
            ),
        )
    }

    private val regional = WeatherProviderCandidate(
            options = WeatherServiceOptions.BrightSky.copy(coverage = germanyLikeArea.boundary),
        service = "regional",
    )
    private val global = WeatherProviderCandidate(
        options = WeatherServiceOptions.OpenMeteo,
        service = "global",
    )

    @Test
    fun regionalProviderPrecedesGlobalProviderInsideCoverage() {
        val selected = WeatherProviderSelection.select(
            candidates = listOf(
                global.copy(options = global.options.copy(priority = 1)),
                regional.copy(options = regional.options.copy(priority = 100)),
            ),
            latitude = 52.0,
            longitude = 10.0,
        )

        assertEquals(listOf("regional", "global"), selected.map { it.service })
    }

    @Test
    fun onlyGlobalProviderIsEligibleOutsideRegionalCoverage() {
        val selected = WeatherProviderSelection.select(
            candidates = listOf(regional, global),
            latitude = 40.0,
            longitude = -74.0,
        )

        assertEquals(listOf("global"), selected.map { it.service })
    }

    @Test
    fun brightSkyForecastDefinitionDoesNotSelectInParis() {
        val brightSkyDocument = Json.encodeToJsonElement(OpenApiDoc.serializer(), APIs.brightSky)
        val brightSkyOptionsJson = brightSkyDocument.jsonObject
            .getValue("paths").jsonObject
            .getValue("/weather").jsonObject
            .getValue("get").jsonObject
            .getValue("x-dicentra-aviator-serviceOptions")
        val brightSkyOptions = Json.decodeFromJsonElement(
            WeatherServiceOptions.serializer(),
            brightSkyOptionsJson,
        )
        val selected = WeatherProviderSelection.select(
            candidates = listOf(
                WeatherProviderCandidate(options = brightSkyOptions, service = "brightSky"),
                global,
            ),
            latitude = 48.86,
            longitude = 2.35,
        )

        assertEquals(listOf("global"), selected.map { it.service })
    }

    @Test
    fun providersWithoutCoverageAreGloballyEligible() {
        val configured = regional.copy(
            options = regional.options.copy(coverage = null),
        )
        val selected = WeatherProviderSelection.select(
            candidates = listOf(configured, global),
            latitude = 52.0,
            longitude = 10.0,
        )

        assertEquals(listOf("regional", "global"), selected.map { it.service })
    }
}
