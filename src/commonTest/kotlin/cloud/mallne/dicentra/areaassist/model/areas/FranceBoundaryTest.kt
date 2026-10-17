package cloud.mallne.dicentra.areaassist.model.areas

import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.rayCast
import cloud.mallne.dicentra.areaassist.model.Point
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FranceBoundaryTest {
    @Test
    fun `France is registered and its multipolygon contains metropolitan and Guiana locations`() {
        val france = AdministrativeAreas.getById("FRA")
        assertNotNull(france)
        assertEquals("FRA", france.id)

        assertTrue(rayCast(Point(2.3522, 48.8566), france.boundary), "Paris should be inside France")
        assertTrue(rayCast(Point(-52.33, 4.94), france.boundary), "Cayenne should be inside French Guiana")
        assertFalse(rayCast(Point(13.405, 52.52), france.boundary), "Berlin should be outside France")
    }
}
