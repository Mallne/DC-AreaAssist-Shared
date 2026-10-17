package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

internal object Custom : StateDefinition {
    override val boundary: MultiPolygon = MultiPolygon(coordinates = emptyList<List<List<Position>>>())
    override val iso3166_2 = "CUSTOM"
}
