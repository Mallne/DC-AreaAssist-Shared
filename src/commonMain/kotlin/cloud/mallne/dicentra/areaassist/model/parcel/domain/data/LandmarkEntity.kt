package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.domain.LandmarkDomain
import cloud.mallne.geokit.Vertex
import kotlinx.serialization.Serializable

@Serializable
data class LandmarkEntity(override val point: Vertex) : LandmarkDomain