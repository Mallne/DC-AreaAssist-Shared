package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ParcelPacketProperty(
    val key: String,
    val value: String,
    val type: DataTypeE,
) {

}
