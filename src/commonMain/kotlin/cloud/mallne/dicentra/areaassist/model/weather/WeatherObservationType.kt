package cloud.mallne.dicentra.areaassist.model.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WeatherObservationType {
    @SerialName("forecast")
    Forecast,

    @SerialName("synop")
    Synop,

    @SerialName("current")
    Current,

    @SerialName("historical")
    Historical,
}
