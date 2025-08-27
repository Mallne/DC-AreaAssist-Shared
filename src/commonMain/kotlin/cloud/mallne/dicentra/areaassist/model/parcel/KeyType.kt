package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class KeyType {
    String,
    Number,
    Boolean,
    Nothing,
}