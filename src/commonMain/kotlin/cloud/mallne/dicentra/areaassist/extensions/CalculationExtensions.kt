package cloud.mallne.dicentra.areaassist.extensions

import cloud.mallne.dicentra.areaassist.model.Point
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object CalculationExtensions {
    const val PI = kotlin.math.PI
    const val TWO_PI = 2 * PI
    const val HALF_PI = PI / 2

    /**
     * Calculates the cartesian distance between two points.
     */
    fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)))
    }

    fun rayCast(point: Point<Double>, boundary: MultiPolygon): Boolean = boundary.coordinates.any { polygon ->
        val outerRing = polygon.firstOrNull() ?: return@any false
        rayCastRing(point, outerRing) && polygon.drop(1).none { rayCastRing(point, it) }
    }

    private fun rayCastRing(point: Point<Double>, ring: List<Position>): Boolean {
        if (ring.size < 3) return false
        val unwrapped = mutableListOf<Point<Double>>()
        for (position in ring) {
            var longitude = position.longitude
            val previous = unwrapped.lastOrNull()?.x
            if (previous != null) {
                while (longitude - previous > 180) longitude -= 360
                while (longitude - previous < -180) longitude += 360
            }
            unwrapped += Point(longitude, position.latitude)
        }
        val center = (unwrapped.minOf { it.x } + unwrapped.maxOf { it.x }) / 2
        var pointLongitude = point.x
        while (pointLongitude - center > 180) pointLongitude -= 360
        while (pointLongitude - center < -180) pointLongitude += 360
        val rayPoint = Point(pointLongitude, point.y)
        var inside = false
        for (index in unwrapped.indices) {
            val a = unwrapped[index]
            val b = unwrapped[(index + 1) % unwrapped.size]
            val cross = (rayPoint.y - a.y) * (b.x - a.x) - (rayPoint.x - a.x) * (b.y - a.y)
            if (kotlin.math.abs(cross) <= 1e-10 && rayPoint.x >= minOf(a.x, b.x) - 1e-10 && rayPoint.x <= maxOf(a.x, b.x) + 1e-10
                && rayPoint.y >= minOf(a.y, b.y) - 1e-10 && rayPoint.y <= maxOf(a.y, b.y) + 1e-10
            ) return true
            if ((a.y > rayPoint.y) != (b.y > rayPoint.y) &&
                rayPoint.x < (b.x - a.x) * (rayPoint.y - a.y) / (b.y - a.y) + a.x
            ) inside = !inside
        }
        return inside
    }

    fun vectorLength(rotationDeg: Double, width: Double, height: Double): Double {
        val radians = rotationDeg.toRadians()
        val halfWidth = width / 2.0
        val halfHeight = height / 2.0

        val x = halfWidth * cos(radians)
        val y = halfHeight * sin(radians)

        return sqrt(x * x + y * y)
    }

    fun scalingFactor(rotationDeg: Double, width: Double, height: Double): Double {
        val vectorLength = vectorLength(rotationDeg, width, height)
        return (vectorLength / width)
    }

    // Dot product for direction vectors
    infix fun DoubleArray.dot(other: DoubleArray) =
        foldIndexed(0.0) { i, acc, cur -> acc + cur * other[i] }

    // Magnitude squared for a direction vector
    fun DoubleArray.magnitudeSquared() =
        fold(0.0) { acc, cur -> acc + cur.pow(2) }

    fun DoubleArray.magnitude() = sqrt(magnitudeSquared())

    fun DoubleArray.normalize(): DoubleArray {
        val magnitude = magnitude()
        return if (magnitude > 0) {
            this.map {
                it / magnitude
            }.toDoubleArray()
        } else {
            this
        }
    }

    // Scalar multiplication for a direction vector
    operator fun Double.times(vector: DoubleArray) = vector.map { this * it }.toDoubleArray()
    operator fun DoubleArray.times(double: Double) = double * this

    /**
     * Calculates a number between two numbers at a specific increment.
     */
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    /**
     * Re-maps a number from one range to another.
     */
    fun map(value: Float, start1: Float, stop1: Float, start2: Float, stop2: Float): Float {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
    }

    /**
     * Converts the angle measured in radians to an approximately equivalent angle measured in degrees.
     */
    fun Float.toDegrees(): Float {
        val degrees = this % (2 * PI.toFloat())
        return degrees * 180 / PI.toFloat()
    }

    /**
     * Converts the angle measured in degrees to an approximately equivalent angle measured in radians.
     */
    fun Float.toRadians(): Float {
        val radians = this % 360
        return radians * PI.toFloat() / 180
    }

    /**
     * Converts the angle measured in radians to an approximately equivalent angle measured in degrees.
     */
    fun Double.toDegrees(): Double {
        val degrees = this % (2 * PI)
        return degrees * 180 / PI
    }

    /**
     * Converts the angle measured in degrees to an approximately equivalent angle measured in radians.
     */
    fun Double.toRadians(): Double {
        val radians = this % 360
        return radians * PI / 180
    }

    fun ease(p: Float): Float {
        return 3 * p * p - 2 * p * p * p
    }

    fun ease(p: Float, g: Float): Float {
        return if (p < 0.5f) {
            0.5f * (2 * p).pow(g)
        } else {
            1 - 0.5f * (2 * (1 - p)).pow(g)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun Double.ifNaNnn(
        defaultValue: () -> Double
    ): Double {
        contract {
            callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
        }
        return if (this.isNaN()) {
            defaultValue()
        } else {
            this
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun Double.ifNaNNull(
        defaultValue: () -> Double?
    ): Double? {
        contract {
            callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
        }
        return if (this.isNaN()) {
            defaultValue()
        } else {
            this
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun Double?.ifNaNNullable(
        defaultValue: () -> Double?
    ): Double? {
        contract {
            callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
        }
        return if (this?.isNaN() == true) {
            defaultValue()
        } else {
            this
        }
    }
}
