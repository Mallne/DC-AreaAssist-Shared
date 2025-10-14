package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import cloud.mallne.dicentra.areaassist.model.parcel.ParcelPropertyKeys
import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelPropertyDomain
import cloud.mallne.dicentra.areaassist.statics.Serialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class ParcelPropertyEntity @OptIn(ExperimentalTime::class) constructor(
    override val key: String,
    override val value: JsonElement,
    override val type: KeyType,
    override val updated: Instant = Clock.System.now(),
) : ParcelPropertyDomain {
    companion object {
        @OptIn(ExperimentalTime::class)
        inline fun <reified T> ParcelPropertyKeys.construct(value: T): ParcelPropertyEntity = ParcelPropertyEntity(
            key = key,
            value = Serialization().encodeToJsonElement(value),
            type = type,
        )

        @OptIn(ExperimentalTime::class)
        fun ParcelPropertyKeys.convert(value: JsonElement): ParcelPropertyEntity = ParcelPropertyEntity(
            key = key,
            value = value,
            type = type,
        )
    }
}