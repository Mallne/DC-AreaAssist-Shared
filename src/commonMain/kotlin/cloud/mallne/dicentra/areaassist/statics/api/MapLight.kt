package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.DisplayConstraints
import cloud.mallne.dicentra.areaassist.model.SystemMode
import cloud.mallne.dicentra.areaassist.model.map.MapLayer
import cloud.mallne.dicentra.areaassist.model.map.MapSource
import cloud.mallne.dicentra.areaassist.model.map.MapStyleServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.Translatable
import cloud.mallne.dicentra.areaassist.statics.APIs.Services
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import io.ktor.openapi.*

object MapLight : ApiObject {
    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers {
            server("https://sgx.geodatenzentrum.de")
        }
        info = OpenApiInfo(
            title = "Basemap Light",
            description = "Official German Vector Map",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                "basemap.de / BKG | Datenquellen: © GeoBasis-DE",
                identifier = "Basemap Light"
            )
        )
    }.copy(
        paths = mapOf(
            "/gdz_basemapworld_vektor/styles/bm_web_wld_col.json" to ReferenceOr.value(
                PathItem(
                    get = Operation.build {
                        `x-dicentra-aviator-serviceDelegateCall` = Services.MAPLAYER.locator(ServiceMethods.GATHER)
                        `x-dicentra-aviator-serviceOptions` = MapStyleServiceOptions(
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
                    },
                )
            )
        )
    )
}