package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.DefaultKeys
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.Path
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants.locator
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceOptions`
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceExtensions.parameter
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import io.ktor.openapi.*

object DE_HH : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25832"
    private const val namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://geodienste.hamburg.de") }
        info = OpenApiInfo(
            title = "WFS ALKIS Hamburg - Vereinfacht",
            description = "Official simplified ALKIS parcel service from LGV Hamburg.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Freie und Hansestadt Hamburg, Landesbetrieb Geoinformation und Vermessung",
                url = "https://creativecommons.org/licenses/by/4.0/"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/WFS_HH_ALKIS_vereinfacht" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Hamburg",
                    get = Operation.build {
                        operationId = Bundesland.HAMBURG.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            this.namespace = DE_HH.namespace
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                            inputCRS = DE_HH.crs
                            outputCRS = Identifier.constructUrn("EPSG", "25832")
                            importCRS = "EPSG:25832"
                            exportCRS = DE_HH.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.HAMBURG.roughBoundaries,
                            correspondsTo = Bundesland.HAMBURG.iso3166_2,
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
                            parcelLinkReference = "${Bundesland.HAMBURG.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "Freie und Hansestadt Hamburg, LGV, CC BY 4.0",
                                url = "https://creativecommons.org/licenses/by/4.0/"
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
