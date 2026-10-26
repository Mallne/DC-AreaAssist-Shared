package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.geokit.coordinates.builtin.BuiltInCoordinateReferenceSystems
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.openapi.OpenApiDoc
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.geojson.MultiPolygon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WfsParcelMappingTest {
    @Test
    fun projectedDefaultNamespaceMapsIntoSharedParcelModels() = runTest { checkMapping("") }

    @Test
    fun namespaceAliasesDoNotChangeTheMappedModel() = runTest { checkMapping("provider:") }

    private suspend fun checkMapping(prefix: String) {
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25833)
            ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        }
        HttpClient(MockEngine {
            respond(response(prefix), headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val json = Serialization()
            val api = json.decodeFromString<OpenApiDoc>(json.encodeToString(APIs.nordrheinWestfalenWfs))
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(api) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertTrue(parcels.isNotEmpty(), "The WFS response must map into shared parcel models")
            val parcel = parcels.single()
            assertEquals("05315005700266______", parcel.parcel.parcelId)
            val geometry = assertIs<MultiPolygon>(parcel.parcel.json.geometry)
            val positions = geometry.coordinates.flatten().flatten()
            assertTrue(positions.isNotEmpty())
            positions.forEach { position ->
                assertTrue(position.latitude in 47.0..55.0, "Expected geographic degrees, got $position")
                assertTrue(position.longitude in 6.0..7.0, "Expected mapped longitude, got $position")
            }
        }
    }

    private fun response(prefix: String) = """
        <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
          xmlns:gml="http://www.opengis.net/gml/3.2"
          xmlns="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
          xmlns:provider="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
          numberReturned="1" numberMatched="1" timeStamp="2026-10-26T10:00:00Z">
          <wfs:boundedBy><gml:Envelope srsName="urn:ogc:def:crs:EPSG::25832">
            <gml:lowerCorner>354899.173 5693946.724</gml:lowerCorner>
            <gml:upperCorner>354906.233 5693951.940</gml:upperCorner>
          </gml:Envelope></wfs:boundedBy>
          <wfs:member><${prefix}Flurstueck gml:id="sample">
            <${prefix}flstkennz>05315005700266______</${prefix}flstkennz>
            <${prefix}geometrie><gml:MultiSurface srsName="urn:ogc:def:crs:EPSG::25832">
              <gml:surfaceMember><gml:Polygon><gml:exterior><gml:LinearRing>
                <gml:posList>354899.173 5693946.724 354906.233 5693946.724 354906.233 5693951.940 354899.173 5693946.724</gml:posList>
              </gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember>
            </gml:MultiSurface></${prefix}geometrie>
          </${prefix}Flurstueck></wfs:member>
        </wfs:FeatureCollection>
    """.trimIndent()
}
