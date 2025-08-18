package cloud.mallne.dicentra.areaassist.units

/**
 * Units to measure how much matter is in an object.
 */
open class Mass(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Mass) = ratio / other.ratio

    companion object {
        val kilograms = Mass("kg", databasePrimitive = "kilograms")
        val grams = Mass("g", 1.0 / 1000, "grams")

        enum class UnitStore(override val unit: Mass) : IUnitStore<Mass> {
            KILOGRAMS(unit = kilograms),
            GRAMS(unit = grams),
        }
    }
}

operator fun Length.times(mass: Mass) = mass * this
operator fun Measure<Length>.times(mass: Mass) = amount * (units * mass)