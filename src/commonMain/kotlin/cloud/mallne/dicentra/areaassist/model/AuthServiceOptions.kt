package cloud.mallne.dicentra.areaassist.model

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class AuthServiceOptions(
    val clientId: String
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)
    fun asParameter(map: Map<String, RequestParameter>): RequestParameters = RequestParameters(
        (map + (APIs.OAuth2.CLIENT_ID to RequestParameter.Single(clientId))).toMutableMap()
    )
}