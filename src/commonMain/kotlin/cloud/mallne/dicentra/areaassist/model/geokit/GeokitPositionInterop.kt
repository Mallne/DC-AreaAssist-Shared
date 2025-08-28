package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.model.Point

object GeokitPositionInterop {
    fun GeokitPosition.toPoint(): Point<Double> {
        return Point(this.latitude, this.longitude)
    }
    fun Point<Double>.toGeokitPosition(): GeokitPosition =
        GeokitPosition(this.x, this.y)
}