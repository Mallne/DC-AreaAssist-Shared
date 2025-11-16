package cloud.mallne.dicentra.areaassist.aviator.wfs

import cloud.mallne.dicentra.areaassist.aviator.AreaAssistParameters
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
import cloud.mallne.dicentra.areaassist.model.curator.QueryMethod
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelKey
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
import cloud.mallne.geokit.ogc.model.fes.*
import cloud.mallne.geokit.ogc.model.gml.DirectPositionType
import cloud.mallne.geokit.ogc.model.gml.Envelope
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
                                        lowerCorner = DirectPositionType(sw.wfsCoord()),
                                        upperCorner = DirectPositionType(ne.wfsCoord()),
                                        srsName = configurationBundle.inputCRS
                                    )
                                )
                            ) { bBOX ->
                                "${bBOX.envelope.lowerCorner.value.joinToString(separator = ",") { it.toString() }},${
                                    bBOX.envelope.upperCorner.value.joinToString(
                                        separator = ","
                                    ) { it.toString() }
                                },${bBOX.envelope.srsName}"
                            }
                    }

                    //Build Query
                    val filter = buildFesFilter(queries, options)
                    filter?.let {
                        context.requestParams[WfsAdapterPlugin.Parameters.QUERY] = RequestParameter.Single(it)
                    }
                }
                after(AviatorExecutionStages.FormingRequest) { context ->
                    val service = (context.requestParams[WfsAdapterPlugin.Parameters.SERVICE].toString())
                    val version = (context.requestParams[WfsAdapterPlugin.Parameters.VERSION].toString())
                    val count = (context.requestParams[WfsAdapterPlugin.Parameters.COUNT]?.asType<Int>())
                    val typeName = (context.requestParams[WfsAdapterPlugin.Parameters.TYPE_NAMES].toString())
                    val srs = (context.requestParams[WfsAdapterPlugin.Parameters.SRS_NAME].toString())
                    val bbox = (context.requestParams[WfsAdapterPlugin.Parameters.BBOX]?.asType<BBOX>())
                    val query =
                        (context.requestParams[WfsAdapterPlugin.Parameters.QUERY]?.asType<AbstractOperatorType>())

                    context.networkChain.forEach { net ->
                        val filters = listOfNotNull(query, bbox)
                        val filter = Filter.fromList(filters)
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
                                            filter = filter
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

    /**
     * Builds an atomic FES node representing a single comparison.
     */
    private fun buildAtomicFesNode(query: Query, field: ParcelKey): ComparisonOpsType {
        val content = query.content
        val reference = field.reference

        require(content != null && reference != null && content.sameAs(field.type)) {
            "Invalid query: $query; its content and reference must not be null and the content type must be the same as the field type"
        }
        when (content) {
            is QueryContentHolder.RANGE -> {
                val range = content.range
                require(range != null && !(range.first > range.second || range.first < 0.0 || (range.first == 0.0 && range.second == 0.0))) {
                    "Invalid range: $range; it must not be null and must satisfy 0 <= first <= second"
                }
                return PropertyIsBetween(
                    valueReference = ValueReference("${configurationBundle.nsPrefix}:${reference}"),
                    lowerBoundary = LowerBoundary(Literal(range.first.toString())),
                    upperBoundary = UpperBoundary(Literal(range.second.toString()))
                )
            }

            is QueryContentHolder.STRING -> {
                val string = content.string
                require(string != null && string.isNotBlank()) {
                    "Invalid string: $string; it must not be null and must not be blank"
                }
                return PropertyIsEqualTo(
                    valueReference = ValueReference("${configurationBundle.nsPrefix}:${reference}"),
                    literal = Literal(string.uppercase())
                )
            }

            is QueryContentHolder.BOOLEAN -> {
                val boolean = content.boolean
                require(boolean != null) {
                    "Invalid boolean: $boolean; it must not be null"
                }
                return PropertyIsEqualTo(
                    valueReference = ValueReference("${configurationBundle.nsPrefix}:${reference}"),
                    literal = Literal(boolean.toString())
                )
            }
        }
    }

    /**
     * Pass 1: Iterates through the input list and groups consecutive elements connected by AND.
     *
     * @return A list of FESNode structures. Each node is either an atomic condition
     * or an <fes:And> group, and they are all intended to be OR'ed together.
     */
    private fun groupAnds(queries: List<Query>, options: ParcelServiceOptions): List<AbstractOperatorType> {
        if (queries.isEmpty()) return emptyList()

        val orGroupOperands = mutableListOf<AbstractOperatorType>()
        // currentAndGroup holds the atomic nodes for the currently accumulating AND sequence
        var currentAndGroup = mutableListOf<AbstractOperatorType>()

        for (query in queries) {
            val field = options.keys.find { it.identifier == query.field?.identifier }
            query.content
            field?.reference

            if (field == null) {
                //there is no backing field for this query
                // we do not throw an error here, instead wi silently continue
                continue
            }

            val atomicNode = buildAtomicFesNode(query, field)

            if (query.method == QueryMethod.AND) {
                // Found an AND: Add the current atomic condition to the active group.
                currentAndGroup.add(atomicNode)
            } else { // operator is OR or null (start of expression)
                // 1. Finalize and save the previous AND group (if it existed)
                if (currentAndGroup.isNotEmpty()) {
                    val combinedNode = when (currentAndGroup.size) {
                        1 -> currentAndGroup.first() // If only one, it's a standalone operand
                        else -> And.fromList(currentAndGroup)
                    }
                    orGroupOperands.add(combinedNode)
                }

                // 2. Start a NEW group with the current atomic node.
                // This node is the first element of a new logical sequence (starting after an OR).
                currentAndGroup = mutableListOf(atomicNode)
            }
        }

        // 3. CRITICAL: Handle the last group after the loop finishes.
        if (currentAndGroup.isNotEmpty()) {
            val combinedNode = when (currentAndGroup.size) {
                1 -> currentAndGroup.first()
                else -> And.fromList(currentAndGroup)
            }
            orGroupOperands.add(combinedNode)
        }

        return orGroupOperands
    }

    /**
     * Pass 2: Takes the list of AND-grouped operands and combines them with a top-level OR,
     * or returns the single node if only one group was found.
     */
    private fun buildFesFilter(queries: List<Query>, options: ParcelServiceOptions): AbstractOperatorType? {
        val orGroupOperands = groupAnds(queries, options)

        return when (orGroupOperands.size) {
            0 -> null
            1 -> orGroupOperands.first() // Simple filter, no top-level And/Or needed
            else -> Or.fromList(orGroupOperands) // Combined by OR
        }
    }

    private fun Geometry.translate(from: String, to: String): Geometry = when (this) {
        is GeometryCollection<*> -> TODO("Converting from a GeometryCollection is not yet supported!")

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