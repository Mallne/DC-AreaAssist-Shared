package cloud.mallne.dicentra.areaassist.model.parcel

enum class TaskState(val value: String) {
    TODO(value = "todo"),
    DOING(value = "doing"),
    DONE(value = "done"),
    CANCELLED(value = "cancelled"),
    GENERIC__ALL(value = "all"),
}