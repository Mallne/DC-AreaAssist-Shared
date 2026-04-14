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
import io.ktor.http.*
import io.ktor.openapi.*

object DE_TH : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server(
                "https://www.geoproxy.geoportal-th.de/geoproxy/services"
            )
        }
        info = OpenApiInfo(
            title = "Geoproxy Thüringen",
            description = "Location based Open Data for Thüringen",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "© GDI-Th",
                identifier = "Flurstücke Thüringen",
                url = LC_DL_BY
            )
        )
        components = Components(
            parameters = Path.wfsParams
        )
    }.copy(
        paths = mapOf(
            "/adv_alkis_wfs" to ReferenceOr.value(
                PathItem(
                    summary = "Geoproxy Thüringen: Flurstücke",
                    post = Operation.build {
                        operationId = Bundesland.THUERINGEN.iso3166_2
                        requestBody = RequestBody(
                            content = mapOf(
                                ContentType.Application.Xml to MediaType(
                                    schema = ReferenceOr.Value(
                                        JsonSchema(
                                            type = JsonType.OBJECT
                                        )
                                    )
                                )
                            )
                        )
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/1.0"
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.THUERINGEN.roughBoundaries,
                            correspondsTo = Bundesland.THUERINGEN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "flaeche",
                                parcelId = "flstkennz",
                                district = "gemarkung",
                                //districtId = "gemaschl",
                                districtCompartment = "flur",
                                plot = "flurstnr",
                                //districtCompartmentId = "flurschl",
                                districtMunicipalityId = "gmdschl",
                                districtRegion = "kreis",
                                districtMunicipality = "gemeinde",
                                //districtRegionId = "kreisschl",
                                //usageLegalDeviation = "abwrecht",
                                //plotNumerator = "flstnrzae",
                                //plotDenominator = "flstnrnen",
                                location = "lagebeztxt",
                                usageHint = "tntext"
                            ),
                            parcelLinkReference = Bundesland.THUERINGEN.iso3166_2 + "_default",
                        ).usable()
                    }
                ),
            ),
        )
    )
}