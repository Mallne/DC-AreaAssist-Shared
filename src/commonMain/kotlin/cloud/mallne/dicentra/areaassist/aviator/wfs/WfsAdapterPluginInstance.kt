package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.exceptions.ExceededTransferLimitException
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionContext
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionStages
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutor
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutorBuilder
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils.optionBundle
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.geokit.coordinates.model.AbstractCoordinate.Companion.toCoordinate
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import cloud.mallne.geokit.geojson.FeatureCollection
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import nl.adaptivity.xmlutil.serialization.XML
import kotlin.time.ExperimentalTime

data class WfsAdapterPluginInstance(
    override val configurationBundle: WfsAdapterPluginConfig,
    override val identity: String,
    val registry: CrsRegistry,
    val xml: XML
) : AviatorPluginInstance {
    @OptIn(ExperimentalTime::class)
    override val x: PluginStagedExecutor<AviatorExecutionContext<@Serializable Any, @Serializable Any>, @Serializable Any, @Serializable Any> =
        PluginStagedExecutorBuilder.steps {
            before(AviatorExecutionStages.PathMatching) { context ->
                val returnGeometry =
                    (context.requestParams[AreaAssistParameters.RETURN_GEOMETRY] as? RequestParameter.Single<Boolean>)?.value
                        ?: true
                val boundary =
                    (context.requestParams[AreaAssistParameters.BOUNDARY] as? RequestParameter.Single<Boundary>)?.value
                val queries =
                    (context.requestParams[AreaAssistParameters.QUERIES] as? RequestParameter.Multi<Query>)?.value
                        ?: listOf()
                val options = optionBundle<ParcelServiceOptions>(context.dataHolder.options, Serialization())

                context.requestParams[WfsAdapterPlugin.Parameters.SERVICE] = RequestParameter.Single("WFS")
                context.requestParams[WfsAdapterPlugin.Parameters.VERSION] = RequestParameter.Single("2.0.0")
                context.requestParams[WfsAdapterPlugin.Parameters.REQUEST] = RequestParameter.Single("GetFeature")
                context.requestParams[WfsAdapterPlugin.Parameters.TYPE_NAMES] =
                    RequestParameter.Single(configurationBundle.typeNames)
                context.requestParams[WfsAdapterPlugin.Parameters.SRS_NAME] =
                    RequestParameter.Single(configurationBundle.outputCRS)
                boundary?.let {
                    context.requestParams[WfsAdapterPlugin.Parameters.BBOX] =
                        RequestParameter.Single(translateBBOX(it, configurationBundle.inputCRS))
                }
                context.requestParams[WfsAdapterPlugin.Parameters.COUNT] = RequestParameter.Single("200")
            }
            after(AviatorExecutionStages.PaintingResponse) { context ->
                val successful =
                    context.networkChain.find { (it.response?.status?.value ?: 500) < 400 }
                val serviceOptions = optionBundle<ParcelServiceOptions>(context.dataHolder.options)
                context.result = try {
                    val parcels: MutableList<ParcelCrateEntity> = mutableListOf()
                    val json = successful?.response?.content
                    if (json != null) {
                        val featureCollection: FeatureCollection =
                            context.dataHolder.json.decodeFromJsonElement<FeatureCollection>(json)
                        // look if to many results
                        val outerProperties = json.jsonObject["properties"]
                        val exd = outerProperties?.jsonObject?.get("exceededTransferLimit")
                            ?.let { context.dataHolder.json.decodeFromJsonElement<Boolean>(it) }
                        if (exd == true) {
                            context.log { warn("Exceeded Transfer Limit for ${successful.url}") }
                            throw ExceededTransferLimitException("Exceeded Transfer Limit for ${successful.url}")
                        }
                        for (feature in featureCollection) {
                            AreaAssistParameters.inflateParcelFromFeature(
                                feature = feature,
                                keys = serviceOptions.keys,
                                origin = serviceOptions.parcelLinkReference,
                                mode = AreaAssistParameters.InflationMode.Reference,
                                json = context.dataHolder.json
                            )?.let { parcels += it }
                        }
                    }
                    parcels
                } catch (e: Exception) {
                    context.log {
                        error(
                            "An unexpected Error occurred while parsing the response",
                            e
                        )
                    }
                    emptyList<ParcelCrateEntity>()
                }
            }
        }

    private fun translateBBOX(boundary: Boundary, targetUrn: String): String {
        //The boundary is always in 4326
        val sw = boundary.southWest.toCoordinate()
        val ne = boundary.northEast.toCoordinate()
        val first = registry.compose(sw, "EPSG:4326", Identifier.deconstructUrnId(targetUrn)).execute()
        val second = registry.compose(ne, "EPSG:4326", Identifier.deconstructUrnId(targetUrn)).execute()
        return "${first.longitude},${first.latitude},${second.longitude},${second.latitude}"
    }

    override fun requestReconfigure(oasConfig: JsonElement): AviatorPluginInstance {
        try {
            val newConf = Json.decodeFromJsonElement(WfsAdapterPluginConfig.serializer(), oasConfig)
            return this.copy(configurationBundle = newConf)
        } catch (_: IllegalArgumentException) {
            return this
        }
    }
}