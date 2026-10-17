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

object DE_RP : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://geo5.service24.rlp.de") }
        info = OpenApiInfo(
            title = "ALKIS Rheinland-Pfalz (Open Data)",
            description = "Official simplified ALKIS parcel data from LVermGeo Rheinland-Pfalz.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Datenlizenz Deutschland Namensnennung 2.0",
                url = ParcelConstants.LC_DL_BY
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/wfs/alkis_rp.fcgi" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Rheinland-Pfalz",
                    get = Operation.build {
                        operationId = Bundesland.RHEINLAND_PFALZ.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.RHEINLAND_PFALZ.roughBoundaries,
                            correspondsTo = Bundesland.RHEINLAND_PFALZ.iso3166_2,
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
                            parcelLinkReference = "${Bundesland.RHEINLAND_PFALZ.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "©GeoBasis-DE / LVermGeoRP, dl-de/by-2-0",
                                url = ParcelConstants.LC_DL_BY
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
