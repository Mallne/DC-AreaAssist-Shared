package cloud.mallne.dicentra.areaassist.model.map

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface MapLayer {
    val id: String
    val userToggle: Boolean
    val description: String?
    val visible: Boolean
    @Serializable
    data class RasterMapLayer(
        override val id: String,
        @SerialName("minzoom")
        val minZoom: Float = 0f,
        @SerialName("maxzoom")
        val maxZoom: Float = 24f,
        @SerialName("raster-opacity")
        val opacity: Float = 1f,
        override val visible: Boolean = true,
        val position: LayerPositioning? = null,
        override val userToggle: Boolean = true,
        override val description: String? = null,
    ) : MapLayer

    @Serializable
    data class LayerPositioning(
        val where: Where,
        val otherLayer: String,
    ) {
        enum class Where {
            Above, Below, Top, Bottom
        }
    }
}