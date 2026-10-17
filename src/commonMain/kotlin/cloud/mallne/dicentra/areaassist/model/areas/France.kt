package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for France.
 * Source: Natural Earth 5.1.2 · 1:110m. Fidelity: 57%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
object France : AdministrativeArea {
    override val id: String = "FRA"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(-51.657797000000016, 4.156232),
                    Position(-52.55642499999999, 2.504705),
                    Position(-52.93965700000001, 2.124858),
                    Position(-53.41846499999997, 2.053389),
                    Position(-54.088063000000034, 2.105557),
                    Position(-54.52475400000003, 2.311849),
                    Position(-54.181726000000026, 3.18978),
                    Position(-54.399542, 4.212611),
                    Position(-54.478633, 4.896756),
                    Position(-53.95804499999997, 5.756548),
                    Position(-52.88214099999999, 5.409851),
                    Position(-51.82334300000002, 4.565768),
                    Position(-51.657797000000016, 4.156232),
                ),
            ),
            listOf(
                listOf(
                    Position(8.099279000000024, 49.017784),
                    Position(7.466759000000025, 47.620582),
                    Position(6.500099999999975, 46.429673),
                    Position(6.843593000000055, 45.991147),
                    Position(7.096651999999949, 45.333099),
                    Position(7.549596000000065, 44.127901),
                    Position(7.435185000000047, 43.693845),
                    Position(6.529244999999946, 43.128892),
                    Position(4.556962999999996, 43.399651),
                    Position(3.1004110000000082, 43.075201),
                    Position(2.9859989999999925, 42.473015),
                    Position(1.8267929999999524, 42.343385),
                    Position(0.33804699999996046, 42.579546),
                    Position(-1.5027709999999388, 43.034014),
                    Position(-1.901350999999977, 43.422802),
                    Position(-1.384225000000015, 44.02261),
                    Position(-1.1937980000000152, 46.014918),
                    Position(-2.9632759999999507, 47.570327),
                    Position(-4.491555000000062, 47.954954),
                    Position(-4.59235000000001, 48.68416),
                    Position(-3.295814000000064, 48.901692),
                    Position(-1.6165109999999459, 48.644421),
                    Position(-1.933493999999996, 49.776342),
                    Position(-0.9894689999999855, 49.347376),
                    Position(1.3387609999999768, 50.127173),
                    Position(1.6390010000000075, 50.946606),
                    Position(2.513572999999951, 51.148506),
                    Position(4.799221999999986, 49.985373),
                    Position(8.099279000000024, 49.017784),
                ),
            ),
            listOf(
                listOf(
                    Position(9.448553608930183, 42.999132915382624),
                    Position(8.993239234951716, 43.08352044659939),
                    Position(8.396073885365126, 41.98367869158273),
                    Position(9.050205555034381, 41.29436773876845),
                    Position(9.636193102378002, 41.57386946131715),
                    Position(9.448553608930183, 42.999132915382624),
                ),
            ),
        ),
    )
}
