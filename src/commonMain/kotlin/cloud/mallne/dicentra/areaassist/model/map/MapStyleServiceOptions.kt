package cloud.mallne.dicentra.areaassist.model.map

import cloud.mallne.dicentra.areaassist.model.DisplayConstraints
import cloud.mallne.dicentra.areaassist.model.parcel.Translatable
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class MapStyleServiceOptions @OptIn(ExperimentalUuidApi::class) constructor(
    val serviceHint: String = Uuid.random().toString(),
    val name: Translatable = Translatable.Localization(mapOf(Translatable.Localization.ENGLISH to serviceHint)),
    val extraSources: List<MapSource> = listOf(),
    val constraints: DisplayConstraints = DisplayConstraints(),
    val mapFont: String,
    val backgroundColor: String = "#212121",
) : InflatedServiceOptions