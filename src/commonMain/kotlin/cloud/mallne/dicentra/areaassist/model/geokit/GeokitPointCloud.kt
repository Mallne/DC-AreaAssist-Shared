package cloud.mallne.dicentra.areaassist.model.geokit

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GeokitPointCloud(
    val points: List<GeokitPosition>,
) : List<GeokitPosition> {
    @Transient
    val bounds: GeokitBounds = GeokitMeasurement.bbox(this)

    override val size: Int
        get() = points.size

    override fun isEmpty(): Boolean = points.isEmpty()
    override fun contains(element: GeokitPosition): Boolean = points.contains(element)
    override fun iterator(): Iterator<GeokitPosition> = points.iterator()
    override fun containsAll(elements: Collection<GeokitPosition>): Boolean = points.containsAll(elements)
    override fun get(index: Int): GeokitPosition = points[index]
    override fun indexOf(element: GeokitPosition): Int = points.indexOf(element)
    override fun lastIndexOf(element: GeokitPosition): Int = points.lastIndexOf(element)
    override fun listIterator(): ListIterator<GeokitPosition> = points.listIterator()
    override fun listIterator(index: Int): ListIterator<GeokitPosition> = points.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<GeokitPosition> = points.subList(fromIndex, toIndex)
}
