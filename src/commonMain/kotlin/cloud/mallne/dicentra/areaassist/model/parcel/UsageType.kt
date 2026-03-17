package cloud.mallne.dicentra.areaassist.model.parcel

import kotlinx.serialization.Serializable

@Serializable
enum class UsageType(val value: String) {
    AGRICULTURE("agriculture"),
    FOREST("forest"),
    PASTURELAND("pastureland"),
    PERENNIALCROPS("perennialcrops"),
    WATER("water"),
    OTHER("other")
}