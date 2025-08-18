package cloud.mallne.dicentra.areaassist.units

/**
 * Units to measure time durations.
 */
open class Pressure(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Pressure) = ratio / other.ratio

    companion object {
        val pascal = Pressure("Pa", databasePrimitive = "pascal")
        val megapascal = Pressure("MPa", ratio = 1000000000.0, databasePrimitive = "megapascal")
        val gigapascal = Pressure("GPa", ratio = 1000000.0, databasePrimitive = "gigapascal")
        val kilopascal = Pressure("kPa", ratio = 1000.0, databasePrimitive = "kilopascal")
        val hectopascal = Pressure("hPa", ratio = 100.0, databasePrimitive = "hectopascal")
        val atmosphere = Pressure("atm", ratio = 101325.0, databasePrimitive = "atmosphere")
        val bar = Pressure("bar", ratio = 100000.0, databasePrimitive = "bar")

        enum class UnitStore(override val unit: Pressure) : IUnitStore<Pressure> {
            PASCAL(unit = pascal),
            MEGAPASCAL(unit = megapascal),
            GIGAPASCAL(unit = gigapascal),
            KILOPASCAL(unit = kilopascal),
            HECTOPASCAL(unit = hectopascal),
            ATMOSPHERE(unit = atmosphere),
            BAR(unit = bar),
        }
    }
}