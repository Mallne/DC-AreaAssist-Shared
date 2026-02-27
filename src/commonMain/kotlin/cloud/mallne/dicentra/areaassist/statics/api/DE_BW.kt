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
import cloud.mallne.dicentra.aviator.koas.servers.Server

object DE_BW : ApiObject {
    override val value: OpenAPI = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://owsproxy.lgl-bw.de/owsproxy/wfs"
            )
        ),
        info = Info(
            title = "LGL Baden-Württemberg",
            description = "Landesweiter, vollständiger Flurstückslayer ALKIS-Daten tagesaktuell",
            version = ParcelConstants.endpointVersion.toString(),
            license = License(
                name = "LGL-BW: Datenlizenz Deutschland - Namensnennung - Version 2.0, www.lgl-bw.de",
                identifier = "Flurstücke Baden Württemberg",
                url = LC_DL_BY
            )
        ),
        paths = mapOf(
            "/WFS_LGL-BW_ALKIS" to PathItem(
                get = Operation(
                    summary = "Flurstücke Baden Württemberg",
                    operationId = Bundesland.BADEN_WUERTTEMBERG.iso3166_2,
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                        AviatorExtensionSpec.PluginMaterialization.O.key to ParcelConstants.wfsAdapterConfig {
                            typeNames = "nora:v_al_flurstueck"
                            namespace = "http://nora-prod.lgl.bwl.de/nora"
                            nsPrefix = "nora"
                            geometryPointer = "geom"
                        },
                        AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                            bounds = Bundesland.BADEN_WUERTTEMBERG.roughBoundaries,
                            correspondsTo = Bundesland.BADEN_WUERTTEMBERG.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                parcelId = "flurstueckskennzeichen",
                                area = "amtliche_flaeche",
                                districtId = "gemarkung_id",
                                district = "gemarkung_name",
                                districtCompartmentId = "flurnummer",
                                plotNumerator = "zaehler",
                                plotDenominator = "nenner",
                                districtMunicipalityId = "gemeinde_id",
                                districtMunicipality = "gemeinde_name",
                            ),
                            parcelLinkReference = Bundesland.BADEN_WUERTTEMBERG.iso3166_2 + "_default",
                        ).usable()
                    ),
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                ),
            ),
        ),
        components = Components(
            parameters = Path.wfsParams
        )
    )
}