package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.aviator.core.plugins.AviatorPlugin
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.geokit.coordinates.CrsRegistry

data class WfsAdapterPlugin(val registry: CrsRegistry) : AviatorPlugin<WfsAdapterPluginConfig> {
    override val identity: String = IDENTITY
    override fun install(config: WfsAdapterPluginConfig.() -> Unit): AviatorPluginInstance {
        val pluginConfig = WfsAdapterPluginConfig()
        config.invoke(pluginConfig)
        return WfsAdapterPluginInstance(
            pluginConfig,
            identity,
            registry,
        )
    }

    companion object {
        const val IDENTITY: String = "WfsAdapter"
    }

    object Parameters {
        const val SERVICE = "SERVICE"
        const val VERSION = "VERSION"
        const val REQUEST = "REQUEST"
        const val TYPE_NAMES = "typeNames"
        const val SRS_NAME = "SRSName"
        const val BBOX = "bbox"
        const val COUNT = "count"
    }
}