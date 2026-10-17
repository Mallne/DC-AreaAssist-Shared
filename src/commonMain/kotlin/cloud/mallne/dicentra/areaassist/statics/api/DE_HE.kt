package cloud.mallne.dicentra.areaassist.statics.api

import cloud.mallne.dicentra.areaassist.model.areas.Bundesland
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.statics.ParcelConstants
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

object DE_HE : ApiObject {
    private const val crs = "urn:ogc:def:crs:EPSG::25832"

    override val value: OpenApiDoc = OpenApiDoc.build {
        `x-dicentra-aviator` = AviatorExtensionSpec.SpecVersion
        servers { server("https://www.gds.hessen.de") }
        info = OpenApiInfo(
            title = "WFS HE ALKIS Vereinfacht",
            description = "Official simplified ALKIS parcel WFS from HVBG Hessen.",
            version = ParcelConstants.endpointVersion.toString(),
            license = OpenApiInfo.License(name = "Datenlizenz Deutschland Zero 2.0", url = "https://www.govdata.de/dl-de/zero-2-0")
        )
        components = Components(parameters = Path.wfsParams)
    }.copy(
        paths = mapOf(
            "/wfs2/aaa-suite/cgi-bin/alkis/vereinf/wfs" to ReferenceOr.value(
                PathItem(
                    summary = "Flurstücke Hessen",
                    get = Operation.build {
                        operationId = Bundesland.HESSEN.iso3166_2
                        `x-dicentra-aviator-serviceDelegateCall` = locator
                        `x-dicentra-aviator-pluginMaterialization` = ParcelConstants.wfsAdapterConfig {
                            typeNames = DE.SIMPLIFIED_ALKIS_TYPE_NAME
                            this.namespace = DE.SIMPLIFIED_ALKIS_NAMESPACE
                            nsPrefix = DE.SIMPLIFIED_ALKIS_PREFIX
                            geometryPointer = DE.SIMPLIFIED_ALKIS_GEOMETRY
                            inputCRS = crs
                            outputCRS = Identifier.constructUrn("EPSG", "25832")
                            importCRS = "EPSG:25832"
                            exportCRS = crs
                        }
                        `x-dicentra-aviator-serviceOptions` = ParcelServiceOptions(
                            boundary = Bundesland.HESSEN.boundary,
                            correspondsTo = Bundesland.HESSEN.iso3166_2,
                            parcelIdPrefix = "06",
                            keys = DE.simplifiedAlkisKeys(),
                            parcelLinkReference = "${Bundesland.HESSEN.iso3166_2}_default",
                            license = OpenApiInfo.License(
                                name = "HVBG Hessen, Geobasisdaten © HVBG",
                                url = "https://www.govdata.de/dl-de/zero-2-0"
                            )
                        ).usable()
                    },
                    parameters = Path.wfsParams.keys.map { ReferenceOr.parameter(it) }
                )
            )
        )
    )
}
