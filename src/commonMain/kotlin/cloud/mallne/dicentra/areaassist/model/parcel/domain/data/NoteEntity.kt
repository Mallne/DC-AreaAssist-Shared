package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.domain.NoteDomain
import kotlinx.serialization.Serializable

@Serializable
data class NoteEntity(override val note: String) : NoteDomain