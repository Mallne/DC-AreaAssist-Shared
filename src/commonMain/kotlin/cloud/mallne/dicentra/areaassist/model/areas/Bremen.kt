package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Bremen.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Bremen : StateDefinition {
    override val iso3166_2: String = "DE-HB"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(8.976511184360334, 53.15236502949618),
                    Position(8.492201302846752, 53.235040890440345),
                    Position(8.486890792846793, 53.22864151000982),
                    Position(8.546045455286958, 53.06883283558885),
                    Position(8.942650618705557, 53.00112892412193),
                    Position(9.004779576344504, 53.075996843715714),
                    Position(8.976511184360334, 53.15236502949618),
                ),
            ),
        ),
    )
}
