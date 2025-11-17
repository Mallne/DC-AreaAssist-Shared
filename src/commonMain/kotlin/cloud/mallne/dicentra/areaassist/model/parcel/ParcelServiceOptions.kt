package cloud.mallne.dicentra.areaassist.model.parcel

import cloud.mallne.dicentra.areaassist.model.Point
import cloud.mallne.dicentra.areaassist.model.bundeslaender.Bundesland
import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.aviator.koas.info.License
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ParcelServiceOptions @OptIn(ExperimentalUuidApi::class) constructor(
    val bounds: List<Point<Double>> = listOf(),
    val minimalDefinition: Boolean = true,
    val canInferWithParcelId: Boolean = true,
    val correspondsTo: String = Bundesland.CUSTOM.iso3166_2,
    val parcelLinkReference: String = Uuid.random().toString(),
    val license: License? = null,
    val keys: List<ParcelKey> = listOf()

) : InflatedServiceOptions {

    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)
}