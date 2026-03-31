package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.weather.WeatherConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import io.ktor.openapi.*

object Brightsky : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server("https://api.brightsky.dev")
        }
        info = OpenApiInfo(
            title = "Brightsky",
            description = "Open Source API for German Weather Service",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                identifier = "Brightsky",
                name = "MIT License",
                url = "https://github.com/jdemaeyer/brightsky/?tab=MIT-1-ov-file#readme"
            )
        )
    }.copy(
        paths = mapOf(
            "/alerts" to ReferenceOr.value(WeatherConstants.WEATHER_ALERTS),
            "/current_weather" to ReferenceOr.value(WeatherConstants.CURRENT_WEATHER),
            "/radar" to ReferenceOr.value(WeatherConstants.WEATHER_RADAR),
            "/sources" to ReferenceOr.value(WeatherConstants.WEATHER_SOURCES),
            "/synop" to ReferenceOr.value(WeatherConstants.WEATHER_SYNOP),
            "/weather" to ReferenceOr.value(WeatherConstants.WEATHER),
        )
    )
}