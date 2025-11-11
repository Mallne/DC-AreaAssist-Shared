package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.aviator.client.ktor.KtorLoggingIds
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginConfigScope
import cloud.mallne.dicentra.aviator.core.plugins.PluginActivationStrategy
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import kotlinx.serialization.Serializable

@Serializable
data class WfsAdapterPluginConfig(
    val active: Boolean = false,
) : AviatorPluginConfigScope {
    override var serviceFilter: MutableList<ServiceLocator> = mutableListOf()
    override var activationStrategy: PluginActivationStrategy = PluginActivationStrategy.EnabledByOAS
    override val silentLoggingTags: MutableList<String> = mutableListOf(KtorLoggingIds.WARN_OPTIONAL_FINALIZATION)
    var typeNames: String = ""
    var namespace: String = ""
    var nsPrefix: String = ""

    /** requested CRS for the WFS Query (wfs:Query srsName="urn:ogc:def:crs:EPSG::4326") */
    var outputCRS: String = Identifier.constructUrn("EPSG", "4326")

    /** used by the coordinates for the GML service (gml:Envelope srsName="urn:ogc:def:crs:EPSG::4326") */
    var inputCRS: String = outputCRS

    /** imported as the CRS; used to convert all the GML Coordinates to 4326 (plugin-only) */
    var importCRS: String = outputCRS

    /** exported as CRS; used to convert client-side 4326 Coordinates to the WFS (plugin-only)  */
    var exportCRS: String = inputCRS
}