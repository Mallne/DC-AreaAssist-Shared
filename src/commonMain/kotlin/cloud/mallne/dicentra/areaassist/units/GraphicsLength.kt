package cloud.mallne.dicentra.areaassist.units

/**
 * Units for measuring graphics display distances (i.e. distances on the surface of a display).
 */
open class GraphicsLength(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: GraphicsLength) = ratio / other.ratio

    companion object {
        /** 1 * pixels is the size of a single pixel on a display. */
        val pixels = GraphicsLength("px", databasePrimitive = "pixels")

        enum class UnitStore(override val unit: GraphicsLength) : IUnitStore<GraphicsLength> {
            PIXELS(unit = pixels),
        }
    }
}