package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Hessen.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Hessen : StateDefinition {
    override val iso3166_2: String = "DE-HE"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(9.627979278564453, 51.637340545654354),
                    Position(9.689008712768555, 51.58312225341825),
                    Position(9.646209716797102, 51.42076873779308),
                    Position(9.9112300872805, 51.41975021362316),
                    Position(10.236989021301497, 51.188148498535156),
                    Position(10.22134017944336, 51.02507019042969),
                    Position(10.048980712890852, 50.67726135253929),
                    Position(10.086059570312727, 50.6323814392091),
                    Position(10.063029289245605, 50.557312011718864),
                    Position(10.040019035339355, 50.49333190917986),
                    Position(9.964568138122559, 50.425350189208984),
                    Position(9.519418716430891, 50.09251022338867),
                    Position(9.419237136841048, 50.08029174804699),
                    Position(9.23793029785179, 50.14928054809582),
                    Position(9.066729545593262, 49.99483871459961),
                    Position(9.14529037475586, 49.79685974121105),
                    Position(9.16348934173584, 49.74538040161133),
                    Position(9.133290290832633, 49.52040100097656),
                    Position(8.817639350891113, 49.404090881347884),
                    Position(8.557209014893033, 49.52381134033203),
                    Position(8.39341926574707, 49.617549896240234),
                    Position(8.187968254089355, 50.03245925903326),
                    Position(7.887711048126221, 49.97066879272484),
                    Position(7.785120010375977, 50.05311203002941),
                    Position(8.13190937042259, 50.791851043701286),
                    Position(8.275670051574707, 50.881240844726506),
                    Position(8.380510330200195, 50.85895156860357),
                    Position(8.574358940124739, 51.28638076782232),
                    Position(8.703760147094727, 51.374889373779354),
                    Position(8.912460327148665, 51.47972106933605),
                    Position(8.990538597106934, 51.50728988647472),
                    Position(9.486080169677734, 51.65705108642601),
                    Position(9.627979278564453, 51.637340545654354),
                ),
            ),
        ),
    )
}
