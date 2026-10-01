package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.ComputistLinkServiceOptions
import cloud.mallne.dicentra.areaassist.model.validateComputistLinkUrl
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.api.ApiObject
import cloud.mallne.dicentra.areaassist.statics.APIs.Services
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import io.ktor.openapi.*

object RVRComputistLink : ApiObject {
    private val options = ComputistLinkServiceOptions(
        id = "Tool.RVR_DE",
    )

    override val value: OpenApiDoc = create(
        id = options.id,
        title = "RVR",
        url = "https://rvr-deutschland.de/",
        description = "Framework agreement for the raw timber trade in Germany",
    )

    /** Builds a Codex-registerable Aviator service definition for an external Computist link. */
    fun create(
        id: String,
        title: String,
        url: String,
        description: String? = null,
    ): OpenApiDoc {
        require(id.isNotBlank()) { "Computist link id must not be blank" }
        requireNotNull(validateComputistLinkUrl(url)) {
            "Computist link URL must be a valid HTTP(S) URL"
        }
        return OpenApiDoc.build {
            `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
            servers { server(url) }
            info = OpenApiInfo(
                title = title,
                description = description.orEmpty(),
                version = ParcelConstants.endpointVersion.toString(),
            )
        }.copy(
            paths = mapOf(
                "/" to ReferenceOr.value(
                    PathItem(
                        get = Operation.build {
                            `x-dicentra-aviator-serviceDelegateCall` =
                                Services.COMPUTIST_LINK.locator(ServiceMethods.GATHER)
                            `x-dicentra-aviator-serviceOptions` = ComputistLinkServiceOptions(id).usable()
                            operationId = "ComputistLink_$id"
                            summary = title
                        },
                    ),
                ),
            ),
        )
    }
}
