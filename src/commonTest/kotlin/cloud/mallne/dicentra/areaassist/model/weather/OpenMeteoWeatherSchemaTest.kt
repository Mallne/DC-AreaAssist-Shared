package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.weaver.core.Weaver
import cloud.mallne.dicentra.aviator.plugin.weaver.toOpenApiSchema
import cloud.mallne.dicentra.areaassist.statics.api.openMeteoProviderResponseSchema
import io.ktor.openapi.JsonType
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.ReferenceOr
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class OpenMeteoWeatherSchemaTest {
    @Test
    fun requestParameterRecipeProjectsLogicalInputsToProviderInputs() {
        val input = buildJsonObject {
            listOf(
                "latitude", "current", "hourly", "daily", "wind_speed_unit", "minutely_15",
                "temperature_unit", "precipitation_unit", "timeformat", "start_date", "end_date",
                "start_hour", "end_hour", "models", "cell_selection", "apikey",
            ).forEach {
                put(it, "")
            }
            listOf("elevation", "forecast_days", "past_days", "past_hours", "forecast_hours").forEach {
                put(it, 3)
            }
            put("lat", 52.52)
            put("lon", 13.41)
            put("timezone", "Europe/Berlin")
            put("temperature_unit", "fahrenheit")
            put("past_hours", 10)
            put("cell_selection", "nearest")
            put("date", "2026-10-19")
        }

        val transformed = Weaver().engine(OpenMeteoWeatherSchema.requestParameters).executeElement(input)

        assertEquals("52.52", transformed.jsonObject["latitude"]?.jsonPrimitive?.content)
        assertEquals("13.41", transformed.jsonObject["longitude"]?.jsonPrimitive?.content)
        assertEquals("Europe/Berlin", transformed.jsonObject["timezone"]?.jsonPrimitive?.content)
        assertEquals("fahrenheit", transformed.jsonObject["temperature_unit"]?.jsonPrimitive?.content)
        assertEquals("10.0", transformed.jsonObject["past_hours"]?.jsonPrimitive?.content)
        assertEquals("nearest", transformed.jsonObject["cell_selection"]?.jsonPrimitive?.content)
        assertEquals(null, transformed.jsonObject["date"])
    }

    @Test
    fun openApiResponseSchemaIsGeneratedFromTheWeaverTransformationRecipe() {
        val schema = OpenMeteoWeatherSchema.schema.toOpenApiSchema(openMeteoProviderResponseSchema)

        assertEquals(JsonType.OBJECT, schema.type)
        assertEquals(setOf("context", "current", "hourly", "daily", "alerts"), schema.properties?.keys)
        val current = schema.properties?.get("current") as? ReferenceOr.Value<JsonSchema>
        assertEquals(JsonType.OBJECT, current?.value?.type)
    }

    @Test
    fun transformsOpenMeteoResponseIntoTheCommonWeatherReport() {
        val input = buildJsonObject {
            put("latitude", 52.52)
            put("longitude", 13.41)
            put("timezone", "Europe/Berlin")
            put("current", buildJsonObject {
                put("time", "2026-10-19T12:00:00+02:00")
                put("temperature_2m", 18.5)
                put("weather_code", 2)
                put("apparent_temperature", 18.0)
                put("relative_humidity_2m", 60)
                put("cloud_cover", 40)
                put("pressure_msl", 1013.2)
                put("precipitation", 0.2)
                put("wind_speed_10m", 3.6)
                put("wind_direction_10m", 180)
                put("wind_gusts_10m", 5.4)
            })
            put("hourly", buildJsonObject {
                put("time", kotlinx.serialization.json.buildJsonArray {
                    add(kotlinx.serialization.json.JsonPrimitive("2026-10-19T12:00:00+02:00"))
                    add(kotlinx.serialization.json.JsonPrimitive("2026-10-19T13:00:00+02:00"))
                })
                put("temperature_2m", values(18.5, 19.0))
                put("weather_code", values(2, 3))
                put("apparent_temperature", values(18.0, 18.4))
                put("dew_point_2m", values(10.0, 10.2))
                put("relative_humidity_2m", values(60, 58))
                put("cloud_cover", values(40, 35))
                put("precipitation", values(0.2, 0.0))
                put("precipitation_probability", values(10, 5))
                put("pressure_msl", values(1013.2, 1012.8))
                put("visibility", values(20_000, 20_000))
                put("wind_speed_10m", values(3.6, 4.0))
                put("wind_direction_10m", values(180, 185))
                put("wind_gusts_10m", values(5.4, 6.0))
                put("shortwave_radiation", values(100, 120))
                put("sunshine_duration", values(900, 1_000))
            })
            put("daily", buildJsonObject {
                put("time", values("2026-10-19"))
                put("temperature_2m_min", values(12.0))
                put("temperature_2m_max", values(20.0))
                put("precipitation_sum", values(1.2))
                put("precipitation_probability_max", values(20))
            })
        }

        val transformed = Weaver().engine(OpenMeteoWeatherSchema.schema).executeElement(input)
        val report = Json.decodeFromJsonElement<WeatherReport>(transformed)

        assertEquals(52.52, report.context.latitude)
        assertEquals("Europe/Berlin", report.context.timezone)
        val current = assertIs<WeatherSection.Available<CurrentConditions>>(report.current).value
        assertEquals(Instant.parse("2026-10-19T10:00:00Z"), current.observedAt)
        assertEquals(18.5, current.measurements.temperatureCelsius)
        assertEquals(2, current.measurements.wmoWeatherCode)
        assertEquals(WeatherAmount(0.2, 60, WeatherAggregation.Sum), current.measurements.precipitationMillimeters)
        val hourly = assertIs<WeatherSection.Available<List<HourlyForecast>>>(report.hourly).value
        assertEquals(2, hourly.size)
        assertEquals(19.0, hourly.last().measurements.temperatureCelsius)
        assertEquals(3, hourly.last().measurements.wmoWeatherCode)
        assertEquals(WeatherAmount(4.0, null, WeatherAggregation.Instant), hourly.last().measurements.windSpeedMetersPerSecond)
        val daily = assertIs<WeatherSection.Available<List<DailyWeatherSummary>>>(report.daily).value
        assertEquals(DailyWeatherSummary("2026-10-19", 12.0, 20.0, 1.2, 20.0), daily.single())
        assertEquals(WeatherSection.Unsupported, report.alerts)
    }

    private fun values(vararg values: Any) = kotlinx.serialization.json.buildJsonArray {
        values.forEach { value ->
            add(
                when (value) {
                    is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                    else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                },
            )
        }
    }
}
