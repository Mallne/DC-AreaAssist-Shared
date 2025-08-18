package cloud.mallne.dicentra.areaassist.statics.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class KeyType {
    String,
    Number,
    Boolean,
    Nothing,
}