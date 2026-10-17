package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon

/** A named administrative area with a ray-castable, serializable boundary. */
interface AdministrativeArea {
    /** Stable identifier used to match area-specific service metadata. */
    val id: String

    val boundary: MultiPolygon
}
