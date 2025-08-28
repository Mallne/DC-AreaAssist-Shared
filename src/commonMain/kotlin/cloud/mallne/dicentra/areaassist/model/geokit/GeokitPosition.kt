package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.extensions.CalculationExtensions.toDegrees
import cloud.mallne.dicentra.areaassist.extensions.NumericalExtensions.round
import cloud.mallne.dicentra.areaassist.units.Angle
import cloud.mallne.dicentra.areaassist.units.Angle.Companion.degrees
import cloud.mallne.dicentra.areaassist.units.Measure
import cloud.mallne.dicentra.areaassist.units.times
import kotlinx.serialization.Serializable
import kotlin.math.atan2

@Serializable
data class GeokitPosition(
    val latitude: Double, //Y
    val longitude: Double, //X
) {

    override fun toString(): String {
        return "$latitude,$longitude"
    }

    fun directionalVectorTo(other: GeokitPosition): DoubleArray {
        return GeokitMeasurement.directionalVectorOf(this, other)
    }

    fun vectorTo(other: GeokitPosition): GeokitVector {
        return GeokitVector(this, other)
    }

    fun vectorFrom(other: GeokitPosition): GeokitVector {
        return GeokitVector(other, this)
    }

    // Point subtraction results in a direction vector (FloatArray)
    operator fun minus(other: GeokitPosition) = vectorFrom(other)
    operator fun plus(other: GeokitPosition) = vectorTo(other)
    operator fun plus(other: DoubleArray) = GeokitPosition(latitude + other[1], longitude + other[0])

    infix fun angleTo(other: GeokitPosition): Measure<Angle> {
        val vector = directionalVectorTo(other)
        val radToX = atan2(vector[1], vector[0])
        val degToX = radToX.toDegrees()
        return (180f - degToX) * degrees //Because we dont want the X Axis but the positive Y Axis is North.
    }

    infix fun distanceTo(other: GeokitPosition) = GeokitMeasurement.distance(this, other)
    infix fun intersection(shape: GeokitShape) = GeokitMeasurement.intersectionVector(this, shape)
    infix fun inside(shape: GeokitShape) = GeokitMeasurement.isPointInsidePolygon(this, shape)

    fun lookingAt(direction: Measure<Angle>, shape: GeokitShape) =
        GeokitMeasurement.intersectionVectorInDirection(this, direction, shape)

    fun asString(decimals: Int = Int.MAX_VALUE) =
        "${this.latitude.round(decimals)}, ${longitude.round(decimals)}"

    companion object Composable {

    }

}