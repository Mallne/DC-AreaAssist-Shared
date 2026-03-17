package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class UsageOwnership(val value: String) {
    PRIVATE("private"),
    PUBLIC("public"),
    MIXED("mixed"),
    OTHER("other");
}