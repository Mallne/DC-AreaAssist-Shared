package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.DisplayConstraints
import cloud.mallne.dicentra.areaassist.model.SystemMode
import cloud.mallne.dicentra.areaassist.model.map.MapLayer
import cloud.mallne.dicentra.areaassist.model.map.MapSource
import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.Translatable
import cloud.mallne.dicentra.areaassist.statics.APIs.Services
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.servers.Server

object MapLight : ApiObject {
    override val value: OpenAPI = OpenAPI(
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
                            constraints = DisplayConstraints(listOf(SystemMode.Light)),
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
                                            display = Translatable.Localization(
                                                mapOf(
                                                    Translatable.Localization.ENGLISH to "Orthophotos Thuringia",
                                                    Translatable.Localization.GERMAN to "Orthofotos Thüringen"
                                                )
                                            ),
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
                                            display = Translatable.Localization(
                                                mapOf(
                                                    Translatable.Localization.ENGLISH to "Parcels Bavaria",
                                                    Translatable.Localization.GERMAN to "Flurstücke Bayern"
                                                )
                                            ),
                                            minZoom = 14.5f
                                        )
                                    )
                                ),
                            ),
                            serviceHint = "basemap_light_default",
                            name = Translatable.Localization.nonTranslatable("Basemap.world"),
                            mapFont = "Roboto Regular"
                        ).usable()
                    ),
                )
            )
        )
    )
}