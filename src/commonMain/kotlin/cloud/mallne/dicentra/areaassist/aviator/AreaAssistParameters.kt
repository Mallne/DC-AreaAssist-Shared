package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.areaassist.model.curator.ParcelKeyProxy
import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.areaassist.model.curator.QueryContentHolder
import cloud.mallne.dicentra.areaassist.model.parcel.GenericJson
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelConstants
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelKey
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelCrateEntity
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelEntity
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelPropertyEntity
import cloud.mallne.dicentra.areaassist.model.parcel.domain.data.ParcelPropertyEntity.Companion.convert
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.geokit.Boundary
import cloud.mallne.geokit.geojson.Feature
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.ExperimentalTime

object AreaAssistParameters {
    const val BOUNDARY = "DCAA_BOUNDARY"
    fun boundary(boundary: Boundary): Pair<String, RequestParameter> = BOUNDARY to RequestParameter.Single(boundary)
    const val QUERIES = "DCAA_QUERIES"
    fun queries(queries: List<Query> = listOf()): Pair<String, RequestParameter> =
        QUERIES to RequestParameter.Multi(queries)

    fun parcelId(parcelId: String): Pair<String, RequestParameter> =
        QUERIES to RequestParameter.Multi(
            listOf(
                parcelIdQuery(parcelId),
            )
        )

    fun parcelIdQuery(parcelId: String): Query =
        Query(
            field = ParcelKeyProxy(ParcelConstants.DefaultKeys.PARCELID.identifier),
            content = QueryContentHolder.STRING(parcelId)
        )

    const val RETURN_GEOMETRY = "DCAA_RETURN_GEOMETRY"
    fun returnGeometry(returnGeometry: Boolean = true): Pair<String, RequestParameter> =
        RETURN_GEOMETRY to RequestParameter.Single(returnGeometry)

    fun parameters(
        queries: List<Query> = listOf(),
        returnGeometry: Boolean = true,
        boundary: Boundary? = null
    ): RequestParameters {
        val m = mutableMapOf(
            queries(queries),
            returnGeometry(returnGeometry),
        )
        if (boundary != null) {
            m += boundary(boundary)
        }
        return RequestParameters(m)
    }

    @OptIn(ExperimentalTime::class)
    fun inflateParcelFromFeature(
        feature: Feature,
        keys: List<ParcelKey>,
        origin: String,
        mode: InflationMode = InflationMode.Auto,
        json: Json = Serialization(),
    ): ParcelCrateEntity? {
        val fields = keys.filter { it.reference != null }
        val kvs = mutableListOf<ParcelPropertyEntity>()
        val props = mutableMapOf<String, JsonElement>()
        var parcelId: String? = null
        for (field in fields) {
            //Convert the K/V GoeJson Properties to the known Format
            val value = when (mode) {
                InflationMode.Auto -> feature.properties[field.identifier] ?: feature.properties[field.reference]
                InflationMode.Reference -> feature.properties[field.reference]
                InflationMode.Identifier -> feature.properties[field.identifier]
            }
            if (value != null) {
                val parcelProperty = field.toParcelProperty()
                if (parcelProperty != null) {
                    val kv = parcelProperty.convert(value)
                    kvs.add(kv)
                    //separatly extract the parcel Id
                    if (field.identifier == ParcelConstants.DefaultKeys.PARCELID.identifier) {
                        parcelId = Serialization().decodeFromJsonElement<String?>(value)
                    }
                }
                props[field.identifier] = value
            }
        }
        props[GenericJson.ORIGIN] = json.encodeToJsonElement(origin)
        val converted = feature.copy(
            properties = props
        )
        return if (parcelId != null) {
            ParcelCrateEntity(
                parcel = ParcelEntity(
                    parcelId = parcelId,
                    origin = origin,
                    json = converted,
                ),
                properties = kvs
            )
        } else null
    }

    enum class InflationMode() {
        Auto, Identifier, Reference
    }
}