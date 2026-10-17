package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Sachsen-Anhalt.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object SachsenAnhalt : StateDefinition {
    override val iso3166_2: String = "DE-ST"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(12.239520072937239, 52.854591369628906),
                    Position(12.26237869262718, 52.79122161865246),
                    Position(12.33942985534668, 52.47510147094738),
                    Position(12.301870346069336, 52.21995162963867),
                    Position(12.221308708190918, 52.16529846191406),
                    Position(12.972139358520621, 51.93397140502941),
                    Position(13.163080215454102, 51.87210083007807),
                    Position(13.198930740356445, 51.71620941162115),
                    Position(13.039240837097168, 51.63716125488281),
                    Position(12.248349189758528, 51.566440582275504),
                    Position(12.307728767395247, 51.03596878051769),
                    Position(12.234389305114746, 50.94504928588867),
                    Position(12.019789695739746, 50.97230148315441),
                    Position(11.471959114074707, 51.109951019287166),
                    Position(11.365938186645735, 51.22121047973644),
                    Position(11.326239585876465, 51.41061019897472),
                    Position(10.992289543151855, 51.4169807434082),
                    Position(10.96851825714134, 51.43177032470709),
                    Position(10.705129623413313, 51.643550872802734),
                    Position(10.577550888061637, 51.78271102905285),
                    Position(10.569430351257324, 52.01726150512718),
                    Position(10.691758155822754, 52.052768707275504),
                    Position(10.987579345703125, 52.337001800537394),
                    Position(10.795768737793196, 52.72050857543968),
                    Position(10.766200065612793, 52.851242065429915),
                    Position(10.992259979248274, 52.90634918212896),
                    Position(11.651300430297965, 53.0311393737793),
                    Position(12.239520072937239, 52.854591369628906),
                ),
            ),
        ),
    )
}
