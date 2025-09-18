package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
data class ParcelProxy(
    override val parcelId: String,
    val serviceHint: String,
) : ParcelCrate
