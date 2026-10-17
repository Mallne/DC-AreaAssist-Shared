package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
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

object DE_SN : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25833"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://geodienste.sachsen.de") }
        info = OpenApiInfo(
            title = "INSPIRE Flurstücke Sachsen",
            description = "Official cadastral parcel data from GeoSN.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(name = "GeoSN, Datenlizenz Deutschland Namensnennung 2.0", url = LC_DL_BY)
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/aaa/public_inspire/alkis/cp/dls/wfs" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Sachsen",
                    get = Operation.build {
                        operationId = Bundesland.SACHSEN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "cp:CadastralParcel"
                            namespace = "http://inspire.ec.europa.eu/schemas/cp/4.0"
                            nsPrefix = "cp"
                            geometryPointer = "geometry"
                            inputCRS = DE_SN.crs
                            outputCRS = Identifier.constructUrn("EPSG", "25833")
                            importCRS = "EPSG:25833"
                            exportCRS = DE_SN.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.SACHSEN.roughBoundaries,
                            correspondsTo = Bundesland.SACHSEN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "areaValue",
                                parcelId = "nationalCadastralReference"
                            ),
                            parcelLinkReference = "${Bundesland.SACHSEN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "© Geodaten Sachsen / GeoSN, dl-de/by-2-0",
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
