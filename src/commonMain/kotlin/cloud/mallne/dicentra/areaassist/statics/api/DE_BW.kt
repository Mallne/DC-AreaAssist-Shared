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
import io.ktor.openapi.*

object DE_BW : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server("https://owsproxy.lgl-bw.de/owsproxy/wfs")
        }
        info = OpenApiInfo(
            title = "LGL Baden-Württemberg",
            description = "Landesweiter, vollständiger Flurstückslayer ALKIS-Daten tagesaktuell",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "LGL-BW: Datenlizenz Deutschland - Namensnennung - Version 2.0, www.lgl-bw.de",
                identifier = "Flurstücke Baden Württemberg",
                url = LC_DL_BY
            )
        )
        components = Components(
            parameters = Path.wfsParams
        )
    }.copy(
        paths = mapOf(
            "/WFS_LGL-BW_ALKIS" to ReferenceOr.value(
                PathItem(
                get = Operation.build {
                    summary = "Flurstücke Baden Württemberg"
                    operationId = Bundesland.BADEN_WUERTTEMBERG.iso3166_2
                    `x-dicentra-aviator-serviceDelegateCall` = locator
                    `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                        typeNames = "nora:v_al_flurstueck"
                        namespace = "http://nora-prod.lgl.bwl.de/nora"
                        nsPrefix = "nora"
                        geometryPointer = "geom"
                    }
                    `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
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
                }
            ).copy(parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) })
            )
        )
    )
}