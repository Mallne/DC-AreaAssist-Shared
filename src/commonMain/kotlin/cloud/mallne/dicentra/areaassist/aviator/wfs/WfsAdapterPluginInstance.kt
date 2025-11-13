package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.AviatorServiceDataHolder.Companion.json
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionContext
import cloud.mallne.dicentra.aviator.core.execution.AviatorExecutionStages
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.plugins.AviatorPluginInstance
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutor
import cloud.mallne.dicentra.aviator.core.plugins.PluginStagedExecutorBuilder
import cloud.mallne.dicentra.aviator.model.AviatorServiceUtils.optionBundle
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter.encodeToElement
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlAdapter.xml
import cloud.mallne.dicentra.aviator.plugin.adapter.xml.XmlBody
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.Vertex
import cloud.mallne.geokit.coordinates.CrsRegistry
import cloud.mallne.geokit.coordinates.model.AbstractCoordinate
import cloud.mallne.geokit.coordinates.model.AbstractCoordinate.Companion.toCoordinate
import cloud.mallne.geokit.coordinates.tokens.ast.expression.Identifier
import cloud.mallne.geokit.geojson.*
import cloud.mallne.geokit.geojson.CalculationInterop.toPosition
import cloud.mallne.geokit.geojson.CalculationInterop.toVertex
import cloud.mallne.geokit.interop.WfsExtensions.toGeoJson
import cloud.mallne.geokit.ogc.model.Envelope
import cloud.mallne.geokit.ogc.model.LowerCorner
import cloud.mallne.geokit.ogc.model.UpperCorner
import cloud.mallne.geokit.ogc.model.fes.BBOX
import cloud.mallne.geokit.ogc.model.fes.Filter
import cloud.mallne.geokit.ogc.model.wfs.FeatureCollection
import cloud.mallne.geokit.ogc.model.wfs.GetFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.time.ExperimentalTime
import cloud.mallne.geokit.ogc.model.wfs.Query as WfsQuery

data class WfsAdapterPluginInstance(
    override val configurationBundle: WfsAdapterPluginConfig,
    override val identity: String,
    val registry: CrsRegistry,
) : AviatorPluginInstance {
    @OptIn(ExperimentalTime::class)
    override val x: PluginStagedExecutor<AviatorExecutionContext<@Serializable Any, @Serializable Any>, @Serializable Any, @Serializable Any> =
        PluginStagedExecutorBuilder.steps {
            if (configurationBundle.active) {
                preExecution {
                    for ((epsgId, wktcrs) in configurationBundle.importCRSData) {
                        if (!registry.understands(epsgId)) {
                            registry.ingest(epsgId)
                        }
                    }
                }
                before(AviatorExecutionStages.PathMatching) { context ->
                    val returnGeometry =
                        (context.requestParams[AreaAssistParameters.RETURN_GEOMETRY]?.asType<Boolean>()) ?: true
                    val boundary = (context.requestParams[AreaAssistParameters.BOUNDARY]?.asType<Boundary>())
                    val queries =
                        (context.requestParams[AreaAssistParameters.QUERIES]?.asType<List<Query>>()) ?: listOf()
                    val options = optionBundle<ParcelServiceOptions>(context.dataHolder.options, Serialization())

                    context.requestParams[WfsAdapterPlugin.Parameters.SERVICE] = RequestParameter.Single("WFS")
                    context.requestParams[WfsAdapterPlugin.Parameters.VERSION] = RequestParameter.Single("2.0.0")
                    context.requestParams[WfsAdapterPlugin.Parameters.REQUEST] = RequestParameter.Single("GetFeature")
                    context.requestParams[WfsAdapterPlugin.Parameters.COUNT] = RequestParameter.Single(2000)
                    context.requestParams[WfsAdapterPlugin.Parameters.TYPE_NAMES] =
                        RequestParameter.Single(configurationBundle.typeNames)
                    context.requestParams[WfsAdapterPlugin.Parameters.SRS_NAME] =
                        RequestParameter.Single(configurationBundle.outputCRS)
                    boundary?.let {
                        val sw = boundary.southWest.translate(targetUrn = configurationBundle.exportCRS)
                        val ne = boundary.northEast.translate(targetUrn = configurationBundle.exportCRS)
                        context.requestParams[WfsAdapterPlugin.Parameters.BBOX] =
                            RequestParameter.Single(
                                BBOX(
                                    envelope = Envelope(
                                        lowerCorner = LowerCorner(sw.wfsCoord()),
                                        upperCorner = UpperCorner(ne.wfsCoord()),
                                        srsName = configurationBundle.inputCRS
                                    )
                                )
                            ) { bBOX ->
                                "${bBOX.envelope.lowerCorner.values.joinToString(separator = ",") { it.toString() }},${
                                    bBOX.envelope.upperCorner.values.joinToString(
                                        separator = ","
                                    ) { it.toString() }
                                },${bBOX.envelope.srsName}"
                            }
                    }
                }
                after(AviatorExecutionStages.FormingRequest) { context ->
                    val service = (context.requestParams[WfsAdapterPlugin.Parameters.SERVICE].toString())
                    val version = (context.requestParams[WfsAdapterPlugin.Parameters.VERSION].toString())
                    val count = (context.requestParams[WfsAdapterPlugin.Parameters.COUNT]?.asType<Int>())
                    val typeName = (context.requestParams[WfsAdapterPlugin.Parameters.TYPE_NAMES].toString())
                    val srs = (context.requestParams[WfsAdapterPlugin.Parameters.SRS_NAME].toString())
                    val bbox = (context.requestParams[WfsAdapterPlugin.Parameters.BBOX]?.asType<BBOX>())

                    context.networkChain.forEach { net ->
                        val bboxFilter = bbox?.let { Filter(it) }
                        net.request?.outgoingContent = XmlBody(
                            context.dataHolder.xml.encodeToElement(
                                GetFeature(
                                    service = service,
                                    version = version,
                                    count = count,
                                    queries = listOf(
                                        WfsQuery(
                                            typeNames = listOf(typeName),
                                            srsName = srs,
                                            filter = bboxFilter!!
                                        )
                                    )
                                )
                            )
                        )
                    }
                }
                after(AviatorExecutionStages.PaintingResponse) { context ->
                    val successful =
                        context.networkChain.find { (it.response?.status?.value ?: 500) < 400 }
                    val serviceOptions = optionBundle<ParcelServiceOptions>(context.dataHolder.options)
                    context.result = try {
                        val parcels: MutableList<ParcelCrateEntity> = mutableListOf()
                        val byteArray = successful?.response?.content
                        if (byteArray != null) {
                            val string = byteArray.decodeToString()
                            val featureCollection: FeatureCollection =
                                context.dataHolder.xml.decodeFromString<FeatureCollection>(string)
                            val geojson = featureCollection.toGeoJson(
                                configurationBundle.nsPrefix,
                                configurationBundle.geometryPointer,
                                context.dataHolder.xml
                            )
                            // look if to many results
                            for (feature in geojson) {
                                val translatedFeature = JsonFeature(
                                    geometry = feature.geometry?.translate(
                                        Identifier.deconstructUrnId(configurationBundle.importCRS), "EPSG:4326"
                                    ),
                                    properties = JsonObject(feature.properties),
                                    id = feature.id,
                                    bbox = feature.bbox,
                                )
                                AreaAssistParameters.inflateParcelFromFeature(
                                    feature = translatedFeature,
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
        }

    private fun Geometry.translate(from: String, to: String): Geometry = when (this) {
        is GeometryCollection<*> -> GeometryCollection(
            bbox = this.bbox?.t(from, to),
            geometries = this.geometries.map { it.translate(from, to) })

        is LineString -> LineString(
            bbox = this.bbox?.t(from, to),
            coordinates = this.coordinates.map { it.t(from, to) })

        is MultiLineString -> MultiLineString(
            bbox = this.bbox?.t(from, to),
            coordinates = this.coordinates.map { it.map { it.t(from, to) } })

        is MultiPoint -> MultiPoint(
            bbox = this.bbox?.t(from, to),
            coordinates = this.coordinates.map { it.t(from, to) })

        is MultiPolygon -> MultiPolygon(
            bbox = this.bbox?.t(from, to),
            coordinates = this.coordinates.map { it.map { it.map { it.t(from, to) } } })

        is Point -> Point(bbox = this.bbox?.t(from, to), coordinates = this.coordinates.t(from, to))
        is Polygon -> Polygon(
            bbox = this.bbox?.t(from, to),
            coordinates = this.coordinates.map { it.map { it.t(from, to) } })
    }

    private fun BoundingBox.t(from: String, to: String): BoundingBox =
        BoundingBox(northeast = this.northeast.t(from, to), southwest = this.southwest.t(to))

    private fun Position.t(
        source: String = "EPSG:4326",
        target: String = "EPSG:4326",
    ) = this.toVertex().t(source, target).toPosition()

    private fun Vertex.t(
        source: String = "EPSG:4326",
        target: String = "EPSG:4326",
    ): Vertex = registry.compose(
        toCoordinate(),
        source,
        target
    ).execute().toVertex()

    private fun Vertex.translate(
        sourceUrn: String = Identifier.constructUrn("EPSG", "4326"),
        targetUrn: String = Identifier.constructUrn("EPSG", "4326")
    ): AbstractCoordinate = registry.compose(
        toCoordinate(),
        Identifier.deconstructUrnId(sourceUrn),
        Identifier.deconstructUrnId(targetUrn)
    ).execute()

    private fun AbstractCoordinate.wfsCoord() = listOf(latitude, longitude)
    private fun Vertex.wfsCoord() = listOf(latitude, longitude)

    override fun requestReconfigure(oasConfig: JsonElement): AviatorPluginInstance {
        try {
            val newConf = Json.decodeFromJsonElement(WfsAdapterPluginConfig.serializer(), oasConfig)
            return this.copy(configurationBundle = newConf)
        } catch (_: IllegalArgumentException) {
            return this
        }
    }
}