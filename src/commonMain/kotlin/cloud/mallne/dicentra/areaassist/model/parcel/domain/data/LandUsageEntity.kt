package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.UsageOwnership
import cloud.mallne.dicentra.areaassist.model.parcel.UsageType
import cloud.mallne.dicentra.areaassist.model.parcel.UsageUse
import cloud.mallne.dicentra.areaassist.model.parcel.domain.LandUsageDomain
import kotlinx.serialization.Serializable

@Serializable
data class LandUsageEntity(
    override val area: Double,
    override val type: UsageType = UsageType.OTHER,
    override val ownership: UsageOwnership = UsageOwnership.PRIVATE,
    override val use: UsageUse = UsageUse.RESIDENTIAL,
) : LandUsageDomain