package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class Length(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Length) = ratio / other.ratio

    companion object {
        val miles = Length("mi", 1609.3440, "miles")
        val millimeters = Length("mm", 0.0010, "millimeters")
        val centimeters = Length("cm", 0.0100, "centimeters")
        val decimeters = Length("dm", 0.1000, "decimeters")
        val meters = Length("m", databasePrimitive = "meters")
        val dekameters = Length("dam", 10.0, "dekameters")
        val hectometer = Length("hm", 100.0, "hectometer")
        val inches = Length("in", 0.0254, "inches")
        val feet = Length("ft", 12 * inches `in` meters, "feet")
        val kilometers = Length("km", 1000.0000, "kilometers")

        fun Measure<Length>.autoConvertMetric(): Measure<Length> = autoConvert(
            meters, meters, listOf(
                kilometers,
                meters,
                centimeters,
                millimeters,
            )
        )

        enum class UnitStore(override val unit: Length) :
            IUnitStore<Length> {
            MILES(unit = miles),
            MILLIMETERS(unit = millimeters),
            CENTIMETERS(unit = centimeters),
            DECIMETERS(unit = decimeters),
            METERS(unit = meters),
            DEKAMETERS(unit = dekameters),
            HECTOMETER(unit = hectometer),
            INCHES(unit = inches),
            FEET(unit = feet),
            KILOMETERS(unit = kilometers),
        }
    }
}

/**
 * Sort Length before Time which is conventional.
 */
operator fun Time.times(other: Length) = other * this
operator fun Measure<Time>.times(other: Length) = amount * other * units

typealias Distance = Length