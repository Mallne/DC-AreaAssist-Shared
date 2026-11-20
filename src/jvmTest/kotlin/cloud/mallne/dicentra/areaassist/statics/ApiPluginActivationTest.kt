package cloud.mallne.dicentra.areaassist.statics

import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.esri.EsriAdapterPluginInstance
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPlugin
import cloud.mallne.dicentra.areaassist.aviator.wfs.WfsAdapterPluginInstance
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-pluginMaterialization`
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec.`x-dicentra-aviator-serviceDelegateCall`
import cloud.mallne.dicentra.aviator.core.plugins.ClientPluginRegistration
import cloud.mallne.dicentra.aviator.core.plugins.DefaultAviatorPluginRegistry
import cloud.mallne.dicentra.aviator.koas.typed.routes
import cloud.mallne.dicentra.aviator.model.ServiceLocator
import cloud.mallne.dicentra.aviator.plugin.weaver.WeaverPlugin
import cloud.mallne.geokit.coordinates.CrsRegistry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiPluginActivationTest {
    @Test
    fun bundledOperationsActivateTheirDeclaredClientPlugins() = runTest {
        val registry = DefaultAviatorPluginRegistry(clients = listOf(
            ClientPluginRegistration(EsriAdapterPlugin.install {}),
            ClientPluginRegistration(WfsAdapterPlugin(CrsRegistry()).install {}),
            ClientPluginRegistration(WeaverPlugin.install {}),
        ))
        val seen = mutableMapOf<String, Int>()
        for (api in APIs.apis) {
            for (route in api.routes()) {
                val requested = route.`x-dicentra-aviator-pluginMaterialization`.orEmpty()
                val known = requested.keys.intersect(setOf(EsriAdapterPlugin.identity, WfsAdapterPlugin.IDENTITY, WeaverPlugin.identity))
                if (known.isEmpty()) continue
                val locator = ServiceLocator(requireNotNull(route.`x-dicentra-aviator-serviceDelegateCall`) {
                    "${api.info.title} ${route.path} requests a client plugin but has no service locator"
                })
                val selected = registry.invocationPlugins(locator, requested).clientPlugins
                assertEquals(known, selected.map { it.identity }.toSet(), "${api.info.title} ${route.path}")
                selected.forEach { plugin ->
                    seen[plugin.identity] = (seen[plugin.identity] ?: 0) + 1
                    when (plugin) {
                        is WfsAdapterPluginInstance -> {
                            assertTrue(plugin.configurationBundle.active, "${api.info.title}: inactive WFS")
                            assertTrue(plugin.configurationBundle.typeNames.isNotBlank(), "${api.info.title}: no typeNames")
                        }
                        is EsriAdapterPluginInstance -> assertTrue(plugin.configurationBundle.active, "${api.info.title}: inactive Esri")
                    }
                }
            }
        }
        assertTrue((seen[WfsAdapterPlugin.IDENTITY] ?: 0) > 0)
        assertEquals(3, seen[WeaverPlugin.identity])
        // Esri's bundled document currently has no operations. Its reusable parcel
        // definitions still need to materialize correctly when consumed separately.
        val esri = registry.invocationPlugins(ParcelConstants.locator, ParcelConstants.esriAdapterConfig)
            .clientPlugins.filterIsInstance<EsriAdapterPluginInstance>().single()
        assertTrue(esri.configurationBundle.active)
    }
}
