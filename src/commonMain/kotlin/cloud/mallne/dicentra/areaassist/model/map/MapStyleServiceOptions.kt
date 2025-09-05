package cloud.mallne.dicentra.areaassist.model.map

import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class MapStyleServiceOptions @OptIn(ExperimentalUuidApi::class) constructor(
    val serviceHint: String = Uuid.random().toString(),
    val name: String = serviceHint,
    val extraSources: List<MapSource> = listOf(),
    val mode: MapMode,
    val backgroundColor: String = "#212121"
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)

    enum class MapMode {
        Dark,
        Light,
        Other
    }
}