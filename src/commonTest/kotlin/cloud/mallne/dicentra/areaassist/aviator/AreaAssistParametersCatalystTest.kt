package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.geokit.Boundary
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AreaAssistParametersCatalystTest {
    @Test
    fun `typed boundary query list and boolean retain their JSON types`() {
        val parameters = AreaAssistParameters.parameters(
            queries = listOf(AreaAssistParameters.parcelIdQuery("parcel-7")),
            returnGeometry = false,
            boundary = Boundary(north = 52.0, east = 13.0, south = 50.0, west = 11.0),
        )

        val wire = parameters.toCatalystParameters(Json)

        assertEquals("cloud.mallne.geokit.Boundary", wire.getValue(AreaAssistParameters.BOUNDARY).codec)
        assertEquals("cloud.mallne.dicentra.areaassist.model.curator.Query", wire.getValue(AreaAssistParameters.QUERIES).codec)
        assertEquals(1, wire.getValue(AreaAssistParameters.QUERIES).values.size)
        assertEquals(JsonPrimitive(false), wire.getValue(AreaAssistParameters.RETURN_GEOMETRY).values.single())
        assertEquals("SINGLE", wire.getValue(AreaAssistParameters.BOUNDARY).cardinality.name)
        assertEquals("MULTI", wire.getValue(AreaAssistParameters.QUERIES).cardinality.name)
    }
}
