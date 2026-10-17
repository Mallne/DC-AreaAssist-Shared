package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Hamburg.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Hamburg : StateDefinition {
    override val iso3166_2: String = "DE-HH"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(10.32660799001792, 53.64347618265617),
                    Position(10.189120653405894, 53.74795637127176),
                    Position(9.754981309853065, 53.6381310224492),
                    Position(9.723658561706543, 53.56900024414069),
                    Position(9.784729596936586, 53.44941812773322),
                    Position(10.2272379639071, 53.359245150377006),
                    Position(10.331219673156738, 53.44622039794934),
                    Position(10.32660799001792, 53.64347618265617),
                ),
            ),
        ),
    )
}
