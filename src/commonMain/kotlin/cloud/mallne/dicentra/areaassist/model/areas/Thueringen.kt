package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Thüringen.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Thueringen : StateDefinition {
    override val iso3166_2: String = "DE-TH"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(10.771890640258789, 51.6449089050293),
                    Position(10.93950080871582, 51.60713958740246),
                    Position(11.010198593139648, 51.4282608032226),
                    Position(11.326239585876465, 51.41061019897472),
                    Position(11.397780418396223, 51.3845405578615),
                    Position(11.480478286743164, 51.29914855957031),
                    Position(11.49051952362072, 51.165691375732536),
                    Position(12.193419456481934, 50.98212051391607),
                    Position(12.297120094299316, 51.09484100341825),
                    Position(12.469669342041016, 51.0888786315918),
                    Position(12.516628265381087, 51.06608200073248),
                    Position(12.621550559997559, 51.001781463623274),
                    Position(12.666169166565055, 50.92364883422863),
                    Position(12.33045482635498, 50.673278808593864),
                    Position(12.281126976013411, 50.639709472656364),
                    Position(11.933539390563965, 50.43050003051758),
                    Position(11.868338584899902, 50.40420913696312),
                    Position(11.821249961853027, 50.392898559570426),
                    Position(11.522049903869856, 50.37353897094738),
                    Position(11.251668930053711, 50.26519012451172),
                    Position(10.72881031036377, 50.19984054565424),
                    Position(10.617910385132063, 50.221820831299),
                    Position(10.039758682251204, 50.51588058471674),
                    Position(9.873108863830566, 50.64184188842779),
                    Position(9.948389053344954, 50.9484214782716),
                    Position(10.12471866607666, 51.14057159423834),
                    Position(9.973508834838867, 51.28619003295904),
                    Position(9.924859046936035, 51.37184143066406),
                    Position(10.054610252380371, 51.4380989074707),
                    Position(10.37830924987793, 51.58518981933605),
                    Position(10.771890640258789, 51.6449089050293),
                ),
            ),
        ),
    )
}
