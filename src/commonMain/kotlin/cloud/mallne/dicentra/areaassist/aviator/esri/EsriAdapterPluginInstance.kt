package cloud.mallne.dicentra.areaassist.aviator.esri

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.exceptions.ExceededTransferLimitException
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
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
import cloud.mallne.geokit.geojson.FeatureCollection
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.ExperimentalTime

data class EsriAdapterPluginInstance(
    override val configurationBundle: EsriAdapterPluginConfig,
    override val identity: String,
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

                val queryString = buildQueryString(queries, options)

                context.requestParams[EsriAdapterPlugin.Parameters.WHERE] =
                    RequestParameter.Single(queryString.encodeURLParameter(false))
                boundary?.let { boundingBox ->
                    val geo = "${boundingBox.west},${boundingBox.north},${boundingBox.east},${boundingBox.south}"
                    context.requestParams[EsriAdapterPlugin.Parameters.GEOMETRY] =
                        RequestParameter.Single(geo.encodeURLParameter(false))
                    context.requestParams[EsriAdapterPlugin.Parameters.IN_SR] = RequestParameter.Single("4326")
                    context.requestParams[EsriAdapterPlugin.Parameters.SPATIAL_REL] =
                        RequestParameter.Single("esriSpatialRelIntersects")
                    context.requestParams[EsriAdapterPlugin.Parameters.GEOMETRY_TYPE] =
                        RequestParameter.Single("esriGeometryEnvelope")
                }
                context.requestParams[EsriAdapterPlugin.Parameters.OUT_FIELDS] =
                    RequestParameter.Single(getOutFields(options))
                context.requestParams[EsriAdapterPlugin.Parameters.OUT_SR] = RequestParameter.Single("4326")
                if (returnGeometry) {
                    context.requestParams[EsriAdapterPlugin.Parameters.RETURN_GEOMETRY] =
                        RequestParameter.Single("true")
                }
                context.requestParams[EsriAdapterPlugin.Parameters.FILE] = RequestParameter.Single("geojson")
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

    private fun getOutFields(options: ParcelServiceOptions): String {
        if (options.minimalDefinition) {
            val outFields = mutableListOf<String>()
            for (key in options.keys) {
                if (key.reference != null) {
                    outFields.add(key.reference)
                }
            }
            return outFields.joinToString(",")
        } else {
            return "*"
        }
    }

    private fun buildQueryString(query: List<Query>, options: ParcelServiceOptions): String {
        var queryString = ""
        val qsa = mutableListOf<String>()
        query.forEachIndexed { i, q ->
            var qs = ""
            var runInErr = false
            val field = options.keys.find { it.identifier == q.field?.identifier }
            val content = q.content
            val reference = field?.reference
            if (field != null && content != null && reference != null) {
                qs += if (i != 0) {
                    " ${q.method.urlParam} "
                } else {
                    " "
                }
                if (!content.sameAs(field.type)) {
                    runInErr = true
                } else {
                    when (content) {
                        is QueryContentHolder.RANGE -> {
                            val range = content.range
                            if (range == null) {
                                runInErr = true
                            } else if (range.first > range.second || range.first < 0.0 || (range.first == 0.0 && range.second == 0.0)) {
                                runInErr = true
                            } else {
                                qs += "($reference = ${range.first} OR $reference = ${range.second})"
                            }
                        }

                        is QueryContentHolder.STRING -> {
                            val string = content.string
                            if (string == null) {
                                runInErr = true
                            } else if (string.isNotBlank()) {
                                qs += "$reference = '${string.uppercase()}'"
                            } else {
                                runInErr = true
                            }
                        }

                        is QueryContentHolder.BOOLEAN -> {
                            val boolean = content.boolean
                            if (boolean == null) {
                                runInErr = true
                            } else {
                                qs += "$reference = '$boolean'"
                            }
                        }
                    }
                }
            } else {
                runInErr = true
            }

            if (!runInErr) {
                qsa.add(qs)
            }
        }
        if (query.isEmpty()) {
            qsa.add("1=1")
        }
        if (qsa.isNotEmpty()) {
            queryString = qsa.joinToString(separator = " ")
        }
        return queryString
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