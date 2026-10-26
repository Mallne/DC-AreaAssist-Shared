package cloud.mallne.dicentra.areaassist.model.weather

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.time.Instant

/** Resolve offset-free observation times using the report's declared timezone, never device time. */
object WeatherReportSerializer : KSerializer<WeatherReport> {
    override val descriptor = WeatherReportContent.serializer().descriptor

    override fun deserialize(decoder: Decoder): WeatherReport {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Weather reports require JSON")
        val report = input.decodeJsonElement().jsonObject
        val timezone = report["context"]?.jsonObject?.get("timezone")?.jsonPrimitive?.contentOrNull
        fun normalize(value: JsonElement, key: String): JsonElement {
            val item = value.jsonObject
            val text = item[key]?.jsonPrimitive?.content ?: return value
            val instant = try {
                Instant.parse(text)
            } catch (_: IllegalArgumentException) {
                val zone = timezone ?: throw SerializationException("Weather timestamp '$text' has no offset or timezone")
                LocalDateTime.parse(text).toInstant(TimeZone.of(zone))
            }
            return JsonObject(item + (key to JsonPrimitive(instant.toString())))
        }
        fun section(key: String, timestamp: String): Pair<String, JsonElement>? {
            val content = report[key]?.jsonObject ?: return null
            if (content["type"]?.jsonPrimitive?.content != "available") return null
            val value = content.getValue("value")
            val normalized = if (value is JsonArray) JsonArray(value.map { normalize(it, timestamp) }) else normalize(value, timestamp)
            return key to JsonObject(content + ("value" to normalized))
        }
        val content = input.json.decodeFromJsonElement<WeatherReportContent>(JsonObject(report + listOfNotNull(
            section("current", "observedAt"), section("hourly", "timestamp"),
        ).toMap()))
        return WeatherReport(content.context, content.current, content.hourly, content.daily, content.alerts)
    }

    override fun serialize(encoder: Encoder, value: WeatherReport) = encoder.encodeSerializableValue(
        WeatherReportContent.serializer(), WeatherReportContent(value.context, value.current, value.hourly, value.daily, value.alerts),
    )
}

@Serializable
private data class WeatherReportContent(
    val context: WeatherContext,
    val current: WeatherSection<CurrentConditions>,
    val hourly: WeatherSection<List<HourlyForecast>>,
    val daily: WeatherSection<List<DailyWeatherSummary>>,
    val alerts: WeatherSection<List<WeatherAlert>> = WeatherSection.Unsupported,
)
