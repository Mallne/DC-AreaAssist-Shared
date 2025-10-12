package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.aviator.core.plugins.AviatorPlugin
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance

object EsriAdapterPlugin : AviatorPlugin<EsriAdapterPluginConfig> {
    override val identity: String = "EsriAdapter"
    override fun install(config: EsriAdapterPluginConfig.() -> Unit): AviatorPluginInstance {
        val pluginConfig = EsriAdapterPluginConfig()
        config.invoke(pluginConfig)
        return EsriAdapterPluginInstance(
            pluginConfig,
            identity,
        )
    }

    object Parameters {
        const val WHERE = "where"
        const val OUT_FIELDS = "outFields"
        const val GEOMETRY = "geometry"
        const val RETURN_GEOMETRY = "returnGeometry"
        const val IN_SR = "inSR"
        const val OUT_SR = "outSR"
        const val GEOMETRY_TYPE = "geometryType"
        const val SPATIAL_REL = "spatialRel"
        const val FILE = "f"
    }
}