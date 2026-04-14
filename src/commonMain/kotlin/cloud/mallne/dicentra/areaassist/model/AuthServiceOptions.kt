package cloud.mallne.dicentra.areaassist.model

import cloud.mallne.dicentra.areaassist.statics.APIs
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import io.ktor.openapi.*
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
data class AuthServiceOptions(
    val clientId: String
) : InflatedServiceOptions {
    fun asParameter(map: Map<String, RequestParameter>): RequestParameters = RequestParameters(
        (map + (APIs.OAuth2.CLIENT_ID to RequestParameter.Single(clientId))).toMutableMap()
    )

    override fun usable(): ServiceOptions {
        return GenericElementWrapper(this, serializer())
    }
}