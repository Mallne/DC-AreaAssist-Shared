package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.areas.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.LC_DL_BY
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

object DE_BB : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25833"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://isk.geobasis-bb.de") }
        info = OpenApiInfo(
            title = "WFS BB ALKIS Vereinfacht",
            description = "Official simplified ALKIS parcel download service from LGB Brandenburg.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Datenlizenz Deutschland Namensnennung 2.0",
                url = LC_DL_BY
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/ows/alkis_vereinf_wfs" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Brandenburg",
                    get = Operation.build {
                        operationId = Bundesland.BRANDENBURG.iso3166_2
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
                            boundary = Bundesland.BRANDENBURG.boundary,
                            correspondsTo = Bundesland.BRANDENBURG.iso3166_2,
                            parcelIdPrefix = "12",
                            keys = DE.simplifiedAlkisKeys(),
                            parcelLinkReference = "${Bundesland.BRANDENBURG.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "© GeoBasis-DE/LGB, dl-de/by-2-0",
                                url = LC_DL_BY
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
