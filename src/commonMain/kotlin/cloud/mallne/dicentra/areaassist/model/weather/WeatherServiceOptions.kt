package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import io.ktor.openapi.*
import kotlinx.serialization.Serializable

@Serializable
data class WeatherServiceOptions(
    val serviceType: ServiceType
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions {
        return GenericElementWrapper(this, serializer())
    }

    companion object {
        @Serializable
        enum class ServiceType {
            BRIGHTSKY
        }
    }
}