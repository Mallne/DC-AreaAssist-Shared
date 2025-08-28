package cloud.mallne.dicentra.areaassist.model.actions

import kotlinx.serialization.Serializable

@Serializable
data class ServersideActionHolder(
    val id: String,
    val action: ServersideAction
)
