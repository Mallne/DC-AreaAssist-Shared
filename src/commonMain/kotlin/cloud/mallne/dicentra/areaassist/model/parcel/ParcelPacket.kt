package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.geokit.geojson.Feature
import kotlinx.serialization.Serializable

@Serializable
data class ParcelPacket(
    override val parcelId: String,
    val origin: String,
    val json: Feature
) : ParcelCrate
