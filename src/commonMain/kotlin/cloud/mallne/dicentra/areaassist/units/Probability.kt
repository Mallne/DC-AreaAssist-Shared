package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class Probability(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Probability) = ratio / other.ratio

    companion object {
        val decimal = Probability("", databasePrimitive = "decimal")
        val percent = Probability("%", 0.01, "percent")
        val permille = Probability("‰", 0.001, "permille")
        val permyriad = Probability("‱", 0.0001, "permyriad")
        val percentmille = Probability("pcm", 0.00001, "percentmille")
        val permillion = Probability("ppm", 0.000001, "permillion")


        enum class UnitStore(override val unit: Probability) : IUnitStore<Probability> {
            DECIMAL(unit = decimal),
            PERCENT(unit = percent),
            PERMILLIE(unit = permille),
            PERMYRIAD(unit = permyriad),
            PERCENTMILLIE(unit = percentmille),
            PERMILLION(unit = permillion),
        }
    }
}