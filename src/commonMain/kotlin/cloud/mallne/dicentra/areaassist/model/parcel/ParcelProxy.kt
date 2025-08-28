package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
data class ParcelProxy(
    val parcelId: String,
    val serviceHint: String,
): ParcelCrate()
