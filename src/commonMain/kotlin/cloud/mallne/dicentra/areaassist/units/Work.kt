package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class Work(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Work) = ratio / other.ratio

    companion object {
        val joules = Work("J", databasePrimitive = "joules")
        val kilojoules = Work("kJ", 0.001, databasePrimitive = "kilojoules")
        val newtonMeters = Work("N m", databasePrimitive = "newtonMeters")
        val wattHours = Work("Wh", 1.0 / 3600.0, databasePrimitive = "wattHours")
        val kilowattHours = Work("kWh", 1.0 / 3.6e6, databasePrimitive = "kilowattHours")
        val megawattHours = Work("MWh", 1.0 / 3.6e9, databasePrimitive = "megawattHours")

        enum class UnitStore(override val unit: Work) :
            IUnitStore<Work> {
            JOULES(joules),
            KILOJOULES(kilojoules),
            NEWTONMETERS(newtonMeters),
            WATTHOURS(wattHours),
            KILOWATTHOURS(kilowattHours),
            MEGAWATTHOURS(megawattHours),
        }
    }
}