package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.areaassist.model.areas.Germany
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import io.ktor.openapi.GenericElementWrapper
import cloud.mallne.dicentra.weaver.core.specification.WeaverSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.maplibre.spatialk.geojson.MultiPolygon

/** Weather-specific discovery metadata. Request semantics belong to each OpenAPI operation. */
@Serializable
data class WeatherServiceOptions(
    /** Null or an empty geometry denotes global coverage. */
    val coverage: MultiPolygon? = null,
    val priority: Int = 0,
    @SerialName("x-dicentra-weaver-schema")
    val weaverSchema: WeaverSchema? = null,
    @SerialName("x-dicentra-weaver-request-body-schema")
    val requestBodySchema: WeaverSchema? = null,
    @SerialName("x-dicentra-weaver-request-parameters-schema")
    val requestParametersSchema: WeaverSchema? = null,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = GenericElementWrapper(this, serializer())

    companion object {
        val BrightSky = WeatherServiceOptions(coverage = Germany.boundary)
        val BrightSkyCurrent = BrightSky.copy(weaverSchema = BrightSkyWeatherSchemas.current)
        val BrightSkyForecast = BrightSky.copy(weaverSchema = BrightSkyWeatherSchemas.forecast)
        val OpenMeteo = WeatherServiceOptions(
            coverage = null,
            weaverSchema = OpenMeteoWeatherSchema.schema,
            requestParametersSchema = OpenMeteoWeatherSchema.requestParameters,
        )
    }
}
