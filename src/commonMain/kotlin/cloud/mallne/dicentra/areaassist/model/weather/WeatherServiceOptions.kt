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
    /** Maps common request parameter names to the upstream provider's documented names. */
    val requestParameterNames: Map<String, String> = emptyMap(),
    /** Provider-specific query defaults, applied only when the caller omitted the parameter. */
    val requestDefaults: Map<String, String> = emptyMap(),
    /** Common request keys unsupported by this provider operation, removed before dispatch. */
    val ignoredRequestParameters: Set<String> = emptySet(),
    @SerialName("x-dicentra-weaver-schema")
    val weaverSchema: WeaverSchema? = null,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = GenericElementWrapper(this, serializer())

    companion object {
        val BrightSky = WeatherServiceOptions(coverage = Germany.boundary)
        val BrightSkyCurrent = BrightSky.copy(weaverSchema = BrightSkyWeatherSchemas.current)
        val BrightSkyForecast = BrightSky.copy(weaverSchema = BrightSkyWeatherSchemas.forecast)
        val OpenMeteo = WeatherServiceOptions(
            coverage = null,
            requestParameterNames = mapOf("lat" to "latitude", "lon" to "longitude"),
            requestDefaults = mapOf(
                "current" to "temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,cloud_cover,precipitation,pressure_msl,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
                "hourly" to "temperature_2m,weather_code,apparent_temperature,dew_point_2m,relative_humidity_2m,cloud_cover,precipitation_probability,precipitation,pressure_msl,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,shortwave_radiation,sunshine_duration",
                "daily" to "temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_probability_max",
                "timezone" to "auto",
                "wind_speed_unit" to "ms",
            ),
            ignoredRequestParameters = setOf("date", "last_date", "max_dist", "tz", "units"),
            weaverSchema = OpenMeteoWeatherSchema.schema,
        )
    }
}
