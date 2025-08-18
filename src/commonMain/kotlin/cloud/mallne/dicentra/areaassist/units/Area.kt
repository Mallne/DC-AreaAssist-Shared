package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class Area(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Area) = ratio / other.ratio

    companion object {
        val squareMillimeters = Area("mm²", 0.0000010, "squareMillimeters")
        val squareCentimeters = Area("cm²", 0.000100, "squareCentimeters")
        val squareDecimeters = Area("dm²", 0.01000, "squareDecimeters")
        val squareMeters = Area("m²", databasePrimitive = "squareMeters")
        val squareDekameters = Area("dam²", 100.0, "squareDekameters")
        val are = Area("are", 1 * squareDekameters `in` squareMeters, "are")
        val squareHectometer = Area("hm²", 10000.0, "squareHectometer")
        val hectare = Area("ha", 1 * squareHectometer `in` squareMeters, "hectare")
        val squareKilometers = Area("km²", 1000000.0, "squareKilometers")

        enum class UnitStore(override val unit: Area) :
            IUnitStore<Area> {
            SQUAREMILLIMETERS(unit = squareMillimeters),
            SQUARECENTIMETERS(unit = squareCentimeters),
            SQUAREDECIMETERS(unit = squareDecimeters),
            SQUAREMETERS(unit = squareMeters),
            SQUAREDEKAMETERS(unit = squareDekameters),
            ARE(unit = are),
            SQUAREHECTOMETER(unit = squareHectometer),
            HECTARE(unit = hectare),
            SQUAREKILOMETERS(unit = squareKilometers),
        }
    }
}