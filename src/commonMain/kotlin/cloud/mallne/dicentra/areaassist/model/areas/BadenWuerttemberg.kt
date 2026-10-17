package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Baden-Württemberg.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object BadenWuerttemberg : StateDefinition {
    override val iso3166_2: String = "DE-BW"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(9.650460243225211, 49.7763404846192),
                    Position(9.795839309692383, 49.72497940063482),
                    Position(9.836868286132812, 49.69924926757807),
                    Position(10.129039764404297, 49.50531005859375),
                    Position(10.172438621521223, 49.39152908325195),
                    Position(10.162669181823958, 49.27928924560547),
                    Position(10.466980934143294, 48.9382896423341),
                    Position(10.505069732666243, 48.6903915405274),
                    Position(10.318519592285156, 48.529701232910156),
                    Position(10.178018569946289, 48.4662704467774),
                    Position(9.971670150756836, 48.38422012329113),
                    Position(10.0683403015139, 48.285560607910156),
                    Position(10.142750740051497, 48.10358047485374),
                    Position(10.133698463440169, 47.819438934326286),
                    Position(10.128538131714095, 47.67704010009777),
                    Position(10.073051452636719, 47.63850021362305),
                    Position(9.567465782165527, 47.5864715576173),
                    Position(8.891834259033544, 47.65522384643583),
                    Position(8.43740367889427, 47.57140731811529),
                    Position(7.669493198395003, 47.53711700439453),
                    Position(7.632383823395003, 47.5624237060548),
                    Position(7.607770919799805, 47.58095932006859),
                    Position(7.524673938751221, 47.66019439697271),
                    Position(7.5778594017029945, 48.121391296386776),
                    Position(7.805009365081901, 48.59319305419922),
                    Position(7.970248699188346, 48.756229400634766),
                    Position(8.197396278381348, 48.957103729248274),
                    Position(8.362239837646484, 49.09968948364275),
                    Position(8.418828964233398, 49.55218887329107),
                    Position(8.429039001464844, 49.58562088012707),
                    Position(8.600359916687239, 49.613231658935774),
                    Position(8.697778701782227, 49.62720108032238),
                    Position(8.729120254516602, 49.522029876708984),
                    Position(9.108948707580794, 49.583000183105696),
                    Position(9.307028770446777, 49.7379608154298),
                    Position(9.323519706726074, 49.76753997802729),
                    Position(9.510279655456543, 49.78639984130888),
                    Position(9.650460243225211, 49.7763404846192),
                ),
            ),
            listOf(
                listOf(
                    Position(8.708373069763297, 47.71555709838867),
                    Position(8.72245121002237, 47.69652938842779),
                    Position(8.661308288574162, 47.695213317871264),
                    Position(8.708373069763297, 47.71555709838867),
                ),
            ),
        ),
    )
}
