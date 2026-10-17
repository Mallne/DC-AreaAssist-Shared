package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Saarland.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Saarland : StateDefinition {
    override val iso3166_2: String = "DE-SL"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(7.351100845328801, 49.54639407035186),
                    Position(7.140482912304606, 49.67211369002556),
                    Position(6.371419906616211, 49.548011779785156),
                    Position(6.3557653822919065, 49.5298971553926),
                    Position(6.35482120513916, 49.46498489379899),
                    Position(6.573975551243166, 49.195955712242004),
                    Position(7.229125500317423, 49.07058411092474),
                    Position(7.362774372100944, 49.14517593383812),
                    Position(7.429557207015591, 49.3206753509662),
                    Position(7.351100845328801, 49.54639407035186),
                ),
            ),
        ),
    )
}
