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
    /** Stable string identity for the provider; absent in legacy service options. */
    val providerId: String? = null,
    /** Shared normalization contract version, distinct from the OpenAPI document version. */
    val contractVersion: String? = null,
    /** Null or an empty geometry denotes global coverage. */
    val coverage: MultiPolygon? = null,
    val priority: Int = 0,
    /** Marks the configured provider as the global default/fallback when no specialization applies. */
    val isGlobalDefault: Boolean = false,
    /** Origin of weather data returned by this configured operation; absent for legacy options. */
    val sourceProvenance: WeatherSourceProvenance? = null,
    @SerialName("x-dicentra-weaver-schema")
    val weaverSchema: WeaverSchema? = null,
    @SerialName("x-dicentra-weaver-request-body-schema")
    val requestBodySchema: WeaverSchema? = null,
    @SerialName("x-dicentra-weaver-request-parameters-schema")
    val requestParametersSchema: WeaverSchema? = null,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = GenericElementWrapper(this, serializer())

    companion object {
        private const val WEATHER_CONTRACT_VERSION = "areaassist-weather-report-v1"

        val BrightSky = WeatherServiceOptions(
            providerId = "brightsky",
            contractVersion = WEATHER_CONTRACT_VERSION,
            coverage = Germany.boundary,
        )
        val BrightSkyCurrent = BrightSky.copy(
            weaverSchema = BrightSkyWeatherSchemas.current,
            sourceProvenance = WeatherSourceProvenance.Observation,
        )
        val BrightSkyForecast = BrightSky.copy(
            weaverSchema = BrightSkyWeatherSchemas.forecast,
            sourceProvenance = WeatherSourceProvenance.Forecast,
        )
        val BrightSkyAlerts = BrightSky.copy(weaverSchema = BrightSkyWeatherSchemas.alertReport)
        val OpenMeteo = WeatherServiceOptions(
            providerId = "open-meteo",
            contractVersion = WEATHER_CONTRACT_VERSION,
            coverage = null,
            isGlobalDefault = true,
            weaverSchema = OpenMeteoWeatherSchema.schema,
            requestParametersSchema = OpenMeteoWeatherSchema.requestParameters,
            sourceProvenance = WeatherSourceProvenance.Model,
        )
    }
}
