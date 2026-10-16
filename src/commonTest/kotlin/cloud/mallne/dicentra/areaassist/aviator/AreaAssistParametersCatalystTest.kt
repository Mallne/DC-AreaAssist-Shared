package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.aviator.plugin.synapse.toCatalystParameters
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystRequest
import cloud.mallne.dicentra.aviator.plugin.synapse.CatalystBodyFormat
import kotlin.io.encoding.Base64
import kotlin.test.assertContentEquals
import cloud.mallne.geokit.Boundary
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AreaAssistParametersCatalystTest {
    @Test
    fun `XML envelope serialization retains WFS bytes alongside typed AreaAssist inputs`() {
        val xml = """<?xml version="1.0" encoding="UTF-8"?><wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs/2.0" version="2.0.0"><!-- Thüringen &amp; parcels --></wfs:GetFeature>""".encodeToByteArray()
        val request = CatalystRequest(
            body = JsonPrimitive(Base64.Default.encode(xml)),
            bodyFormat = CatalystBodyFormat.Base64("application/xml"),
            typedParameters = AreaAssistParameters.parameters(
                boundary = Boundary(north = 51.0, east = 11.1, south = 50.9, west = 11.0),
                queries = emptyList(),
                returnGeometry = true,
            ).toCatalystParameters(Json),
        )
        val decoded = Json.decodeFromString(CatalystRequest.serializer(), Json.encodeToString(CatalystRequest.serializer(), request))
        assertEquals(request, decoded)
        assertEquals(CatalystBodyFormat.Base64("application/xml"), decoded.bodyFormat)
        assertContentEquals(xml, Base64.Default.decode((decoded.body as JsonPrimitive).content))
    }

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
