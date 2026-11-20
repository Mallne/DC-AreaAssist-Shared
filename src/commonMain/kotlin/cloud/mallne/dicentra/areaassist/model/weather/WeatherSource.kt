package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.units.BetterAsMeasure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.Position
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Provenance and optional station/model details for a weather observation or forecast. */
@OptIn(ExperimentalTime::class)
@Serializable
data class WeatherSource(
    /** Stable source identity; unlike station IDs, this is required for associating measurements. */
    val id: Int,
    @SerialName("dwd_station_id")
    val dwdStationId: String? = null,
    @SerialName("wmo_station_id")
    val wmoStationId: String? = null,
    @SerialName("station_name")
    val stationName: String? = null,
    @SerialName("observation_type")
    val observationType: WeatherObservationType,
    /** Null for legacy payloads whose origin cannot be inferred reliably. */
    val provenance: WeatherSourceProvenance? = null,
    @SerialName("first_record")
    val firstRecord: Instant,
    @SerialName("last_record")
    val lastRecord: Instant,
    @SerialName("lat")
    val latitude: Double,
    @SerialName("lon")
    val longitude: Double,
    @SerialName("height")
    val elevation: Double,
    @property:BetterAsMeasure
    val distance: Double,
    val providerName: String? = null,
    val attribution: String? = null,
    val modelName: String? = null,
) {
    val position: Position
        get() = Position(longitude, latitude, elevation)
}

@Serializable
enum class WeatherSourceProvenance {
    Observation,
    Model,
    Forecast,
}
