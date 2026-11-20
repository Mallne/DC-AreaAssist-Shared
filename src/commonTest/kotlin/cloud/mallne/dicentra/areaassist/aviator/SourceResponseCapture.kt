package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.aviator.core.execution.AviatorSourceResponse
import cloud.mallne.dicentra.aviator.core.execution.AviatorSourceResponseObserver
import cloud.mallne.dicentra.aviator.orchestration.model.BodyValue
import cloud.mallne.dicentra.aviator.orchestration.model.HttpResponseMetadata
import cloud.mallne.dicentra.aviator.orchestration.model.ResponseSnapshot
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Per-invocation capture of transport responses for tests. Create a separate instance for each
 * invocation and install it in that invocation's coroutine context.
 */
class SourceResponseCapture : AviatorSourceResponseObserver() {
    var responses: List<AviatorSourceResponse> = emptyList()
        private set

    override suspend fun onResponses(responses: List<AviatorSourceResponse>) {
        this.responses = responses.toList()
    }
}

internal fun AviatorSourceResponse.requireJsonBody(json: Json = Json): JsonElement {
    val type = ContentType.parse(contentType)
    require(
        type.contentType.equals("application", ignoreCase = true) &&
            (type.contentSubtype.equals("json", ignoreCase = true) ||
                type.contentSubtype.endsWith("+json", ignoreCase = true)),
    ) { "Source response is not JSON" }
    val charset = type.parameters.firstOrNull { it.name.equals("charset", ignoreCase = true) }?.value
    require(charset == null || charset.equals("utf-8", ignoreCase = true)) {
        "JSON source response charset must be UTF-8"
    }
    val content = requireNotNull(body) { "Source response body is unavailable" }
    return json.parseToJsonElement(content.decodeToString(throwOnInvalidSequence = true))
}

internal fun AviatorSourceResponse.toArazzoSnapshot(json: Json = Json): ResponseSnapshot {
    val content = body
    val sourceBody = when {
        content == null -> BodyValue.Unavailable
        content.isEmpty() -> BodyValue.Empty
        else -> try {
            BodyValue.JsonValue(requireJsonBody(json))
        } catch (exception: Exception) {
            if (exception is CancellationException) throw exception
            BodyValue.BytesValue(content.toList())
        }
    }
    return ResponseSnapshot(sourceBody, contentType, HttpResponseMetadata(status, headers.mapValues { it.value.toList() }))
}
