package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.magnitude
import kotlinx.serialization.Serializable

@Serializable
data class GeokitVector(
    val origin: GeokitPosition,
    val direction: DoubleArray
) {
    val destination: GeokitPosition
        get() = GeokitMeasurement.destination(origin, direction)

    constructor(origin: GeokitPosition, destination: GeokitPosition) : this(
        origin,
        origin.directionalVectorTo(destination)
    )

    init {
        require(direction.size == 2) { "Vector must be in two-dimensional space." }
    }

    infix fun dot(other: GeokitVector) =
        this.direction.foldIndexed(0.0) { i, acc, cur -> acc + cur * other.direction[i] }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other != null && this::class != other::class) return false

        other as GeokitVector

        if (origin != other.origin) return false
        if (!direction.contentEquals(other.direction)) return false

        return true
    }

    fun length() = GeokitMeasurement.distance(origin, destination)
    fun fastLength() = direction.magnitude()
    override fun hashCode(): Int {
        var result = origin.hashCode()
        result = 31 * result + direction.contentHashCode()
        return result
    }
}
