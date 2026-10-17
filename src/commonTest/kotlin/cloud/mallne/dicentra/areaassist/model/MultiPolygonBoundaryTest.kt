package cloud.mallne.dicentra.areaassist.model

import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions
import cloud.mallne.dicentra.areaassist.model.areas.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

class MultiPolygonBoundaryTest {
    private fun ring(vararg coordinates: Pair<Double, Double>) = coordinates.map { (longitude, latitude) ->
        Position(longitude, latitude)
    }

    private fun polygon(outer: List<Position>, vararg holes: List<Position>): List<List<Position>> =
        listOf(outer) + holes

    private fun multipolygon(vararg polygons: List<List<Position>>) = MultiPolygon(coordinates = polygons.toList())

    @Test
    fun `multipart polygons and holes raycast independently`() {
        val boundary = multipolygon(
            polygon(
                ring(0.0 to 0.0, 4.0 to 0.0, 4.0 to 4.0, 0.0 to 4.0, 0.0 to 0.0),
                ring(1.0 to 1.0, 3.0 to 1.0, 3.0 to 3.0, 1.0 to 3.0, 1.0 to 1.0),
            ),
            polygon(ring(10.0 to 10.0, 12.0 to 10.0, 12.0 to 12.0, 10.0 to 12.0, 10.0 to 10.0)),
        )

        assertTrue(CalculationExtensions.rayCast(Point(0.5, 0.5), boundary))
        assertFalse(CalculationExtensions.rayCast(Point(2.0, 2.0), boundary))
        assertTrue(CalculationExtensions.rayCast(Point(11.0, 11.0), boundary))
        assertFalse(CalculationExtensions.rayCast(Point(6.0, 6.0), boundary))
    }

    @Test
    fun `GeoKit MultiPolygon serializes and round-trips as GeoJSON`() {
        val boundary = multipolygon(
            polygon(ring(0.0 to 0.0, 1.0 to 0.0, 0.0 to 1.0, 0.0 to 0.0)),
        )
        val encoded = Json.encodeToString(MultiPolygon.serializer(), boundary)
        assertEquals(boundary, Json.decodeFromString(MultiPolygon.serializer(), encoded))
        assertTrue("\"type\":\"MultiPolygon\"" in encoded)
    }

    @Test
    fun `parcel service metadata uses the versioned GeoJSON model`() {
        val options = ParcelServiceOptions(
            boundary = Bundesland.THUERINGEN.boundary,
        )
        val encoded = Json.encodeToString(ParcelServiceOptions.serializer(), options)
        assertTrue("\"boundaryFormatVersion\":2" in encoded)
        assertTrue("\"type\":\"MultiPolygon\"" in encoded)
        assertFalse("\"bounds\"" in encoded)
    }

    @Test
    fun `raycast unwraps antimeridian rings`() {
        val boundary = multipolygon(
            polygon(ring(179.0 to 10.0, -179.0 to 10.0, -179.0 to 12.0, 179.0 to 12.0, 179.0 to 10.0)),
        )

        assertTrue(CalculationExtensions.rayCast(Point(180.0, 11.0), boundary))
        assertTrue(CalculationExtensions.rayCast(Point(-179.5, 11.0), boundary))
        assertFalse(CalculationExtensions.rayCast(Point(0.0, 11.0), boundary))
    }
}
