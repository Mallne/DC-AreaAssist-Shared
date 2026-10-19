package cloud.mallne.dicentra.areaassist.model.areas

import cloud.mallne.dicentra.areaassist.model.areas.AdministrativeArea
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Germany.
 * Source: Natural Earth 5.1.2 · 1:110m. Fidelity: 52%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
object Germany : AdministrativeArea {
    override val id: String = "DEU"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(14.119686000000002, 53.757029),
                    Position(14.353315000000066, 53.248171),
                    Position(15.016996000000063, 51.106674),
                    Position(12.966836999999941, 50.484076),
                    Position(12.24011100000007, 50.266338),
                    Position(13.595946000000026, 48.877172),
                    Position(13.025850999999989, 47.637584),
                    Position(12.932627000000025, 47.467646),
                    Position(11.426414000000022, 47.523766),
                    Position(10.402084000000059, 47.302488),
                    Position(9.594225999999935, 47.525058),
                    Position(7.466759000000025, 47.620582),
                    Position(7.5936759999999595, 48.333019),
                    Position(8.099279000000024, 49.017784),
                    Position(6.658230000000003, 49.201958),
                    Position(6.186320000000023, 49.463803),
                    Position(6.043073000000049, 50.128052),
                    Position(5.988657999999987, 51.851616),
                    Position(6.589396999999963, 51.852029),
                    Position(7.092052999999964, 53.144043),
                    Position(6.90513999999996, 53.482162),
                    Position(7.100424999999973, 53.693932),
                    Position(8.800734000000034, 54.020786),
                    Position(8.572118000000046, 54.395646),
                    Position(8.526229000000058, 54.962744),
                    Position(9.921906000000035, 54.983104),
                    Position(9.939579999999978, 54.596642),
                    Position(10.95011199999999, 54.363607),
                    Position(10.939467000000036, 54.008693),
                    Position(12.518440000000055, 54.470371),
                    Position(13.647467000000006, 54.075511),
                    Position(14.119686000000002, 53.757029),
                ),
            ),
        ),
    )
}
