package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.weather.WeatherConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.LC_DL_BY
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.servers.Server

object Brightsky : ApiObject {
    override val value: OpenAPI = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://api.brightsky.dev"
            ),
        ),
        info = Info(
            title = "Brightsky",
            description = "Open Source API for German Weather Service",
            version = ParcelConstants.endpointVersion.toString(),
            license = License(
                identifier = "Brightsky",
                name = "MIT License",
                url = "https://github.com/jdemaeyer/brightsky/?tab=MIT-1-ov-file#readme"
            )
        ),
        paths = mapOf(
            "/alerts" to WeatherConstants.WEATHER_ALERTS,
            "/current_weather" to WeatherConstants.CURRENT_WEATHER,
            "/radar" to WeatherConstants.WEATHER_RADAR,
            "/sources" to WeatherConstants.WEATHER_SOURCES,
            "/synop" to WeatherConstants.WEATHER_SYNOP,
            "/weather" to WeatherConstants.WEATHER,
        )
    )
}