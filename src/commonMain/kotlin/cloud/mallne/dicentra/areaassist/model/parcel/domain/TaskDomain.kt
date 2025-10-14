package cloud.mallne.dicentra.areaassist.model.parcel.domain

import cloud.mallne.dicentra.areaassist.model.parcel.TaskState
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface TaskDomain {
    val title: String
    val description: String?

    @OptIn(ExperimentalTime::class)
    val dueDate: Instant?
    val taskState: TaskState
}