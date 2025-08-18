package cloud.mallne.dicentra.areaassist.units

import cloud.mallne.dicentra.areaassist.units.Angle.Companion.degrees
import kotlin.math.PI

/**
 * Units to measure geometric angles.
 */
open class Angle(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Angle) = ratio / other.ratio

    companion object {

        /** 2π * radians is the circumference of a circle */
        val radians = Angle("rad", databasePrimitive = "radians")

        /** 360 * degrees is the circumference of a circle */
        val degrees = object : Angle("°", PI / 180, databasePrimitive = "degrees") {
            override val spaceBetweenMagnitude = false
        }

        fun sin(angle: Measure<Angle>) = kotlin.math.sin(angle `in` radians)
        fun cos(angle: Measure<Angle>) = kotlin.math.cos(angle `in` radians)
        fun tan(angle: Measure<Angle>) = kotlin.math.tan(angle `in` radians)
        fun asin(value: Double) = kotlin.math.asin(value) * radians
        fun acos(value: Double) = kotlin.math.acos(value) * radians
        fun atan(value: Double) = kotlin.math.atan(value) * radians
        fun atan2(value1: Double, value2: Double) =
            kotlin.math.atan2(value1, value2) * radians

        fun sinh(angle: Measure<Angle>) = kotlin.math.sinh(angle `in` radians)
        fun cosh(angle: Measure<Angle>) = kotlin.math.cosh(angle `in` radians)
        fun tanh(angle: Measure<Angle>) = kotlin.math.tanh(angle `in` radians)
        fun asinh(value: Double) = kotlin.math.asinh(value) * radians
        fun acosh(value: Double) = kotlin.math.acosh(value) * radians
        fun atanh(value: Double) = kotlin.math.atanh(value) * radians

        enum class UnitStore(override val unit: Angle) : IUnitStore<Angle> {
            RADIANS(unit = radians),
            DEGREES(unit = degrees);
        }
    }
}

/**
 * Returns a measure that is within [0°, 360°)
 */
fun Measure<Angle>.normalize(): Measure<Angle> {
    var result = (this `in` degrees) % 360

    if (result < 0) {
        result += 360
    }

    return result * degrees
}

/**
 * Returns a measure that is within [-180°, 180°)
 */
fun Measure<Angle>.normalizeGeo(): Measure<Angle> {
    var result = (this `in` degrees) % 360

    if (result > 180) {
        result -= 360
    } else if (result < -180) {
        result += 360
    }

    return result * degrees
}