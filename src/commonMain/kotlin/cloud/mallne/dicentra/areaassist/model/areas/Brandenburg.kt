package cloud.mallne.dicentra.areaassist.model.areas

import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Position

/** Generated boundary model for Brandenburg.
 * Source: deutschlandGeoJSON 4 ? low resolution ? 4090d4e. Fidelity: 20%.
 * Generalized outline; not an authoritative legal or cadastral boundary.
 * At fidelity below 100%, this is a conservative cover and may have false positives.
 */
internal object Brandenburg : StateDefinition {
    override val iso3166_2: String = "DE-BB"

    override val boundary: MultiPolygon = MultiPolygon(
        coordinates = listOf(
            listOf(
                listOf(
                    Position(14.240408897399902, 53.42845153808605),
                    Position(14.420248985290527, 53.32563018798834),
                    Position(14.448929786682129, 53.26163864135748),
                    Position(14.366089820861816, 53.07647705078131),
                    Position(14.155890464782715, 52.87955856323248),
                    Position(14.63906192779541, 52.58003234863304),
                    Position(14.741278648376465, 52.07339096069347),
                    Position(14.739829063415755, 51.60350036621105),
                    Position(14.665378570556868, 51.55078887939459),
                    Position(14.015210151672363, 51.37430191040062),
                    Position(13.779280662536621, 51.36178207397472),
                    Position(13.560149192810286, 51.369880676269815),
                    Position(13.286639213562012, 51.38579177856457),
                    Position(13.233359336853141, 51.39775848388672),
                    Position(13.192778587341536, 51.427951812744254),
                    Position(13.063730239868391, 51.64789962768549),
                    Position(12.549958229064941, 51.980232238769815),
                    Position(12.435828208923567, 52.01491165161133),
                    Position(12.221308708190918, 52.16529846191406),
                    Position(12.15108871459961, 52.521560668945256),
                    Position(12.239520072937239, 52.854591369628906),
                    Position(11.846659660339355, 52.90298080444353),
                    Position(11.347668647766113, 53.05216217041044),
                    Position(11.273380279541016, 53.101150512695426),
                    Position(11.635769844055403, 53.239810943603516),
                    Position(12.05480003356945, 53.366352081298885),
                    Position(12.234510421753157, 53.35324859619146),
                    Position(12.670860290527344, 53.25107192993181),
                    Position(13.789619445801009, 53.556289672851506),
                    Position(14.240408897399902, 53.42845153808605),
                ),
            ),
        ),
    )
}
