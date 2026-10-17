package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Nordrhein-Westfalen.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object NordrheinWestfalen : StateDefinition {
    override val iso3166_2: String = "DE-NW"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(8.666278839111328, 52.52527999877924),
                    Position(9.051368713378906, 52.502090454101676),
                    Position(9.13135147094738, 52.484748840331974),
                    Position(9.07633018493675, 52.230110168456974),
                    Position(9.46591854095459, 51.855468750000114),
                    Position(9.451740264892578, 51.64538192749035),
                    Position(9.228690147400016, 51.459732055664006),
                    Position(8.957579612732161, 51.38727188110374),
                    Position(8.734568595886458, 51.10649108886713),
                    Position(8.471490859985352, 50.91152191162121),
                    Position(8.15311813354515, 50.69478988647455),
                    Position(8.052820205688477, 50.699131011963004),
                    Position(7.834569931030501, 50.881149291992415),
                    Position(7.660749912262077, 50.76705932617193),
                    Position(6.809259891510237, 50.34981155395508),
                    Position(6.70684909820568, 50.329780578613395),
                    Position(6.403338909149397, 50.32192993164068),
                    Position(6.339757919311523, 50.37989425659174),
                    Position(6.172194004058838, 50.550514221191406),
                    Position(5.963199138641357, 50.79505157470703),
                    Position(5.871619224548567, 51.05083084106451),
                    Position(6.07818603515625, 51.2447128295899),
                    Position(6.231968879699707, 51.36598205566412),
                    Position(5.964007854461897, 51.74161148071289),
                    Position(5.972117900848389, 51.831855773925724),
                    Position(6.107149124145508, 51.88898849487299),
                    Position(6.15888977050804, 51.905384063720646),
                    Position(6.402299880981445, 51.87480163574219),
                    Position(6.7684521675111, 52.12076568603527),
                    Position(6.995787143707275, 52.228771209716854),
                    Position(7.613338947296143, 52.47603988647472),
                    Position(7.693008899688948, 52.45629119873058),
                    Position(7.937768936157227, 52.368728637695426),
                    Position(7.988409996032715, 52.31166076660179),
                    Position(8.269809722900504, 52.13460922241222),
                    Position(8.29825973510765, 52.4587097167971),
                    Position(8.508218765259016, 52.51488876342796),
                    Position(8.666278839111328, 52.52527999877924),
                ),
            ),
        ),
    )
}
