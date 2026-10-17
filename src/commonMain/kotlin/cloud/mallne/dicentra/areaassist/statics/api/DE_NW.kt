package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.LC_DL_BY
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceExtensions.parameter
import io.ktor.openapi.*

object DE_NW : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25832"
    private const val namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://www.wfs.nrw.de") }
        info = OpenApiInfo(
            title = "WFS NW ALKIS Vereinfacht",
            description = "Official simplified ALKIS parcel download service from Geobasis NRW.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Datenlizenz Deutschland Namensnennung 2.0",
                url = LC_DL_BY
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/geobasis/wfs_nw_alkis_vereinfacht" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Nordrhein-Westfalen",
                    get = Operation.build {
                        operationId = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            this.namespace = DE_NW.namespace
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                            inputCRS = DE_NW.crs
                            outputCRS = cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier.constructUrn("EPSG", "25832")
                            importCRS = "EPSG:25832"
                            exportCRS = DE_NW.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.NORDRHEIN_WESTFALEN.roughBoundaries,
                            correspondsTo = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2,
                            keys = DefaultKeys.fillIn(
                                area = "flaeche",
                                parcelId = "flstkennz",
                                district = "gemarkung",
                                districtId = "gemaschl",
                                districtCompartment = "flur",
                                districtCompartmentId = "flurschl",
                                districtMunicipality = "gemeinde",
                                districtMunicipalityId = "gmdschl",
                                districtRegion = "kreis",
                                districtRegionId = "kreisschl",
                                plotNumerator = "flstnrzae",
                                plotDenominator = "flstnrnen",
                                location = "lagebeztxt",
                                usageHint = "tntxt",
                                usageLegalDeviation = "abwrecht"
                            ),
                            parcelLinkReference = "${Bundesland.NORDRHEIN_WESTFALEN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "© Geobasis NRW / dl-de/by-2-0",
                                url = LC_DL_BY
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
