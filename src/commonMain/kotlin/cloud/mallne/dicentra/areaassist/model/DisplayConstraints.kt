package cloud.mallne.dicentra.areaassist.model

import kotlinx.serialization.Serializable

@Serializable
data class DisplayConstraints(
    val linkedSystemModes: List<SystemMode> = listOf(),
)