package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
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
import cloud.mallne.dicentra.areaassist.statics.api.DE
import cloud.mallne.dicentra.areaassist.statics.api.DE_BE
import cloud.mallne.dicentra.areaassist.statics.api.DE_BW
import cloud.mallne.dicentra.areaassist.statics.api.DE_HB
import cloud.mallne.dicentra.areaassist.statics.api.DE_HH
import cloud.mallne.dicentra.areaassist.statics.api.DE_MV
import cloud.mallne.dicentra.areaassist.statics.api.DE_RP
import cloud.mallne.dicentra.areaassist.statics.api.DE_SN
import cloud.mallne.dicentra.areaassist.statics.api.DE_SL
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.http.URLBuilder
import io.ktor.openapi.OpenApiDoc
import kotlinx.coroutines.test.runTest
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WfsParcelMappingTest {
    @Test
    fun hamburgLiveGmlFeatureMapsToSharedParcel() = runTest {
        val liveSample = """
            <wfs:FeatureCollection xmlns:ave="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:wfs="http://www.opengis.net/wfs/2.0"
              timeStamp="2026-11-17T15:22:05Z" numberMatched="unknown" numberReturned="1">
              <wfs:member><ave:Flurstueck gml:id="AVE_FLURSTUECK_1">
                <ave:geometrie><gml:MultiSurface srsName="EPSG:25832">
                  <gml:surfaceMember><gml:Polygon><gml:exterior><gml:LinearRing>
                    <gml:posList>560964.261 5941908.088 560931.515 5941916.147 560956.361 5941952.208 560958.795 5941950.303 560978.401 5941945.931 560982.707 5941945.718 560968.508 5941918.148 560964.261 5941908.088</gml:posList>
                  </gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember>
                </gml:MultiSurface></ave:geometrie>
                <ave:oid>DEHHALKAV00006M6FL</ave:oid><ave:aktualit>2017-12-28Z</ave:aktualit>
                <ave:idflurst>DEHHALKAV00006M6</ave:idflurst><ave:flaeche>1210.0</ave:flaeche>
                <ave:flstkennz>020305___05356______</ave:flstkennz><ave:land>Hamburg</ave:land>
                <ave:landschl>02</ave:landschl><ave:gemarkung>Eidelstedt</ave:gemarkung>
                <ave:gemaschl>020305</ave:gemaschl><ave:flurschl>020305___</ave:flurschl>
                <ave:flstnrzae>5356</ave:flstnrzae><ave:regbezirk>Hamburg</ave:regbezirk>
                <ave:regbezschl>020</ave:regbezschl><ave:kreis>Hamburg</ave:kreis>
                <ave:kreisschl>02000</ave:kreisschl><ave:gemeinde>Freie und Hansestadt Hamburg</ave:gemeinde>
                <ave:gmdschl>02000000</ave:gmdschl><ave:lagebeztxt>Kuhlen</ave:lagebeztxt>
                <ave:tntxt>Landwirtschaft/Weihnachtsbaumkultur;1210</ave:tntxt>
              </ave:Flurstueck></wfs:member>
            </wfs:FeatureCollection>
        """.trimIndent()
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
            ingest(BuiltInCoordinateReferenceSystems.EPSG1149)
        }

        HttpClient(MockEngine {
            respond(liveSample, headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_HH.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(registry))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertEquals(1, parcels.size)
            assertEquals("020305___05356______", parcels.single().parcel.parcelId)
            assertIs<MultiPolygon>(parcels.single().parcel.json.geometry)
        }
    }

    @Test
    fun rhinelandPalatinateLiveGmlFeatureMapsToSharedParcel() = runTest {
        val liveSample = """
            <wfs:FeatureCollection xmlns:ave="http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
              xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:wfs="http://www.opengis.net/wfs/2.0"
              timeStamp="2026-11-18T13:47:22" numberMatched="unknown" numberReturned="1">
              <wfs:boundedBy><gml:Envelope srsName="urn:ogc:def:crs:EPSG::4326">
                <gml:lowerCorner>50.915528 7.742708</gml:lowerCorner>
                <gml:upperCorner>50.918415 7.749504</gml:upperCorner>
              </gml:Envelope></wfs:boundedBy>
              <wfs:member><ave:Flurstueck gml:id="Flurstueck.1">
                <gml:boundedBy><gml:Envelope srsName="urn:ogc:def:crs:EPSG::4326">
                  <gml:lowerCorner>50.915528 7.742708</gml:lowerCorner>
                  <gml:upperCorner>50.918415 7.749504</gml:upperCorner>
                </gml:Envelope></gml:boundedBy>
                <ave:geometrie><gml:Polygon srsName="urn:ogc:def:crs:EPSG::4326">
                  <gml:exterior><gml:LinearRing><gml:posList srsDimension="2">50.916364 7.742708 50.916308 7.743035 50.915912 7.744473 50.916364 7.742708</gml:posList></gml:LinearRing></gml:exterior>
                </gml:Polygon></ave:geometrie>
                <ave:oid>DERPLP010000037XFL</ave:oid><ave:idflurst>DERPLP010000037X</ave:idflurst>
                <ave:flaeche>70267</ave:flaeche><ave:flstkennz>07000100100059______</ave:flstkennz>
                <ave:landschl>07</ave:landschl><ave:gemarkung>Friesenhagen</ave:gemarkung>
                <ave:gemaschl>070001</ave:gemaschl><ave:flur>Flur 1</ave:flur>
                <ave:flurschl>070001001</ave:flurschl><ave:flstnrzae>59</ave:flstnrzae>
                <ave:kreis>Altenkirchen</ave:kreis><ave:kreisschl>07132</ave:kreisschl>
                <ave:gemeinde>Friesenhagen</ave:gemeinde><ave:gmdschl>07132037</ave:gmdschl>
                <ave:abwrecht>Kein abweichender Rechtszustand</ave:abwrecht>
                <ave:lagebeztxt>Schönbach 2</ave:lagebeztxt><ave:tntxt>Grünland;46018</ave:tntxt>
              </ave:Flurstueck></wfs:member>
            </wfs:FeatureCollection>
        """.trimIndent()
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
        }

        HttpClient(MockEngine {
            respond(liveSample, headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_RP.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(registry))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val parcels = assertNotNull(service.request<List<ParcelCrateEntity>, NoBody>())
            assertEquals(1, parcels.size)
            assertEquals("07000100100059______", parcels.single().parcel.parcelId)
            assertIs<Polygon>(parcels.single().parcel.json.geometry)
        }
    }

    @Test
    fun rhinelandPalatinateWfsConfigurationMatchesCapabilitiesAndDescribeFeatureType() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_RP.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(CrsRegistry()))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
            val options = service.optionBundle<ParcelServiceOptions>()

            assertEquals("/wfs/alkis_rp.fcgi", DE_RP.value.paths.keys.single())
            assertEquals("ave:Flurstueck", wfs.configurationBundle.typeNames)
            assertEquals(DE.SIMPLIFIED_ALKIS_NAMESPACE, wfs.configurationBundle.namespace)
            assertEquals("ave", wfs.configurationBundle.nsPrefix)
            assertEquals("geometrie", wfs.configurationBundle.geometryPointer)
            assertEquals("urn:ogc:def:crs:EPSG::4326", wfs.configurationBundle.outputCRS)
            assertEquals("urn:ogc:def:crs:EPSG::4326", wfs.configurationBundle.inputCRS)
            assertEquals("07", options.parcelIdPrefix)
        }

        val providerFields = setOf(
            "geometrie", "oid", "aktualit", "idflurst", "flaeche", "flstkennz",
            "land", "landschl", "gemarkung", "gemaschl", "flur", "flurschl",
            "flstnrzae", "flstnrnen", "regbezirk", "regbezschl", "kreis", "kreisschl",
            "gemeinde", "gmdschl", "abwrecht", "lagebeztxt", "tntxt", "gid",
        )
        val mappedFields = DE.simplifiedAlkisKeys().mapNotNull { it.reference }.toSet()
        assertTrue(mappedFields.all { it in providerFields }, "Unmapped Rheinland-Pfalz fields: ${mappedFields - providerFields}")
    }

    @Test
    fun hamburgWfsConfigurationAndProviderSchemaMatch() = runTest {
        val registry = CrsRegistry().apply {
            ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
            ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
        }

        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_HH.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(registry))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
            val options = service.optionBundle<ParcelServiceOptions>()
            assertEquals("/WFS_HH_ALKIS_vereinfacht", DE_HH.value.paths.keys.single())
            assertEquals("ave:Flurstueck", wfs.configurationBundle.typeNames)
            assertEquals(DE.SIMPLIFIED_ALKIS_NAMESPACE, wfs.configurationBundle.namespace)
            assertEquals("geometrie", wfs.configurationBundle.geometryPointer)
            assertEquals("urn:ogc:def:crs:EPSG::25832", wfs.configurationBundle.inputCRS)
            assertEquals("urn:ogc:def:crs:EPSG::25832", wfs.configurationBundle.outputCRS)
            assertEquals("02", options.parcelIdPrefix)
        }

        val hamburgSchemaFields = setOf(
            "oid", "aktualit", "idflurst", "flaeche", "flstkennz", "land", "landschl",
            "gemarkung", "gemaschl", "flur", "flurschl", "flstnrzae", "flstnrnen",
            "regbezirk", "regbezschl", "kreis", "kreisschl", "gemeinde", "gmdschl",
            "abwrecht", "lagebeztxt", "tntxt", "geometrie",
        )
        val mappedFields = DE.simplifiedAlkisKeys().mapNotNull { it.reference }.toSet()
        assertTrue(mappedFields.all { it in hamburgSchemaFields }, "Unmapped Hamburg fields: ${mappedFields - hamburgSchemaFields}")
    }

    @Test
    fun badenWuerttembergUsesTheAdvertisedGetFeatureKvpBinding() = runTest {
        var method: String? = null
        var requestUrl: String? = null
        HttpClient(MockEngine { request ->
            method = request.method.value
            requestUrl = request.url.toString()
            respond(
                """<wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"/>""",
                headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"),
            )
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_BW.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(CrsRegistry()))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }

            service.request<List<ParcelCrateEntity>, NoBody>()
        }

        assertEquals("GET", method)
        val parameters = URLBuilder(requestUrl.orEmpty()).parameters
        assertEquals("GetFeature", parameters["request"])
        assertEquals("2.0.0", parameters["version"])
        assertEquals("nora:v_al_flurstueck", parameters["typeNames"])
    }

    @Test
    fun berlinFeatureNamespaceMatchesDescribeFeatureTypeContract() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val registry = CrsRegistry().apply {
                ingest(BuiltInCoordinateReferenceSystems.EPSG4326)
                ingest(BuiltInCoordinateReferenceSystems.EPSG25832)
                ingest(BuiltInCoordinateReferenceSystems.EPSG25833)
            }
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_BE.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(registry))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()

            assertEquals("alkis_flurstuecke", wfs.configurationBundle.namespace)
            assertEquals("alkis_flurstuecke", wfs.configurationBundle.nsPrefix)
            assertEquals("alkis_flurstuecke:flurstuecke", wfs.configurationBundle.typeNames)
        }
    }

    @Test
    fun bremenWfsConfigurationMatchesCapabilitiesAndDescribeFeatureType() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val registry = CrsRegistry()
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_HB.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(registry))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()

            assertEquals("app:flurstuecke", wfs.configurationBundle.typeNames)
            assertEquals("http://www.deegree.org/app", wfs.configurationBundle.namespace)
            assertEquals("app", wfs.configurationBundle.nsPrefix)
            assertEquals("geom", wfs.configurationBundle.geometryPointer)
            // Bremen advertises EPSG:25832 by default and supports EPSG:4326 as an alternate CRS.
            assertEquals("urn:ogc:def:crs:EPSG::4326", wfs.configurationBundle.outputCRS)
            assertEquals("urn:ogc:def:crs:EPSG::4326", wfs.configurationBundle.inputCRS)
        }

        // Bremen DescribeFeatureType exposes these simplified ALKIS parcel fields.
        val bremenFields = setOf(
            "oid", "aktualit", "idflurst", "flaeche", "flstkennz", "land", "landschl",
            "gemarkung", "gemaschl", "flur", "flurschl", "flstnrzae", "flstnrnen",
            "regbezirk", "regbezschl", "kreis", "kreisschl", "gemeinde", "gmdschl",
            "abwrecht", "lagebeztxt", "tntxt", "geom",
        )
        val mappedFields = DE.simplifiedAlkisKeys().mapNotNull { it.reference }.toSet()
        assertTrue(mappedFields.all { it in bremenFields }, "Unmapped Bremen schema fields: ${mappedFields - bremenFields}")
    }

    @Test
    fun saxonyInspireWfsConfigurationMatchesCapabilitiesAndOfficialSchema() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_SN.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(CrsRegistry()))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
            val options = service.optionBundle<ParcelServiceOptions>()

            assertEquals("/aaa/public_inspire/alkis/cp/dls/wfs", DE_SN.value.paths.keys.single())
            assertEquals("cp:CadastralParcel", wfs.configurationBundle.typeNames)
            assertEquals("http://inspire.ec.europa.eu/schemas/cp/4.0", wfs.configurationBundle.namespace)
            assertEquals("geometry", wfs.configurationBundle.geometryPointer)
            assertEquals("urn:ogc:def:crs:EPSG::25832", wfs.configurationBundle.outputCRS)
            assertEquals("urn:ogc:def:crs:EPSG::25833", wfs.configurationBundle.inputCRS)
            assertEquals(
                setOf("areaValue", "nationalCadastralReference"),
                options.keys.mapNotNull { it.reference }.toSet(),
            )
        }
    }

    @Test
    fun saarlandInspireWfsConfigurationMatchesCapabilitiesAndSchema() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_SL.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(CrsRegistry()))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
            val options = service.optionBundle<ParcelServiceOptions>()

            assertEquals("/gdi-sl/inspirewfs_Flurstuecke_Grundstuecke_ALKIS", DE_SL.value.paths.keys.single())
            assertEquals("cp:CadastralParcel", wfs.configurationBundle.typeNames)
            assertEquals("http://inspire.ec.europa.eu/schemas/cp/4.0", wfs.configurationBundle.namespace)
            assertEquals("geometry", wfs.configurationBundle.geometryPointer)
            assertEquals("urn:ogc:def:crs:EPSG::4326", wfs.configurationBundle.outputCRS)
            assertEquals(setOf("nationalCadastralReference"), options.keys.mapNotNull { it.reference }.toSet())
        }
    }

    @Test
    fun mecklenburgVorpommernWfsConfigurationMatchesCapabilitiesAndDescribeFeatureType() {
        HttpClient(MockEngine {
            respond("", headers = headersOf(HttpHeaders.ContentType, "application/gml+xml; version=3.2"))
        }).use { client ->
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(DE_MV.value) {
                    install(XmlAdapter)
                    install(WfsAdapterPlugin(CrsRegistry()))
                }.single { it.serviceLocator == APIs.Services.PARCEL_SERVICE.locator(ServiceMethods.GATHER) }
            val wfs = service.plugins.filterIsInstance<WfsAdapterPluginInstance>().single()
            val options = service.optionBundle<ParcelServiceOptions>()

            assertEquals("/dienste/alkis_wfs_einfach", DE_MV.value.paths.keys.single())
            assertEquals(DE.SIMPLIFIED_ALKIS_TYPE_NAME, wfs.configurationBundle.typeNames)
            assertEquals(DE.SIMPLIFIED_ALKIS_NAMESPACE, wfs.configurationBundle.namespace)
            assertEquals(DE.SIMPLIFIED_ALKIS_GEOMETRY, wfs.configurationBundle.geometryPointer)
            assertEquals("urn:ogc:def:crs:EPSG::25833", wfs.configurationBundle.outputCRS)
            assertEquals("urn:ogc:def:crs:EPSG::25833", wfs.configurationBundle.inputCRS)
            assertEquals("13", options.parcelIdPrefix)
        }

        val mvProviderFields = setOf(
            "idflurst", "flstkennz", "land", "landschl", "gemarkung", "gemaschl",
            "flur", "flurschl", "flstnrzae", "flstnrnen", "regbezirk", "regbezschl",
            "kreis", "kreisschl", "gemeinde", "gmdschl", "oid", "aktualit",
            "geometrie", "flaeche", "abwrecht", "lagebeztxt", "tntxt",
        )
        val mappedFields = DE.simplifiedAlkisKeys().mapNotNull { it.reference }.toSet()
        assertTrue(mappedFields.all { it in mvProviderFields }, "Unmapped M-V fields: ${mappedFields - mvProviderFields}")
    }

    @Test
    fun sharedAlkisFieldMappingsExistInBrandenburgDescribeFeatureType() {
        // Provider schema: Brandenburg WFS DescribeFeatureType, ave:Flurstueck.
        val providerFields = setOf(
            "idflurst", "flstkennz", "land", "landschl", "gemarkung", "gemaschl",
            "flur", "flurschl", "flstnrzae", "flstnrnen", "regbezirk", "regbezschl",
            "kreis", "kreisschl", "gemeinde", "gmdschl", "oid", "aktualit",
            "geometrie", "flaeche", "abwrecht", "lagebeztxt", "tntxt",
        )

        val configuredFields = DE.simplifiedAlkisKeys().mapNotNull { it.reference }.toSet()
        assertTrue(configuredFields.isNotEmpty())
        assertTrue(
            configuredFields.all { it in providerFields },
            "Configured ALKIS fields not present in the provider schema: ${configuredFields - providerFields}",
        )
        assertEquals("ave:Flurstueck", DE.SIMPLIFIED_ALKIS_TYPE_NAME)
        assertEquals(
            "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0",
            DE.SIMPLIFIED_ALKIS_NAMESPACE,
        )
        assertEquals("geometrie", DE.SIMPLIFIED_ALKIS_GEOMETRY)
    }

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
