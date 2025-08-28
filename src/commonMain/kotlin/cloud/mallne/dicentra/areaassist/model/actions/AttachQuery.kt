package cloud.mallne.dicentra.areaassist.model.actions

import cloud.mallne.dicentra.areaassist.model.curator.Query
import kotlinx.serialization.Serializable

@Serializable
data class AttachQuery(
    val queries: List<Query>,
    val serviceHint: List<String>,
    val startImmediately: Boolean,
) : ServersideAction()
