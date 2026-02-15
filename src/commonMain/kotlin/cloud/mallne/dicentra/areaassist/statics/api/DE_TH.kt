package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.LC_DL_BY
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.io.MediaType
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.RequestBody
import cloud.mallne.dicentra.aviator.koas.servers.Server

object DE_TH : ApiObject {
    override val value: OpenAPI = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://www.geoproxy.geoportal-th.de/geoproxy/services"
            )
        ),
        info = Info(
            title = "Geoproxy Thüringen",
            description = "Location based Open Data for Thüringen",
            version = ParcelConstants.endpointVersion.toString(),
            license = License(
                name = "© GDI-Th",
                identifier = "Flurstücke Thüringen",
                url = LC_DL_BY
            )
        ),
        paths = mapOf(
            "/adv_alkis_wfs" to PathItem(
                summary = "Geoproxy Thüringen: Flurstücke",
                post = Operation(
                    operationId = Bundesland.THUERINGEN.iso3166_2,
                    requestBody = ReferenceOr.Value(
                        RequestBody(
                            content = mapOf(
                                "application/xml" to MediaType(schema = ReferenceOr.Value(Schema(type = Schema.Type.Basic.Object)))
                            )
                        )
                    ),
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                        AviatorExtensionSpec.PluginMaterialization.O.key to ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/1.0"
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                        },
                        AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
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
                    ),
                ),
            ),
        ),
        components = Components(
            parameters = Path.wfsParams
        )
    )
}