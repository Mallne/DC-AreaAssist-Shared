package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class UsageUse(val value: String) {
    RESIDENTIAL("residential"),
    COMMERCIAL("commercial"),
    INDUSTRIAL("industrial"),
    MIXED("mixed"),
    OTHER("other")
}