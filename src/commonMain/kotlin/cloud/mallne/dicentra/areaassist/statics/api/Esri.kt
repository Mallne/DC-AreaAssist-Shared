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
        paths = emptyMap(),
    )
}
