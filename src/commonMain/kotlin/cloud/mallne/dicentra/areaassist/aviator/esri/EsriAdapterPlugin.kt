package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.aviator.core.plugins.AviatorPlugin
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance

object EsriAdapterPlugin : AviatorPlugin<EsriAdapterPluginConfig> {
    override val identity: String = "DC-AVEsriAdapter"
    override fun install(config: EsriAdapterPluginConfig.() -> Unit): AviatorPluginInstance {
        val pluginConfig = EsriAdapterPluginConfig()
        config.invoke(pluginConfig)
        return EsriAdapterPluginInstance(
            pluginConfig,
            identity,
        )
    }
}