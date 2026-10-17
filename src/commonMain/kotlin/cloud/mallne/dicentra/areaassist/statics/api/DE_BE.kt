package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
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
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import io.ktor.openapi.*

object DE_BE : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25833"
    private const val namespace = "alkis_flurstuecke"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://gdi.berlin.de") }
        info = OpenApiInfo(
            title = "ALKIS Berlin Flurstücke",
            description = "Official open cadastral parcel WFS from the Berlin Senate Department.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Datenlizenz Deutschland Zero 2.0",
                url = "https://www.govdata.de/dl-de/zero-2-0"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/services/wfs/alkis_flurstuecke" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Berlin",
                    get = Operation.build {
                        operationId = Bundesland.BERLIN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "alkis_flurstuecke:flurstuecke"
                            this.namespace = DE_BE.namespace
                            nsPrefix = "alkis_flurstuecke"
                            geometryPointer = "geom"
                            inputCRS = DE_BE.crs
                            outputCRS = Identifier.constructUrn("EPSG", "25833")
                            importCRS = "EPSG:25833"
                            exportCRS = DE_BE.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.BERLIN.roughBoundaries,
                            correspondsTo = Bundesland.BERLIN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "afl",
                                parcelId = "fsko",
                                district = "namgmk",
                                districtId = "gmk",
                                districtCompartment = "fln",
                                districtMunicipality = "namgem",
                                districtMunicipalityId = "gdz",
                                plotNumerator = "zae",
                                plotDenominator = "nen"
                            ),
                            parcelLinkReference = "${Bundesland.BERLIN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "Datenlizenz Deutschland Zero 2.0",
                                url = "https://www.govdata.de/dl-de/zero-2-0"
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
