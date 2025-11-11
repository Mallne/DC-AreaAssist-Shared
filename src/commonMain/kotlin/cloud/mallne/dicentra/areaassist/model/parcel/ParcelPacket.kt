package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.geokit.geojson.JsonFeature
import kotlinx.serialization.Serializable

@Serializable
data class ParcelPacket(
    override val parcelId: String,
    val origin: String,
    val json: JsonFeature
) : ParcelCrate
