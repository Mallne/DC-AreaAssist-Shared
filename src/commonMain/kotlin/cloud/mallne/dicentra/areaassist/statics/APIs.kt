package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.model.AuthServiceOptions
import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.map.MapLayer
import cloud.mallne.dicentra.areaassist.model.map.MapSource
import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.screen.DeepLinks
import cloud.mallne.dicentra.areaassist.model.weather.WeatherConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.LC_DL_BY
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.dicentra.aviator.koas.Components
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.io.MediaType
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.RequestBody
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.aviator.model.SemVer
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
            version = ParcelConstants.endpointVersion.toString(),
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
                            backgroundColor = "#fafafa",
                            extraSources = listOf(
                                MapSource.RasterMapSource(
                                    tiles = listOf(
                                        "https://www.geoproxy.geoportal-th.de/geoproxy/services/DOP?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=th_dop"
                                    ),
                                    //bounds = Boundary(
                                    //    north = 51.66367013,
                                    //    east = 12.71188404,
                                    //    south = 50.15442687,
                                    //    west = 9.85484026,
                                    //),
                                    tileSize = 512,
                                    layers = listOf(
                                        MapLayer.RasterMapLayer(
                                            id = "l_dopTh",
                                            visible = false,
                                            description = "Orthofotos Thüringen",
                                            minZoom = 14.5f
                                        )
                                    )
                                ),
                                MapSource.RasterMapSource(
                                    tiles = listOf(
                                        "https://geoservices.bayern.de/od/wms/alkis/v1/parzellarkarte?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=by_alkis_parzellarkarte_umr_schwarz"
                                    ),
                                    tileSize = 512,
                                    layers = listOf(
                                        MapLayer.RasterMapLayer(
                                            id = "l_bayern",
                                            visible = false,
                                            description = "Passive Flurstücke Bayern",
                                            minZoom = 14.5f
                                        )
                                    )
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
            version = ParcelConstants.endpointVersion.toString(),
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
                                    tiles = listOf(
                                        "https://www.geoproxy.geoportal-th.de/geoproxy/services/DOP?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=th_dop"
                                    ),
//                                    bounds = Boundary(
//                                        north = 51.66367013,
//                                        east = 12.71188404,
//                                        south = 50.15442687,
//                                        west = 9.85484026,
//                                    ),
                                    tileSize = 512,
                                    layers = listOf(
                                        MapLayer.RasterMapLayer(
                                            id = "l_dopTh",
                                            visible = false,
                                            description = "Orthofotos Thüringen",
                                            minZoom = 14.5f
                                        )
                                    )
                                ),
                                MapSource.RasterMapSource(
                                    tiles = listOf(
                                        "https://sgx.geodatenzentrum.de/wms_topplus_open?bbox={bbox-epsg-3857}&service=WMS&version=1.1.0&request=GetMap&layers=web_light_grau&styles=&srs=EPSG:3857&width=256&height=256&format=image/png&transparent=true"
                                    ),
                                    tileSize = 256,
                                    layers = listOf(
                                        MapLayer.RasterMapLayer(
                                            id = "l_topplus",
                                            opacity = 0.5f,
                                            userToggle = false,
                                            position = MapLayer.LayerPositioning(
                                                MapLayer.LayerPositioning.Where.Below,
                                                "Hintergrund"
                                            )
                                        ),
                                    )
                                ),
                                MapSource.RasterMapSource(
                                    tiles = listOf(
                                        "https://geoservices.bayern.de/od/wms/alkis/v1/parzellarkarte?bbox={bbox-epsg-3857}&service=WMS&request=GetMap&styles=&srs=EPSG:3857&width=512&height=512&format=image/png&transparent=true&version=1.1.1&layers=by_alkis_parzellarkarte_umr_gelb"
                                    ),
                                    tileSize = 512,
                                    layers = listOf(
                                        MapLayer.RasterMapLayer(
                                            id = "l_bayern",
                                            visible = false,
                                            description = "Passive Flurstücke Bayern",
                                            minZoom = 14.5f
                                        )
                                    )
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
            version = ParcelConstants.endpointVersion.toString()
        ),
        paths = mapOf(
            //"/thueringen_flstck/FeatureServer/0/query" to ParcelConstants.DE_TH,
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
            parameters = Path.esriParams
        )
    )

    val thueringenWfs = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://www.geoproxy.geoportal-th.de/geoproxy/services"
            )
        ),
        info = Info(
            title = "Geoproxy Thüringen",
            description = "Location based Open Data for Thüringen",
            version = ParcelConstants.endpointVersion.toString(),
            license = License(
                name = "© GDI-Th",
                identifier = "Flurstücke Thüringen",
                url = LC_DL_BY
            )
        ),
        paths = mapOf(
            "/adv_alkis_wfs" to PathItem(
                summary = "Flurstücke Thüringen",
                post = Operation(
                    operationId = Bundesland.THUERINGEN.iso3166_2,
                    requestBody = ReferenceOr.Value(
                        RequestBody(
                            content = mapOf(
                                "application/xml" to MediaType(schema = ReferenceOr.Value(Schema(type = Schema.Type.Basic.Object)))
                            )
                        )
                    ),
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                        AviatorExtensionSpec.PluginMaterialization.O.key to ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/1.0"
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                        },
                        AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                            bounds = Bundesland.THUERINGEN.roughBoundaries,
                            correspondsTo = Bundesland.THUERINGEN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "flaeche",
                                parcelId = "flstkennz",
                                district = "gemarkung",
                                //districtId = "gemaschl",
                                districtCompartment = "flur",
                                plot = "flurstnr",
                                //districtCompartmentId = "flurschl",
                                districtMunicipalityId = "gmdschl",
                                districtRegion = "kreis",
                                districtMunicipality = "gemeinde",
                                //districtRegionId = "kreisschl",
                                //usageLegalDeviation = "abwrecht",
                                //plotNumerator = "flstnrzae",
                                //plotDenominator = "flstnrnen",
                                location = "lagebeztxt",
                                usageHint = "tntext"
                            ),
                            parcelLinkReference = Bundesland.THUERINGEN.iso3166_2 + "_default",
                        ).usable()
                    ),
                ),
            ),
        ),
        components = Components(
            parameters = Path.wfsParams
        )
    )
    val badenWuerttembergWfs = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://owsproxy.lgl-bw.de/owsproxy/wfs"
            )
        ),
        info = Info(
            title = "LGL Baden-Württemberg",
            description = "Landesweiter, vollständiger Flurstückslayer ALKIS-Daten tagesaktuell",
            version = ParcelConstants.endpointVersion.toString(),
            license = License(
                name = "LGL-BW: Datenlizenz Deutschland - Namensnennung - Version 2.0, www.lgl-bw.de",
                identifier = "Flurstücke Baden Württemberg",
                url = LC_DL_BY
            )
        ),
        paths = mapOf(
            "/WFS_LGL-BW_ALKIS" to PathItem(
                summary = "Flurstücke Baden Württemberg",
                get = Operation(
                    operationId = Bundesland.BADEN_WUERTTEMBERG.iso3166_2,
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                        AviatorExtensionSpec.PluginMaterialization.O.key to ParcelConstants.wfsAdapterConfig {
                            typeNames = "nora:v_al_flurstueck"
                            namespace = "http://nora-prod.lgl.bwl.de/nora"
                            nsPrefix = "nora"
                            geometryPointer = "geom"
                        },
                        AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                            bounds = Bundesland.BADEN_WUERTTEMBERG.roughBoundaries,
                            correspondsTo = Bundesland.BADEN_WUERTTEMBERG.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                parcelId = "flurstueckskennzeichen",
                                area = "amtliche_flaeche",
                                districtId = "gemarkung_id",
                                district = "gemarkung_name",
                                districtCompartmentId = "flurnummer",
                                plotNumerator = "zaehler",
                                plotDenominator = "nenner",
                                districtMunicipalityId = "gemeinde_id",
                                districtMunicipality = "gemeinde_name",
                            ),
                            parcelLinkReference = Bundesland.BADEN_WUERTTEMBERG.iso3166_2 + "_default",
                        ).usable()
                    ),
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameters(it) }
                ),
            ),
        ),
        components = Components(
            parameters = Path.wfsParams
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
        thueringenWfs,
        badenWuerttembergWfs,
        brightSky,
    )

    fun apiOverrideVersion(version: SemVer = ParcelConstants.endpointVersion): List<OpenAPI> {
        return apis.map { it.copy(info = it.info.copy(version = version.toString())) }
    }

    enum class Services(
        private val serviceLocator: String,
    ) {
        DISCOVERY_SERVICE("DCAACodexDiscoveryBundle"),
        SERVERSIDE_ACTIONS("DCAACodexServerSideActions"),
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