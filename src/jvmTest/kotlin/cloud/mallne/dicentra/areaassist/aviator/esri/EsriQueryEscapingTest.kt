package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.geokit.Boundary
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.ParcelKeyProxy
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
import cloud.mallne.dicentra.areaassist.model.curator.QueryMethod
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.areaassist.statics.api.Esri
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.openapi.ReferenceOr
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EsriQueryEscapingTest {
    @Test
    fun envelopeUsesArcGisXminYminXmaxYmaxAndHonorsFalseReturnGeometry() = runTest {
        var geometry: String? = null
        var returnGeometry: String? = null
        val client = HttpClient(MockEngine { request ->
            geometry = request.url.parameters["geometry"]
            returnGeometry = request.url.parameters["returnGeometry"]
            respond("""{"type":"FeatureCollection","features":[]}""", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val api = Esri.value.copy(paths = mapOf("/parcels/query" to ReferenceOr.value(ParcelConstants.DE_TH)))
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(api) {
                    install(JsonAdapter)
                    install(EsriAdapterPlugin)
                }.single()
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                requestParams = AreaAssistParameters.parameters(
                    boundary = Boundary(north = 52.0, east = 13.0, south = 50.0, west = 11.0),
                    returnGeometry = false,
                ),
            )
            assertEquals("11.0,50.0,13.0,52.0", geometry)
            assertEquals("false", returnGeometry)
        } finally {
            client.close()
        }
    }

    @Test
    fun bundledEsriFieldReferencesRemainUsable() = runTest {
        var calls = 0
        val client = HttpClient(MockEngine {
            calls++
            respond("""{"type":"FeatureCollection","features":[]}""", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val items = listOf(
                ParcelConstants.DE_TH, ParcelConstants.DE_SN, ParcelConstants.DE_BB,
                ParcelConstants.DE_HE, ParcelConstants.DE_HH, ParcelConstants.DE_NW,
                ParcelConstants.DE_ST, ParcelConstants.DE_BE, ParcelConstants.DE_NI,
            )
            for (item in items) {
                val api = Esri.value.copy(paths = mapOf("/parcels/query" to ReferenceOr.value(item)))
                val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                    .build(api) {
                        install(JsonAdapter)
                        install(EsriAdapterPlugin)
                    }.single()
                service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                    requestParams = AreaAssistParameters.parameters(),
                )
            }
            assertEquals(items.size, calls)
        } finally {
            client.close()
        }
    }

    @Test
    fun untrustedFieldReferencesCannotChangeSqlOrOutFields() = runTest {
        val whereValues = mutableListOf<String?>()
        val client = HttpClient(MockEngine { request ->
            whereValues += request.url.parameters["where"]
            respond("""{"type":"FeatureCollection","features":[]}""", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val api = Esri.value.copy(paths = mapOf("/parcels/query" to ReferenceOr.value(ParcelConstants.DE_TH)))
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(api) {
                    install(JsonAdapter)
                    install(EsriAdapterPlugin)
                }.single()
            val options = service.optionBundle<ParcelServiceOptions>()
            fun withReference(reference: String, minimal: Boolean) = service.copy(options = options.copy(
                minimalDefinition = minimal,
                keys = options.keys.map { key ->
                    if (key.identifier == ParcelConstants.DefaultKeys.PARCELID.identifier) key.copy(reference = reference)
                    else key
                },
            ).usable())

            for (reference in listOf("id OR 1=1", "id;DROP", "id--comment", "id,other", "id..other", "1field")) {
                assertFailsWith<IllegalArgumentException> {
                    withReference(reference, minimal = false).requestContextful<List<ParcelCrateEntity>, NoBody>(
                        requestParams = AreaAssistParameters.parameters(queries = listOf(AreaAssistParameters.parcelIdQuery("x"))),
                    )
                }
                assertFailsWith<IllegalArgumentException> {
                    withReference(reference, minimal = true).requestContextful<List<ParcelCrateEntity>, NoBody>(
                        requestParams = AreaAssistParameters.parameters(),
                    )
                }
            }
            assertEquals(0, whereValues.size)

            withReference("Kataster.Flurstückskennzeichen", minimal = true)
                .requestContextful<List<ParcelCrateEntity>, NoBody>(
                    requestParams = AreaAssistParameters.parameters(queries = listOf(AreaAssistParameters.parcelIdQuery("x"))),
                )
            assertEquals(listOf(" Kataster.Flurstückskennzeichen = 'x'"), whereValues.map { requireNotNull(it) })
        } finally {
            client.close()
        }
    }

    @Test
    fun stringLiteralsAreEscapedInProviderRequest() = runTest {
        val whereValues = mutableListOf<String?>()
        val client = HttpClient(MockEngine { request ->
            whereValues += request.url.parameters["where"]
            respond("""{"type":"FeatureCollection","features":[]}""", HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val api = Esri.value.copy(paths = mapOf("/parcels/query" to ReferenceOr.value(ParcelConstants.DE_TH)))
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(api) {
                    install(JsonAdapter)
                    install(EsriAdapterPlugin)
                }.single()
            val inputs = listOf("plain", "O'Brien", "' OR 1=1 --", "a''b")
            for (input in inputs) {
                service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                    requestParams = AreaAssistParameters.parameters(queries = listOf(AreaAssistParameters.parcelIdQuery(input))),
                )
            }
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                requestParams = AreaAssistParameters.parameters(queries = listOf(
                    Query(field = ParcelKeyProxy("unknown"), content = QueryContentHolder.STRING("ignored")),
                    AreaAssistParameters.parcelIdQuery("O'Brien"),
                )),
            )
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                requestParams = AreaAssistParameters.parameters(queries = listOf(
                    AreaAssistParameters.parcelIdQuery("first"),
                    AreaAssistParameters.parcelIdQuery("O'Brien").copy(method = QueryMethod.OR),
                )),
            )
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                requestParams = AreaAssistParameters.parameters(queries = listOf(
                    Query(field = ParcelKeyProxy(ParcelConstants.DefaultKeys.AREA.identifier),
                        content = QueryContentHolder.RANGE(1.0 to 3.0)),
                )),
            )
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                requestParams = AreaAssistParameters.parameters(queries = listOf(
                    Query(field = ParcelKeyProxy(ParcelConstants.DefaultKeys.AREA.identifier),
                        content = QueryContentHolder.RANGE(0.0 to 0.0)),
                )),
            )
            assertEquals(
                listOf(" flstkennz = 'plain'", " flstkennz = 'O''Brien'",
                    " flstkennz = ''' OR 1=1 --'", " flstkennz = 'a''''b'", " flstkennz = 'O''Brien'",
                    " flstkennz = 'first'  OR flstkennz = 'O''Brien'", " flaeche BETWEEN 1.0 AND 3.0",
                    " flaeche BETWEEN 0.0 AND 0.0"),
                whereValues.map { requireNotNull(it) },
            )
            for (range in listOf(Double.NaN to 3.0, 1.0 to Double.POSITIVE_INFINITY)) {
                assertFailsWith<IllegalArgumentException> {
                    service.requestContextful<List<ParcelCrateEntity>, NoBody>(
                        requestParams = AreaAssistParameters.parameters(queries = listOf(
                            Query(field = ParcelKeyProxy(ParcelConstants.DefaultKeys.AREA.identifier),
                                content = QueryContentHolder.RANGE(range)),
                        )),
                    )
                }
            }
            assertEquals(8, whereValues.size, "Invalid ranges must not reach the provider")
        } finally {
            client.close()
        }
    }
}
