package cloud.mallne.dicentra.areaassist.model.geokit

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class GeokitBounds(
    val southWest: GeokitPosition,
    val northEast: GeokitPosition,
) {
    val north
        get() = northEast.latitude
    val east
        get() = northEast.longitude
    val south
        get() = southWest.latitude
    val west
        get() = southWest.longitude

    val southEast: GeokitPosition
        get() {
            return GeokitPosition(south, east)
        }
    val northWest: GeokitPosition
        get() {
            return GeokitPosition(north, west)
        }

    val center: GeokitPosition
        get() = GeokitMeasurement.center(this)

    val spanLatitude: Double
        get() = abs(north - south)

    val spanLongitude: Double
        get() = abs(east - west)

    constructor(north: Double, east: Double, south: Double, west: Double) : this(
        southWest = GeokitPosition(south, west),
        northEast = GeokitPosition(north, east)
    )

    private fun containsLatitude(latitude: Double): Boolean {
        return latitude in south..north
    }

    private fun containsLongitude(longitude: Double): Boolean {
        return longitude in west..east
    }

    /**
     * Determines whether this LatLngBounds contains a point.
     */
    operator fun contains(latLng: GeokitPosition): Boolean {
        return (containsLatitude(latLng.latitude) && containsLongitude(latLng.longitude))
    }

    init {
        require(!(north.isNaN() || south.isNaN())) { "latitude must not be NaN" }
        require(!(east.isNaN() || west.isNaN())) { "longitude must not be NaN" }
        require(!(east.isInfinite() || west.isInfinite())) { "longitude must not be infinite" }
        require(
            !(north > 90 || north < -90 || south > 90 || south < -90)
        ) { "latitude must be between -90 and 90" }
        require(north >= south) { "latNorth cannot be less than latSouth" }
        require(east >= west) { "lonEast cannot be less than lonWest" }
    }
}
