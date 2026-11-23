package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.areaassist.statics.api.OpenMeteo
import cloud.mallne.dicentra.aviator.client.ktor.KtorAviatorServiceConverter
import cloud.mallne.dicentra.aviator.core.NoBody
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.plugin.adapter.json.JsonAdapter
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import cloud.mallne.dicentra.aviator.orchestration.model.BodyValue
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SourceResponsePipelineTest {
    @Test
    fun unsuccessfulProviderResponseRemainsAvailableWithoutJsonSubstitution() = runTest {
        val body = "provider unavailable"
        val client = HttpClient(MockEngine {
            respond(body, HttpStatusCode.ServiceUnavailable, headersOf(
                HttpHeaders.ContentType to listOf("text/plain"),
                "X-Provider" to listOf("first", "second"),
            ))
        })
        try {
            val service = KtorAviatorServiceConverter(client, serializers = Serialization.serializers())
                .build(OpenMeteo.value) {
                    install(JsonAdapter)
                    install(WeaverPlugin)
                }.single()
            val capture = SourceResponseCapture()
            withContext(capture) {
                service.requestContextful<JsonElement, NoBody>(
                    requestParams = RequestParameters(mutableMapOf(
                        "lat" to RequestParameter.Single(52.52),
                        "lon" to RequestParameter.Single(13.41),
                    )),
                )
            }
            val source = capture.responses.single()
            assertEquals(503, source.status)
            assertEquals(listOf("first", "second"), source.headers.entries.single {
                it.key.equals("X-Provider", ignoreCase = true)
            }.value)
            assertContentEquals(body.encodeToByteArray(), source.body)
            assertFailsWith<IllegalArgumentException> { source.requireJsonBody() }
            val snapshot = source.toArazzoSnapshot()
            assertEquals(503, snapshot.http?.status)
            assertEquals(BodyValue.BytesValue(body.encodeToByteArray().toList()), snapshot.body)
            assertEquals(listOf("first", "second"), snapshot.http?.headers?.entries?.single {
                it.key.equals("X-Provider", ignoreCase = true)
            }?.value)
            assertTrue(capture.responses.single() === source)
        } finally {
            client.close()
        }
    }
}
