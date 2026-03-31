package cloud.mallne.dicentra.areaassist.model

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import kotlinx.serialization.Serializable

@Serializable
data class AuthServiceOptions(
    val clientId: String
) : InflatedServiceOptions {
    fun asParameter(map: Map<String, RequestParameter>): RequestParameters = RequestParameters(
        (map + (APIs.OAuth2.CLIENT_ID to RequestParameter.Single(clientId))).toMutableMap()
    )
}