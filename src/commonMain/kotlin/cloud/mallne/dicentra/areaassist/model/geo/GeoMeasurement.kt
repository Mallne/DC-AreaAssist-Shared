package cloud.mallne.dicentra.areaassist.model.geo

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class GeoMeasurement(
    val strike: Double,
    val dip: Double,
    val dipDirection: Double,
    val azimuth: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant = Clock.System.now(),
    val note: String = "",
)
