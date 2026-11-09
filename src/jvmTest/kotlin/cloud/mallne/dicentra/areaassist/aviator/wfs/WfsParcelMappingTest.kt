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
import cloud.mallne.dicentra.areaassist.statics.api.DE_BB
import cloud.mallne.dicentra.areaassist.statics.api.DE_HE
import cloud.mallne.dicentra.areaassist.statics.api.DE_ST
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

    @Test
    fun saxonyAnhaltProjectedCoordinatesStayNearTheirWfsBbox() = runTest {
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
            ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        }
        val response = """
            <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
              xmlns:gml="http://www.opengis.net/gml/3.2"
              xmlns:ave="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              xmlns="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              numberReturned="1" numberMatched="1" timeStamp="2026-11-09T00:00:00Z">
              <wfs:member><ave:Flurstueck gml:id="DESTLIKA0000qSPfFL">
                <ave:flstkennz>15132203000005______</ave:flstkennz>
                <ave:geometrie><gml:MultiSurface srsName="urn:ogc:def:crs:EPSG::25832">
                  <gml:surfaceMember><gml:Polygon><gml:exterior><gml:LinearRing>
                    <gml:posList>668785.856 5760437.649 668809.761 5760432.081 668829.419 5760451.363 668785.856 5760437.649</gml:posList>
                  </gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember>
                </gml:MultiSurface></ave:geometrie>
              </ave:Flurstueck></wfs:member>
            </wfs:FeatureCollection>
        """.trimIndent()

        HttpClient(MockEngine {
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(DE_ST.value) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertEquals(1, parcels.size)
            assertEquals("15132203000005______", parcels.single().parcel.parcelId)
            val geometry = assertIs<MultiPolygon>(parcels.single().parcel.json.geometry)
            val positions = geometry.coordinates.flatten().flatten()
            assertTrue(positions.isNotEmpty())
            assertEquals(11.4571057807, positions.first().longitude, absoluteTolerance = 1e-6)
            assertEquals(51.9689625767, positions.first().latitude, absoluteTolerance = 1e-6)
            positions.forEach { position ->
                assertTrue(position.latitude in 51.9..52.1, "Expected requested Saxony-Anhalt latitude, got $position")
                assertTrue(position.longitude in 11.3..11.6, "Expected requested Saxony-Anhalt longitude, got $position")
            }
        }
    }

    @Test
    fun brandenburgWfsCoordinatesMapToProviderGeographicReference() = runTest {
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25833)
            ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        }
        val response = """
            <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
              xmlns:gml="http://www.opengis.net/gml/3.2"
              xmlns="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              numberReturned="1" numberMatched="1" timeStamp="2026-11-09T00:00:00Z">
              <wfs:member><Flurstueck gml:id="DEBBAL010007c4j7FL">
                <flstkennz>12050102501696______</flstkennz>
                <geometrie><gml:MultiSurface srsName="urn:ogc:def:crs:EPSG::25832">
                  <gml:surfaceMember><gml:Polygon><gml:exterior><gml:LinearRing>
                    <gml:posList>775818.825587298 5812929.32225837 775899.014736332 5812917.84812822 775900.004500647 5812917.70754436 775818.825587298 5812929.32225837</gml:posList>
                  </gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember>
                </gml:MultiSurface></geometrie>
              </Flurstueck></wfs:member>
            </wfs:FeatureCollection>
        """.trimIndent()

        HttpClient(MockEngine {
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(DE_BB.value) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertEquals(1, parcels.size)
            assertEquals("12050102501696______", parcels.single().parcel.parcelId)
            val geometry = assertIs<MultiPolygon>(parcels.single().parcel.json.geometry)
            val first = geometry.coordinates.flatten().flatten().first()

            // GeoBasis-BB returned this same feature in EPSG:4326 as
            // (52.3969704761363, 13.0545402852741) for the source vertex above.
            assertEquals(13.0545402852741, first.longitude, absoluteTolerance = 1e-7)
            assertEquals(52.3969704761363, first.latitude, absoluteTolerance = 1e-7)
        }
    }

    @Test
    fun hessenFrankfurtParcelMapsToProviderGeographicReference() = runTest {
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
            ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        }
        val response = """
            <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
              xmlns:gml="http://www.opengis.net/gml/3.2"
              xmlns="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              numberReturned="1" numberMatched="1" timeStamp="2026-11-09T00:00:00Z">
              <wfs:member><Flurstueck gml:id="DEHE06200001biAQFL">
                <flstkennz>060464125000010002__</flstkennz>
                <geometrie><gml:MultiSurface srsName="urn:ogc:def:crs:EPSG::25832">
                  <gml:surfaceMember><gml:Polygon><gml:exterior><gml:LinearRing>
                    <gml:posList>476930.215 5552187.184 476941.155 5552171.312 476952.358 5552178.837 476941.343 5552194.816 476930.215 5552187.184</gml:posList>
                  </gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember>
                </gml:MultiSurface></geometrie>
              </Flurstueck></wfs:member>
            </wfs:FeatureCollection>
        """.trimIndent()

        HttpClient(MockEngine {
            respond(response, headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(DE_HE.value) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertEquals(1, parcels.size)
            assertEquals("060464125000010002__", parcels.single().parcel.parcelId)
            val geometry = assertIs<MultiPolygon>(parcels.single().parcel.json.geometry)
            val first = geometry.coordinates.flatten().flatten().first()

            // HVBG returned this same Frankfurt feature in EPSG:4326 as
            // (50.1214780107256, 8.67728366901777) for the source vertex above.
            assertEquals(8.67728366901777, first.longitude, absoluteTolerance = 1e-7)
            assertEquals(50.1214780107256, first.latitude, absoluteTolerance = 1e-7)
        }
    }

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
