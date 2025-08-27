package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.areaassist.units.Units
import kotlinx.serialization.Serializable

@Serializable
data class UnitFormat(
    val unitSystem: Units.Companion.SISystems,
    val ingestUnit: String,
    val defaultDisplayUnit: String = ingestUnit,
    val precision: Int = 2,
    val editable: Boolean = true,
)