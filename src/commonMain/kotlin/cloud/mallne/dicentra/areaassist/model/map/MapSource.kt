package cloud.mallne.dicentra.areaassist.model.map

import cloud.mallne.geokit.Boundary
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface MapSource {
    val layers: List<MapLayer>
    @Serializable
    class RasterMapSource(
        val tiles: List<String>,
        val tileSize: Int = 512,
        val bounds: Boundary? = null,
        val minZoom: Int = 0,
        val maxZoom: Int = 22,
        val scheme: SourceScheme = SourceScheme.XYZ,
        val attribution: String? = null,
        override val layers: List<MapLayer>
    ) : MapSource

    enum class SourceScheme {
        @SerialName("xyz")
        XYZ,

        @SerialName("tms")
        TMS
    }
}