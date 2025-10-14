package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelCrateDomain
import kotlinx.serialization.Serializable

@Serializable
data class ParcelCrateEntity(
    override var parcel: ParcelEntity,
    override var landMarks: List<LandmarkEntity> = emptyList(),
    override var tasks: List<TaskEntity> = emptyList(),
    override var notes: List<NoteEntity> = emptyList(),
    override var landUsages: List<LandUsageEntity> = emptyList(),
    override var properties: List<ParcelPropertyEntity> = emptyList(),
) : ParcelCrateDomain<ParcelEntity, LandmarkEntity, TaskEntity, NoteEntity, LandUsageEntity, ParcelPropertyEntity>