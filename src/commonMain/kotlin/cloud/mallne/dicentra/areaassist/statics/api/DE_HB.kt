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
import io.ktor.openapi.*

object DE_HB : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://geodienste.bremen.de") }
        info = OpenApiInfo(
            title = "ALKIS Bremen (Open Data)",
            description = "Official parcel data from Landesamt GeoInformation Bremen, CC BY 4.0.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Creative Commons Namensnennung 4.0 International (CC BY 4.0)",
                url = "https://creativecommons.org/licenses/by/4.0/"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/wfs_hduk2958loah3976niun" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Bremen",
                    get = Operation.build {
                        operationId = Bundesland.BREMEN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "app:flurstuecke"
                            namespace = "http://www.deegree.org/app"
                            nsPrefix = "app"
                            geometryPointer = "geom"
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.BREMEN.roughBoundaries,
                            correspondsTo = Bundesland.BREMEN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "flaeche",
                                parcelId = "flstkennz",
                                district = "gemarkung",
                                districtId = "gemaschl",
                                districtCompartment = "flur",
                                districtCompartmentId = "flurschl",
                                districtMunicipality = "gemeinde",
                                districtMunicipalityId = "gmdschl",
                                districtRegion = "kreis",
                                districtRegionId = "kreisschl",
                                plotNumerator = "flstnrzae",
                                plotDenominator = "flstnrnen",
                                location = "lagebeztxt",
                                usageHint = "tntxt",
                                usageLegalDeviation = "abwrecht"
                            ),
                            parcelLinkReference = "${Bundesland.BREMEN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "Landesamt GeoInformation Bremen, CC BY 4.0",
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
