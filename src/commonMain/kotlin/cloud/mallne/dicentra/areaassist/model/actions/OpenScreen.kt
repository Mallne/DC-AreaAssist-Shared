package cloud.mallne.dicentra.areaassist.model.actions

import kotlinx.serialization.Serializable

@Serializable
data class OpenScreen(
    val link: String,
) : ServersideAction()
