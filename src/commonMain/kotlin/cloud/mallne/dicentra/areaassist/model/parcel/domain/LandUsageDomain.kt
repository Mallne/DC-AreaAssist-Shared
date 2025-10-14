package cloud.mallne.dicentra.areaassist.model.parcel.domain

import cloud.mallne.dicentra.areaassist.model.parcel.UsageOwnership
import cloud.mallne.dicentra.areaassist.model.parcel.UsageType
import cloud.mallne.dicentra.areaassist.model.parcel.UsageUse

interface LandUsageDomain {
    val area: Double
    val type: UsageType
    val ownership: UsageOwnership
    val use: UsageUse
}