package cloud.mallne.dicentra.areaassist.model.actions

import cloud.mallne.dicentra.areaassist.model.parcel.ParcelCrate
import kotlinx.serialization.Serializable

@Serializable
data class ImportPackets(
    val parcels: List<ParcelCrate>,
) : ServersideAction()
