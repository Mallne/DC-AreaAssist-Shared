package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.polyfill.Validation
import kotlinx.serialization.Serializable

@Serializable
data class KeyFormat(
    val chrono: ChronoFormat? = null,
    val unit: UnitFormat? = null,
) {

    init {
        //exaclty one or none Property has to be set
        require(Validation.Null.oneOrNone(chrono, unit)) {
            "Exactly one or none of chrono and unit must be set."
        }
    }
}