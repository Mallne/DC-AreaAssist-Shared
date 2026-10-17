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

object DE_ST : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25832"
    private const val endpoint = "/wss/service/ST_LVermGeo_ALKIS_WFS_OpenData/guest"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://www.geodatenportal.sachsen-anhalt.de") }
        info = OpenApiInfo(
            title = "WFS ST ALKIS Vereinfachtes Schema",
            description = "Official open-data cadastral parcel WFS from LVermGeo Sachsen-Anhalt.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(name = "Datenlizenz Deutschland Namensnennung 2.0", url = LC_DL_BY)
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            endpoint to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Sachsen-Anhalt",
                    get = Operation.build {
                        operationId = Bundesland.SACHSEN_ANHALT.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            this.namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                            inputCRS = DE_ST.crs
                            outputCRS = Identifier.constructUrn("EPSG", "25832")
                            importCRS = "EPSG:25832"
                            exportCRS = DE_ST.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.SACHSEN_ANHALT.roughBoundaries,
                            correspondsTo = Bundesland.SACHSEN_ANHALT.iso3166_2,
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
                            parcelLinkReference = "${Bundesland.SACHSEN_ANHALT.iso3166_2}_default",
                            license = OpenApiInfo.License(name = "LVermGeo Sachsen-Anhalt, dl-de/by-2-0", url = LC_DL_BY)
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
