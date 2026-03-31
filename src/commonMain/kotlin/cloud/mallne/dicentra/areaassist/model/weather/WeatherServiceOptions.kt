package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import kotlinx.serialization.Serializable

@Serializable
data class WeatherServiceOptions(
    val serviceType: ServiceType
) : InflatedServiceOptions {

    companion object {
        @Serializable
        enum class ServiceType {
            BRIGHTSKY
        }
    }
}