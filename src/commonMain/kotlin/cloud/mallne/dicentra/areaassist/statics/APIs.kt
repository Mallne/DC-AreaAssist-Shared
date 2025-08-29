package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.model.map.MapLayer
import cloud.mallne.dicentra.areaassist.model.map.MapSource
import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelConstants
import cloud.mallne.dicentra.areaassist.model.screen.DeepLinks
import cloud.mallne.dicentra.areaassist.model.weather.WeatherConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.aviator.model.ServiceLocator

object APIs {
    val mapLight = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://sgx.geodatenzentrum.de"
            ),
        ),
        info = Info(
            title = "Basemap Light",
            description = "Official German Vector Map",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                "basemap.de / BKG | Datenquellen: © GeoBasis-DE",
                identifier = "Basemap Light"
            )
        ),
        paths = mapOf(
            "/gdz_basemapworld_vektor/styles/bm_web_wld_col.json" to PathItem(
                get = Operation(
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to Services.MAPLAYER.locator(
                            ServiceMethods.GATHER
                        ).usable(),
                        AviatorExtensionSpec.ServiceOptions.O.key to MapStyleServiceOptions(
                            mode = MapStyleServiceOptions.MapMode.Light,
                            extraSources = listOf(
                                MapSource.RasterMapSource(
                                    id = "bayern",
                                    tiles = listOf(
                                        "https://geoservices.bayern.de/od/wms/alkis/v1/parzellarkarte?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=by_alkis_parzellarkarte_umr_schwarz"
                                    ),
                                    tileSize = 512
                                )
                            ),
                            extraLayers = listOf(
                                MapLayer.RasterMapLayer(
                                    id = "l_bayern",
                                    source = "bayern",
                                    visible = false,
                                )
                            ),
                            serviceHint = "basemap_light_default",
                            name = "Basemap.world"
                        ).usable()
                    ),
                )
            )
        )
    )
    val mapDark = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://basemap.de"
            ),
        ),
        info = Info(
            title = "Basemap Dark",
            description = "Official German Vector Map",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                "basemap.de / BKG | Datenquellen: © GeoBasis-DE",
                identifier = "Basemap Dark"
            )
        ),
        paths = mapOf(
            "/data/produkte/web_vektor/styles/bm_web_drk.json" to PathItem(
                get = Operation(
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to Services.MAPLAYER.locator(
                            ServiceMethods.GATHER
                        ).usable(),
                        AviatorExtensionSpec.ServiceOptions.O.key to MapStyleServiceOptions(
                            mode = MapStyleServiceOptions.MapMode.Dark,
                            extraSources = listOf(
                                MapSource.RasterMapSource(
                                    id = "topplus",
                                    tiles = listOf(
                                        "https://sgx.geodatenzentrum.de/wms_topplus_open?bbox={bbox-epsg-3857}&service=WMS&version=1.1.0&request=GetMap&layers=web_light_grau&styles=&srs=EPSG:3857&width=256&height=256&format=image/png&transparent=true"
                                    ),
                                    tileSize = 256
                                ),
                                MapSource.RasterMapSource(
                                    id = "bayern",
                                    tiles = listOf(
                                        "https://geoservices.bayern.de/od/wms/alkis/v1/parzellarkarte?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=by_alkis_parzellarkarte_umr_gelb"
                                    ),
                                    tileSize = 512
                                )
                            ),
                            extraLayers = listOf(
                                MapLayer.RasterMapLayer(
                                    id = "l_topplus",
                                    source = "topplus",
                                    opacity = 0.5f,
                                    userToggle = false,
                                    position = MapLayer.LayerPositioning(
                                        MapLayer.LayerPositioning.Where.Below,
                                        "Hintergrund"
                                    )
                                ),
                                MapLayer.RasterMapLayer(
                                    id = "l_bayern",
                                    source = "bayern",
                                    visible = false,
                                )
                            ),
                            serviceHint = "basemap_dark_default",
                            name = "Basemap.de Dark + TopPlusOpen"
                        ).usable()
                    ),
                )
            )
        )
    )
    val esri = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://services2.arcgis.com/jUpNdisbWqRpMo35/arcgis/rest/services"
            )
        ),
        info = Info(
            title = "Esri",
            description = "Location based Open Data for Germany",
            version = AviatorExtensionSpec.SpecVersion
        ),
        paths = mapOf(
            "/thueringen_flstck/FeatureServer/0/query" to ParcelConstants.DE_TH,
            "/Flurstuecke_Sachsen/FeatureServer/0/query" to ParcelConstants.DE_SN,
            "/Flurstücke_Brandenburg/FeatureServer/0/query" to ParcelConstants.DE_BB,
            "/flstk_hessen/FeatureServer/0/query" to ParcelConstants.DE_HE,
            "/Flurstuecke_Hamburg/FeatureServer/0/query" to ParcelConstants.DE_HH,
            "/flstk_nrw/FeatureServer/0/query" to ParcelConstants.DE_NW,
            "/Flurstuecke_Sachsen_Anhalt/FeatureServer/0/query" to ParcelConstants.DE_ST,
            "/Flurst_Berlin/FeatureServer/0/query" to ParcelConstants.DE_BE,
            "/NDS_Flurstuecke/FeatureServer/0/query" to ParcelConstants.DE_NI,
        ),
        components = Components(
            parameters = ParcelConstants.Path.params
        )
    )
    val brightSky = OpenAPI(
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
            version = AviatorExtensionSpec.SpecVersion,
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

    object OAuth2 {
        const val CLIENT_ID = "client_id"
        const val REDIRECT_URI = "redirect_uri"
        const val STATE = "state"
        const val RESPONSE_TYPE = "response_type"
        const val CODE = "code"
        const val GRANT_TYPE = "grant_type"
        const val REFRESH_TOKEN = "refresh_token"

        val APP_REDIRECT_URI = DeepLinks.DCAA.login

        fun paramsForAuthorization(
            redirectUri: String = APP_REDIRECT_URI,
            state: String,
            responseType: String = "code",
            serviceOptions: AuthServiceOptions
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REDIRECT_URI to RequestParameter.Single(redirectUri),
                STATE to RequestParameter.Single(state),
                RESPONSE_TYPE to RequestParameter.Single(responseType),
            )
        )

        fun paramsForToken(
            code: String,
            redirectUri: String = APP_REDIRECT_URI,
            grantType: String = "authorization_code",
            serviceOptions: AuthServiceOptions
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REDIRECT_URI to RequestParameter.Single(redirectUri),
                CODE to RequestParameter.Single(code),
                GRANT_TYPE to RequestParameter.Single(grantType),
            )
        )

        fun paramsForRefreshToken(
            refreshToken: String,
            grantType: String = "refresh_token",
            serviceOptions: AuthServiceOptions
        ): RequestParameters = serviceOptions.asParameter(
            mapOf(
                REFRESH_TOKEN to RequestParameter.Single(refreshToken),
                GRANT_TYPE to RequestParameter.Single(grantType),
            )
        )
    }

    val apis = listOf(
        mapLight,
        mapDark,
        esri,
        brightSky,
    )

    enum class Services(
        private val serviceLocator: String,
    ) {
        DISCOVERY_SERVICE("DCAACodexDiscoveryBundle"),
        WEATHER_SERVICE_CURRENT("&.scribe.weatherService.current"),
        WEATHER_SERVICE_WARNING("&.scribe.weatherService.warning"),
        WEATHER_SERVICE_FORECAST("&.scribe.weatherService.forecast"),
        MAPLAYER("&.surveyor.map"),
        PARCEL_SERVICE("&.curator.parcelService"),
        AUTH_TOKEN("&.warden.token"),
        AUTH_AUTHORIZATION("&.warden.auth"),
        AUTH_ACCOUNT("&.warden.account");

        fun locator(flavour: ServiceMethods): ServiceLocator {
            return ServiceLocator(
                this.serviceLocator,
                flavour
            )
        }
    }
}