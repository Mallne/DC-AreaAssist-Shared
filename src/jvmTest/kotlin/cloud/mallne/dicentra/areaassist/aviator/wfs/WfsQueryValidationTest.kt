package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.model.curator.ParcelKeyProxy
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.areaassist.statics.api.DE_BW
import cloud.mallne.dicentra.areaassist.statics.api.DE_TH
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.geokit.coordinates.builtin.BuiltInCoordinateReferenceSystems
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WfsQueryValidationTest {
    @Test
    fun nonFiniteRangeFailsBeforeRequest() = runTest {
        var calls = 0
        val requestUrls = mutableListOf<String>()
        val requestMethods = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            calls++
            requestUrls += request.url.toString()
            requestMethods += request.method.value
            respond("""<wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"/>""",
                headers = headersOf(HttpHeaders.ContentType, "application/xml"))
        })
        try {
            val registry = CrsRegistry().apply { ingest(BuiltInCoordinateReferenceSystems.EPSG4326) }
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(DE_BW.value) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single()
            fun parameters(bounds: Pair<Double, Double>) = AreaAssistParameters.parameters(queries = listOf(
                Query(
                    field = ParcelKeyProxy(ParcelConstants.DefaultKeys.AREA.identifier),
                    content = QueryContentHolder.RANGE(bounds),
                ),
            ))

            for (range in listOf(Double.NaN to 3.0, 1.0 to Double.NaN,
                Double.NEGATIVE_INFINITY to 3.0, 1.0 to Double.POSITIVE_INFINITY)
            ) {
                assertFailsWith<IllegalArgumentException> {
                    service.requestContextful<List<ParcelCrateEntity>, NoBody>(requestParams = parameters(range))
                }
            }
            assertEquals(0, calls)
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(requestParams = parameters(1.0 to 3.0))
            assertEquals(1, calls)
            service.requestContextful<List<ParcelCrateEntity>, NoBody>(requestParams = parameters(0.0 to 0.0))
            assertEquals(2, calls)
            assertTrue(requestMethods.all { it == "GET" }, "$requestMethods $requestUrls")
            val filter = URLBuilder(requestUrls.last()).parameters["filter"]
            assertTrue(filter?.contains("PropertyIsBetween") == true, requestUrls.last())
            assertEquals(2, Regex(">0\\.0<").findAll(filter.orEmpty()).count(), requestUrls.last())
        } finally {
            client.close()
        }
    }

    @Test
    fun postOperationKeepsApiDefinedMethod() = runTest {
        var method: String? = null
        val client = HttpClient(MockEngine { request ->
            method = request.method.value
            respond(
                """<wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"/>""",
                headers = headersOf(HttpHeaders.ContentType, "application/xml")
            )
        })
        try {
            val registry = CrsRegistry().apply { ingest(BuiltInCoordinateReferenceSystems.EPSG4326) }
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers()).build(DE_TH.value) {
                install(XmlAdapter)
                install(WfsAdapterPlugin(registry))
            }.single()

            service.requestContextful<List<ParcelCrateEntity>, NoBody>()

            assertEquals("POST", method)
        } finally {
            client.close()
        }
    }
}
