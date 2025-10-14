package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelCrateDomain
import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelDomain
import kotlinx.serialization.Serializable

@Serializable
data class ParcelCrateEntity(
    override var parcel: ParcelDomain,
    override var landMarks: List<LandmarkEntity> = emptyList(),
    override var tasks: List<TaskEntity> = emptyList(),
    override var notes: List<NoteEntity> = emptyList(),
    override var landUsages: List<LandUsageEntity> = emptyList(),
    override var properties: List<ParcelPropertyEntity> = emptyList(),
) : ParcelCrateDomain<LandmarkEntity, TaskEntity, NoteEntity, LandUsageEntity, ParcelPropertyEntity>