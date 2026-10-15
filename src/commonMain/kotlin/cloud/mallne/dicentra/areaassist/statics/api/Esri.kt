package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import io.ktor.openapi.*

object Esri : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server(
                "https://services2.arcgis.com/jUpNdisbWqRpMo35/arcgis/rest/services"
            )
        }
        info = OpenApiInfo(
            title = "Esri",
            description = "Location based Open Data for Germany",
            version = ParcelConstants.endpointVersion.toString()
        )
        components = Components(
            parameters = Path.esriParams
        )
    }.copy(
        paths = mapOf(
            //"/thueringen_flstck/FeatureServer/0/query" to ReferenceOr.value(ParcelConstants.DE_TH),
            "/Flurstuecke_Sachsen/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_SN),
            "/Flurstücke_Brandenburg/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_BB),
            "/flstk_hessen/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_HE),
            "/Flurstuecke_Hamburg/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_HH),
            "/flstk_nrw/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_NW),
            "/Flurstuecke_Sachsen_Anhalt/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_ST),
            "/Flurst_Berlin/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_BE),
            "/NDS_Flurstuecke/FeatureServer/1/query" to ReferenceOr.value(ParcelConstants.DE_NI),
        ),
    )
}
