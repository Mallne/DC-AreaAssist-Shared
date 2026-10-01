package cloud.mallne.dicentra.areaassist.model

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import io.ktor.http.Url
import io.ktor.openapi.GenericElementWrapper
import kotlinx.serialization.Serializable

@Serializable
data class ComputistLinkServiceOptions(val id: String) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = GenericElementWrapper(this, serializer())
}

fun validateComputistLinkUrl(url: String): String? {
    if (url != url.trim() || url.any(Char::isWhitespace)) return null
    if (!url.startsWith("https://", ignoreCase = true) && !url.startsWith("http://", ignoreCase = true)) return null
    return runCatching {
        Url(url).takeIf { (it.protocol.name == "http" || it.protocol.name == "https") && it.host.isNotBlank() }
            ?.toString()
    }.getOrNull()
}
