package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.aviator.core.execution.AviatorSourceResponse
import cloud.mallne.dicentra.aviator.orchestration.model.BodyValue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFails
import kotlin.test.assertIs

class SourceResponseCaptureTest {
    @Test
    fun sourceJsonSupportsStructuredMediaTypesAndJsonNull() {
        val source = response("""{"station":"München"}""".encodeToByteArray(), "application/geo+json; charset=utf-8")
        assertEquals("München", source.requireJsonBody().jsonObject.getValue("station").jsonPrimitive.content)
        assertEquals(JsonNull, response("null".encodeToByteArray()).requireJsonBody())
        assertEquals(200, source.status)
        assertEquals(listOf("one", "two"), source.headers["x-provider"])
    }

    @Test
    fun sourceJsonRejectsMissingInvalidAndNonJsonResponses() {
        assertFailsWith<IllegalArgumentException> { response(null).requireJsonBody() }
        assertFailsWith<IllegalArgumentException> { response("{}".encodeToByteArray(), "text/plain").requireJsonBody() }
        assertFailsWith<IllegalArgumentException> { response("{}".encodeToByteArray(), "application/json; charset=iso-8859-1").requireJsonBody() }
        assertFailsWith<IllegalArgumentException> { response("{".encodeToByteArray()).requireJsonBody() }
        assertFails { response(byteArrayOf(0xc3.toByte(), 0x28)).requireJsonBody() }
        assertFailsWith<IllegalArgumentException> { response(byteArrayOf()).requireJsonBody() }
    }

    @Test
    fun arazzoSnapshotPreservesStatusHeadersAndBodyAvailability() {
        val json = response("""{"station":"München"}""".encodeToByteArray())
        val snapshot = json.toArazzoSnapshot()
        assertEquals(json.requireJsonBody(), assertIs<BodyValue.JsonValue>(snapshot.body).value)
        assertEquals(200, snapshot.http?.status)
        assertEquals(listOf("one", "two"), snapshot.http?.headers?.get("x-provider"))
        assertEquals(BodyValue.Unavailable, response(null).toArazzoSnapshot().body)
        assertEquals(BodyValue.Empty, response(byteArrayOf()).toArazzoSnapshot().body)
        val malformed = "{".encodeToByteArray()
        assertEquals(BodyValue.BytesValue(malformed.toList()), response(malformed).toArazzoSnapshot().body)
        val nonJson = "not json".encodeToByteArray()
        assertEquals(BodyValue.BytesValue(nonJson.toList()), response(nonJson, "text/plain").toArazzoSnapshot().body)
        val invalidUtf8 = byteArrayOf(0xc3.toByte(), 0x28)
        assertEquals(BodyValue.BytesValue(invalidUtf8.toList()), response(invalidUtf8).toArazzoSnapshot().body)
    }

    private fun response(body: ByteArray?, contentType: String = "application/json") =
        AviatorSourceResponse(200, mapOf("x-provider" to listOf("one", "two")), contentType, body)
}
