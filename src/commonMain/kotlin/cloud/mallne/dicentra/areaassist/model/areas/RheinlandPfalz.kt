package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Rheinland-Pfalz.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object RheinlandPfalz : StateDefinition {
    override val iso3166_2: String = "DE-RP"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(7.799629211425781, 50.943019866943416),
                    Position(7.854489803314436, 50.9263610839846),
                    Position(7.973968982696761, 50.845249176025504),
                    Position(8.121088981628418, 50.70222854614252),
                    Position(8.174539566040039, 50.592750549316634),
                    Position(8.138910293579215, 50.2740592956543),
                    Position(8.257689476013184, 50.02706909179699),
                    Position(8.346780776977766, 49.95928955078125),
                    Position(8.486090660095442, 49.76776885986328),
                    Position(8.508589744568098, 49.44147872924799),
                    Position(8.491369247436523, 49.30128860473627),
                    Position(8.362239837646484, 49.09968948364258),
                    Position(8.241430282592887, 48.9688415527346),
                    Position(7.671083927154541, 49.045970916748104),
                    Position(7.531178951263428, 49.097133636474666),
                    Position(7.353888988494987, 49.17060852050787),
                    Position(7.292399883270264, 49.24573135375988),
                    Position(7.217249870300293, 49.572399139404354),
                    Position(6.608709812164534, 49.52021026611328),
                    Position(6.378159046173323, 49.54800033569359),
                    Position(6.363647937774886, 49.57404708862305),
                    Position(6.0983700752260575, 50.0599098205567),
                    Position(6.170382022857666, 50.23625564575207),
                    Position(6.380168914795149, 50.38045120239269),
                    Position(7.7363801002504715, 50.91852188110346),
                    Position(7.799629211425781, 50.943019866943416),
                ),
            ),
        ),
    )
}
