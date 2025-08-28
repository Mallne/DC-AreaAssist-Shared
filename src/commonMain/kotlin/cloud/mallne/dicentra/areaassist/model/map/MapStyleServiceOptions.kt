package cloud.mallne.dicentra.areaassist.model.map

import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class MapStyleServiceOptions(
    val extraLayers: List<MapLayer> = listOf(),
    val extraSources: List<MapSource> = listOf(),
    val mode: MapMode,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)

    enum class MapMode {
        Dark,
        Light,
        Other
    }
}