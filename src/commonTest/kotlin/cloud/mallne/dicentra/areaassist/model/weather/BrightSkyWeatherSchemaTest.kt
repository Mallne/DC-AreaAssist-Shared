package cloud.mallne.dicentra.areaassist.model.weather

import cloud.mallne.dicentra.weaver.core.Weaver
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BrightSkyWeatherSchemaTest {
    @Test
    fun currentWeatherPreservesAllBrightSkyMeasurementIntervals() {
        val response = Json.parseToJsonElement(
            """
            {
              "sources": [{"lat": 52.52, "lon": 13.405}],
              "weather": {
                "timestamp": "2026-11-18T10:00:00Z",
                "condition": "dry",
                "temperature": 8.5,
                "dew_point": 4.0,
                "relative_humidity": 72,
                "cloud_cover": 25,
                "pressure_msl": 1012.0,
                "visibility": 10000,
                "wind_direction_10": 190,
                "precipitation_10": 0.1,
                "precipitation_30": 0.3,
                "precipitation_60": 0.6,
                "wind_speed_10": 3.6,
                "wind_speed_30": 7.2,
                "wind_speed_60": 10.8,
                "wind_gust_speed_10": 7.2,
                "wind_gust_speed_30": 10.8,
                "wind_gust_speed_60": 14.4,
                "solar_10": 0.1,
                "solar_30": 0.2,
                "solar_60": 0.3,
                "sunshine_10": 1,
                "sunshine_30": 2,
                "sunshine_60": 3
              }
            }
            """.trimIndent(),
        )

        val output = Weaver().engine(BrightSkyWeatherSchemas.current).executeElement(response)
        val report = Json.decodeFromJsonElement<WeatherReport>(output)
        val measurements = (report.current as WeatherSection.Available).value.measurements

        fun assertIntervals(
            actual: WeatherIntervalAmounts?,
            expected: List<Double>,
            aggregation: WeatherAggregation,
        ) {
            val values = listOf(
                assertNotNull(actual).minute10,
                actual.minute30,
                actual.minute60,
            )
            values.forEachIndexed { index, amount ->
                val intervalAmount = assertNotNull(amount)
                assertEquals(expected[index], assertNotNull(intervalAmount.value), 0.00001)
                assertEquals(listOf(10, 30, 60)[index], intervalAmount.periodMinutes)
                assertEquals(aggregation, intervalAmount.aggregation)
            }
        }

        assertIntervals(measurements.precipitationByInterval, listOf(0.1, 0.3, 0.6), WeatherAggregation.Sum)
        assertIntervals(measurements.windSpeedByInterval, listOf(1.0, 2.0, 3.0), WeatherAggregation.Instant)
        assertIntervals(measurements.windGustByInterval, listOf(2.0, 3.0, 4.0), WeatherAggregation.Maximum)
        assertIntervals(measurements.shortwaveRadiationByInterval, listOf(100.0, 200.0, 300.0), WeatherAggregation.Mean)
        assertIntervals(measurements.sunshineByInterval, listOf(60.0, 120.0, 180.0), WeatherAggregation.Sum)
        assertEquals(WeatherAmount(0.6, 60, WeatherAggregation.Sum), measurements.precipitationMillimeters)
        assertEquals(1.0, assertNotNull(assertNotNull(measurements.windSpeedMetersPerSecond).value), 0.00001)
        assertEquals(WeatherAmount(2.0, 10, WeatherAggregation.Maximum), measurements.windGustMetersPerSecond)
        assertEquals(300.0, assertNotNull(assertNotNull(measurements.shortwaveRadiationWattsPerSquareMeter).value), 0.00001)
        assertEquals(180.0, assertNotNull(assertNotNull(measurements.sunshineSeconds).value), 0.00001)
    }
}
