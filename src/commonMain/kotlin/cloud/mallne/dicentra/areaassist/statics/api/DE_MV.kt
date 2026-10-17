package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.areas.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceExtensions.parameter
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import io.ktor.openapi.*

object DE_MV : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25833"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://www.geodaten-mv.de") }
        info = OpenApiInfo(
            title = "ALKIS Mecklenburg-Vorpommern (Open Data)",
            description = "Official simplified ALKIS parcel data from LAiV Mecklenburg-Vorpommern.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Creative Commons Namensnennung 4.0 International (CC BY 4.0)",
                url = "https://creativecommons.org/licenses/by/4.0/"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/dienste/alkis_wfs_einfach" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Mecklenburg-Vorpommern",
                    get = Operation.build {
                        operationId = Bundesland.MECKLENBURG_VORPOMMERN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = DE.SIMPLIFIED_ALKIS_TYPE_NAME
                            this.namespace = DE.SIMPLIFIED_ALKIS_NAMESPACE
                            nsPrefix = DE.SIMPLIFIED_ALKIS_PREFIX
                            geometryPointer = DE.SIMPLIFIED_ALKIS_GEOMETRY
                            inputCRS = crs
                            outputCRS = Identifier.constructUrn("EPSG", "25833")
                            importCRS = "EPSG:25833"
                            exportCRS = crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            boundary = Bundesland.MECKLENBURG_VORPOMMERN.boundary,
                            correspondsTo = Bundesland.MECKLENBURG_VORPOMMERN.iso3166_2,
                            parcelIdPrefix = "13",
                            keys = DE.simplifiedAlkisKeys(),
                            parcelLinkReference = "${Bundesland.MECKLENBURG_VORPOMMERN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "© GeoBasis-DE/M-V, CC BY 4.0",
                                url = "https://creativecommons.org/licenses/by/4.0/"
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
