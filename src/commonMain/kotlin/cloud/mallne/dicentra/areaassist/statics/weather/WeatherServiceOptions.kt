package cloud.mallne.dicentra.areaassist.statics.weather

import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class WeatherServiceOptions(
    val serviceType: ServiceType
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)

    companion object {
        @Serializable
        enum class ServiceType {
            BRIGHTSKY
        }
    }
}