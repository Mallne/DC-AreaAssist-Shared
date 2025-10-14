package cloud.mallne.dicentra.areaassist.model.parcel.domain

import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import kotlinx.serialization.json.JsonElement
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface ParcelPropertyDomain {
    val key: String
    val value: JsonElement
    val type: KeyType

    @OptIn(ExperimentalTime::class)
    val updated: Instant
}