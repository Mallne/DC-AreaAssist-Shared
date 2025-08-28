package cloud.mallne.dicentra.areaassist.model.map

import cloud.mallne.dicentra.areaassist.model.geokit.GeokitBounds
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class MapSource() {
    @Serializable
    data class RasterMapSource(
        val id: String,
        val tiles: List<String>,
        val tileSize: Int = 512,
        val bounds: GeokitBounds? = null,
        val minZoom: Int = 0,
        val maxZoom: Int = 22,
        val scheme: SourceScheme = SourceScheme.XYZ,
        val attribution: String? = null,
        val volatile: Boolean = false,
    ) : MapSource()

    enum class SourceScheme {
        @SerialName("xyz")
        XYZ,

        @SerialName("tms")
        TMS
    }
}