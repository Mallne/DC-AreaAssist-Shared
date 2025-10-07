package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionContext
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionStages
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutor
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutorBuilder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class EsriAdapterPluginInstance(
    override val configurationBundle: EsriAdapterPluginConfig,
    override val identity: String,
) : AviatorPluginInstance {
    override val x: PluginStagedExecutor<AviatorExecutionContext<@Serializable Any, @Serializable Any>, @Serializable Any, @Serializable Any> =
        PluginStagedExecutorBuilder.steps {
            after(AviatorExecutionStages.PaintingResponse) { context ->

            }
            before(AviatorExecutionStages.FormingRequest) { context ->
                val current = getThisPlugin<EsriAdapterPluginInstance>(context)
                val active = current?.configurationBundle?.active ?: false
                if (!active) return@before
                val body = if (context.body != null && context.bodyClazz != null) {
                    context.dataHolder.json.encodeToJsonElement(
                        context.bodyClazz!!.third,
                        context.body!!
                    )
                } else {
                    null
                }
                context.body = CatalystRequest(
                    parameters = context.requestParams.toStringList(),
                    body = body
                )
                context.bodyClazz = Triple(
                    CatalystRequest::class,
                    typeOf<CatalystRequest>(),
                    serializer<CatalystRequest>()
                ) as Triple<KClass<@Serializable Any>, KType, KSerializer<@Serializable Any>>
            }
        }


    override fun requestReconfigure(oasConfig: JsonElement): AviatorPluginInstance {
        try {
            val newConf = Json.decodeFromJsonElement(EsriAdapterPluginConfig.serializer(), oasConfig)
            return this.copy(configurationBundle = newConf)
        } catch (_: IllegalArgumentException) {
            return this
        }
    }
}