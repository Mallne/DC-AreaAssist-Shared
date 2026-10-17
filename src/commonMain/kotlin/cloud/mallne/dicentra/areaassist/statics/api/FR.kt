package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.areas.France
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceExtensions.parameter
import io.ktor.openapi.*

object FR : ApiObject {
    private const val namespace = "http://CADASTRALPARCELS.PARCELLAIRE_EXPRESS"
    private const val namespacePrefix = "CADASTRALPARCELS.PARCELLAIRE_EXPRESS"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://data.geopf.fr") }
        info = OpenApiInfo(
            title = "IGN Parcellaire Express (PCI) France",
            description = "Official cadastral parcel layer published by IGN on the Géoplateforme.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Licence Ouverte 2.0",
                url = "https://www.etalab.gouv.fr/licence-ouverte-open-licence/"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/wfs/ows" to ReferenceOr.value(
                PathItem(
                    summary = "Parcelles cadastrales France",
                    get = Operation.build {
                        operationId = "FR"
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "$namespacePrefix:parcelle"
                            this.namespace = FR.namespace
                            nsPrefix = FR.namespacePrefix
                            geometryPointer = "geom"
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            boundary = France.boundary,
                            correspondsTo = France.id,
                            keys = DefaultKeys.fillIn(
                                area = "contenance",
                                district = "nom_com",
                                districtCompartment = "section",
                                districtMunicipalityId = "code_insee",
                                parcelId = "idu",
                                plotNumerator = "numero"
                            ),
                            parcelLinkReference = "${France.id}_default",
                            license = OpenApiInfo.License(
                                name = "Licence Ouverte 2.0",
                                url = "https://www.etalab.gouv.fr/licence-ouverte-open-licence/"
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
