package cloud.mallne.dicentra.areaassist.units

/**
 * Units to measure time durations.
 */
open class Time(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: Time) = ratio / other.ratio

    companion object {
        val milliseconds = Time("ms", databasePrimitive = "milliseconds")
        val seconds = Time("s", 1000.0, "seconds")
        val minutes = Time("min", 60 * seconds.ratio, "minutes")
        val hours = Time("hr", 60 * minutes.ratio, "hours")
        val days = Time("d", 24 * hours.ratio, "days")

        enum class UnitStore(override val unit: Time) : IUnitStore<Time> {
            MILLISECONDS(unit = milliseconds),
            SECONDS(unit = seconds),
            MINUTES(unit = minutes),
            HOURS(unit = hours),
            DAYS(unit = days),
        }
    }
}