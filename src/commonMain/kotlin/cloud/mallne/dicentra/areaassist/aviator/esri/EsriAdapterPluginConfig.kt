package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.aviator.client.ktor.KtorLoggingIds
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginConfigScope
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import kotlinx.serialization.Serializable

@Serializable
data class EsriAdapterPluginConfig(val active: Boolean = false) : AviatorPluginConfigScope {
    override var serviceFilter: MutableList<ServiceLocator> = mutableListOf()
    override val silentLoggingTags: MutableList<String> = mutableListOf(KtorLoggingIds.WARN_OPTIONAL_FINALIZATION)
}