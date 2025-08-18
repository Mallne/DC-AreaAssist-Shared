package cloud.mallne.dicentra.areaassist.units

/**
 * Units for length or distance.
 */
open class WoodVolume(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: WoodVolume) = ratio / other.ratio

    companion object {
        val festmeter = WoodVolume("fm", databasePrimitive = "festmeter")
        val raummeter = WoodVolume("rm", 1 / 1.4, "raummeter")
        val schuettraummeter = WoodVolume("srm", 1 / 2.3, "schuettraummeter")

        enum class UnitStore(override val unit: WoodVolume) : IUnitStore<WoodVolume> {
            FESTMETER(unit = festmeter),
            RAUMMETER(unit = raummeter),
            SCHUETTRAUMMETER(unit = schuettraummeter),
        }
    }
}