package cloud.mallne.dicentra.areaassist.model.geokit

import cloud.mallne.dicentra.areaassist.model.Point

object GeokitPositionInterop {
    fun GeokitPosition.toPoint(): Point<Double> {
        return Point(this.longitude, this.latitude)
    }
    fun Point<Double>.toGeokitPosition(): GeokitPosition =
        GeokitPosition(this.y, this.x)
}