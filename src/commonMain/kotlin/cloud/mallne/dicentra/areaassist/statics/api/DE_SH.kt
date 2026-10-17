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

object DE_SH : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25832"
    private const val namespace = "http://repository.gdi-de.org/schemas/adv/produkt/alkis-vereinfacht/2.0"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://service.gdi-sh.de") }
        info = OpenApiInfo(
            title = "ALKIS Schleswig-Holstein (Open Data)",
            description = "Official parcel data from LVermGeo SH, licensed CC BY 4.0.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(
                name = "Creative Commons Namensnennung 4.0 International (CC BY 4.0)",
                url = "https://creativecommons.org/licenses/by/4.0/"
            )
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/WFS_SH_ALKIS_vereinf_OpenGBD" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Schleswig-Holstein",
                    get = Operation.build {
                        operationId = Bundesland.SCHLESWIG_HOLSTEIN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = "ave:Flurstueck"
                            this.namespace = DE_SH.namespace
                            nsPrefix = "ave"
                            geometryPointer = "geometrie"
                            inputCRS = DE_SH.crs
                            outputCRS = Identifier.constructUrn("EPSG", "25832")
                            importCRS = "EPSG:25832"
                            exportCRS = DE_SH.crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            bounds = Bundesland.SCHLESWIG_HOLSTEIN.roughBoundaries,
                            correspondsTo = Bundesland.SCHLESWIG_HOLSTEIN.iso3166_2,
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
                            parcelLinkReference = "${Bundesland.SCHLESWIG_HOLSTEIN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "© GeoBasis-DE/LVermGeo SH/CC BY 4.0",
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
