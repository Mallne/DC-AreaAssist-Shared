package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class Volume(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Volume) = ratio / other.ratio

    companion object {
        val liters = Volume("L", databasePrimitive = "liters")
        val milliliters = Volume("mL", 0.001, "milliliters")
        val centiliters = Volume("cL", 0.01, "centiliters")
        val deciliters = Volume("dL", 0.1, "deciliters")
        val dekaliters = Volume("daL", 10.0, "dekaliters")
        val hectoliters = Volume("hL", 100.0, "hectoliters")
        val kiloliters = Volume("kL", 1000.0, "kiloliters")
        val cubicMillimeters = Volume("mm³", 1e-6, "cubicMillimeters")
        val cubicCentimeters = Volume("cm³", 1e-3, "cubicCentimeters")
        val cubicDecimeters = Volume("dm³", databasePrimitive = "cubicDecimeters")
        val cubicMeters = Volume("m³", 1000.0, "cubicMeters")
        val cubicKilometers = Volume("km³", 1e9, "cubicKilometers")

        enum class UnitStore(override val unit: Volume) : IUnitStore<Volume> {
            LITERS(unit = liters),
            MILLILITERS(unit = milliliters),
            CENTILITERS(unit = centiliters),
            DECILITERS(unit = deciliters),
            DEKALITERS(unit = dekaliters),
            HECTOLITERS(unit = hectoliters),
            KILOLITERS(unit = kiloliters),
            CUBICMILLIMETERS(unit = cubicMillimeters),
            CUBICCENTIMETERS(unit = cubicCentimeters),
            CUBICDECIMETERS(unit = cubicDecimeters),
            CUBICMETERS(unit = cubicMeters),
            CUBICKILOMETERS(unit = cubicKilometers),
        }
    }
}