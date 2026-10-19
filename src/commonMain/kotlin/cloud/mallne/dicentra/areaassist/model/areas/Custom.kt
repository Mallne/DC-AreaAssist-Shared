package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

object Custom : AdministrativeArea {
    override val boundary: MultiPolygon = MultiPolygon(coordinates = emptyList())
    override val id = "CUSTOM"
}
