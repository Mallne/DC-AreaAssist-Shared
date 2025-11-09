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
    var inputCRS: String = Identifier.constructUrn("EPSG", "4326")
    var importCRS: String = Identifier.constructUrn("EPSG", "4326")
    var outputCRS: String = Identifier.constructUrn("EPSG", "4326")
    var exportCRS: String = Identifier.constructUrn("EPSG", "4326")
}