package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.areas.Bundesland
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

object DE_SL : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://geoportal.saarland.de") }
        info = OpenApiInfo(
            title = "INSPIRE Flurstücke Saarland (Open Data)",
            description = "Official cadastral parcel service from LVGL Saarland.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Datenlizenz Deutschland Namensnennung 2.0",
                url = ParcelConstants.LC_DL_BY
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/gdi-sl/inspirewfs_Flurstuecke_Grundstuecke_ALKIS" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Saarland",
                    get = Operation.build {
                        operationId = Bundesland.SAARLAND.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "cp:CadastralParcel"
                            namespace = "http://inspire.ec.europa.eu/schemas/cp/4.0"
                            nsPrefix = "cp"
                            geometryPointer = "geometry"
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            boundary = Bundesland.SAARLAND.boundary,
                            correspondsTo = Bundesland.SAARLAND.iso3166_2,
                            parcelIdPrefix = "10",
                            keys = DefaultKeys.fillIn(parcelId = "nationalCadastralReference"),
                            parcelLinkReference = "${Bundesland.SAARLAND.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "©LVGL 2022, dl-de/by-2-0, www.lvgl.saarland.de",
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
