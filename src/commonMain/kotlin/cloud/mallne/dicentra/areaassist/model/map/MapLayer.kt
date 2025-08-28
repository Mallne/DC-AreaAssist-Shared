package cloud.mallne.dicentra.areaassist.model.map

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class MapLayer {
    @Serializable
    data class RasterMapLayer(
        val id: String,
        val source: String,
        @SerialName("minzoom")
        val minZoom: Int = 0,
        @SerialName("maxzoom")
        val maxZoom: Int = 24,
        @SerialName("raster-opacity")
        val opacity: Float = 1f,
        val visible: Boolean = true,
        val userToggle: Boolean = true,
        val position: LayerPositioning? = null,
    ) : MapLayer()

    @Serializable
    data class LayerPositioning(
        val where: Where,
        val otherLayer: String,
    ) {
        enum class Where {
            Above, Below
        }
    }
}