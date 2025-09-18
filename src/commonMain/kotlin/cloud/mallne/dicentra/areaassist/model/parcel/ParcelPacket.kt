package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ParcelPacket(
    override val parcelId: String,
    val origin: String,
    val json: JsonElement,
    val properties: List<ParcelPacketProperty>
) : ParcelCrate
