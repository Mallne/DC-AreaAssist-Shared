package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Sachsen.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Sachsen : StateDefinition {
    override val iso3166_2: String = "DE-SN"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(12.877999305725325, 51.6726989746096),
                    Position(13.01632022857666, 51.667060852050724),
                    Position(13.169989585876465, 51.598300933837834),
                    Position(13.421508789062727, 51.45396041870117),
                    Position(14.153639793396223, 51.54505157470703),
                    Position(14.690399169922102, 51.599472045898494),
                    Position(14.910359382629622, 51.493190765381144),
                    Position(14.963753700256461, 51.453544616699446),
                    Position(15.028479576111067, 51.30979919433622),
                    Position(15.03811264038086, 51.24029922485346),
                    Position(14.9301118850708, 50.99140548706055),
                    Position(14.805079460144043, 50.828918457031364),
                    Position(14.611928939819336, 50.85478210449219),
                    Position(14.560112953186035, 50.92348480224632),
                    Position(12.327589988708496, 50.17972946167015),
                    Position(12.282715797424544, 50.18267822265648),
                    Position(11.97589206695568, 50.35607910156256),
                    Position(11.893327713012809, 50.44530105590832),
                    Position(11.87872314453125, 50.545829772949446),
                    Position(12.261869430541992, 50.82387924194347),
                    Position(12.185659408569336, 51.18424224853521),
                    Position(12.158598899841309, 51.320590972900334),
                    Position(12.155740737915039, 51.464221954345646),
                    Position(12.199160575866927, 51.52273178100597),
                    Position(12.248349189758528, 51.566440582275504),
                    Position(12.877999305725325, 51.6726989746096),
                ),
            ),
        ),
    )
}
