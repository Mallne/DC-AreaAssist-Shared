package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Berlin.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Berlin : StateDefinition {
    override val iso3166_2: String = "DE-BE"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(13.76840877532959, 52.437110900878906),
                    Position(13.530497550964355, 52.67502212524414),
                    Position(13.242410659790266, 52.67502212524413),
                    Position(13.093818664550781, 52.526430130004705),
                    Position(13.093818664550781, 52.41743850708012),
                    Position(13.172534942626953, 52.338722229003956),
                    Position(13.67002010345459, 52.33872222900397),
                    Position(13.76840877532959, 52.437110900878906),
                    Position(13.76840877532959, 52.437110900878906),
                ),
            ),
        ),
    )
}
