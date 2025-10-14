package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.TaskState
import cloud.mallne.dicentra.areaassist.model.parcel.domain.TaskDomain
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class TaskEntity @OptIn(ExperimentalTime::class) constructor(
    override val title: String,
    override val description: String? = null,
    override val dueDate: Instant? = null,
    override val taskState: TaskState = TaskState.TODO
) : TaskDomain