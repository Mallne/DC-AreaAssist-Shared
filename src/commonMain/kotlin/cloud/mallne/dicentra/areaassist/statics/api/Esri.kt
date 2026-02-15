package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.DisplayConstraints
import cloud.mallne.dicentra.areaassist.model.SystemMode
import cloud.mallne.dicentra.areaassist.model.map.MapLayer
import cloud.mallne.dicentra.areaassist.model.map.MapSource
import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.Translatable
import cloud.mallne.dicentra.areaassist.statics.APIs.Services
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.servers.Server

object Esri : ApiObject {
    override val value: OpenAPI = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://services2.arcgis.com/jUpNdisbWqRpMo35/arcgis/rest/services"
            )
        ),
        info = Info(
            title = "Esri",
            description = "Location based Open Data for Germany",
            version = ParcelConstants.endpointVersion.toString()
        ),
        paths = mapOf(
            //"/thueringen_flstck/FeatureServer/0/query" to ParcelConstants.DE_TH,
            "/Flurstuecke_Sachsen/FeatureServer/0/query" to ParcelConstants.DE_SN,
            "/Flurstücke_Brandenburg/FeatureServer/0/query" to ParcelConstants.DE_BB,
            "/flstk_hessen/FeatureServer/0/query" to ParcelConstants.DE_HE,
            "/Flurstuecke_Hamburg/FeatureServer/0/query" to ParcelConstants.DE_HH,
            "/flstk_nrw/FeatureServer/0/query" to ParcelConstants.DE_NW,
            "/Flurstuecke_Sachsen_Anhalt/FeatureServer/0/query" to ParcelConstants.DE_ST,
            "/Flurst_Berlin/FeatureServer/0/query" to ParcelConstants.DE_BE,
            "/NDS_Flurstuecke/FeatureServer/0/query" to ParcelConstants.DE_NI,
        ),
        components = Components(
            parameters = Path.esriParams
        )
    )
}