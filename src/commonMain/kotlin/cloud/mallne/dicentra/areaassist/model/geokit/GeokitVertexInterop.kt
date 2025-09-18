package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.model.Point
import cloud.mallne.geokit.Vertex

object GeokitVertexInterop {
    fun Vertex.toPoint(): Point<Double> {
        return Point(this.longitude, this.latitude)
    }

    fun Point<Double>.toGeokitPosition(): Vertex =
        Vertex(this.y, this.x)
}