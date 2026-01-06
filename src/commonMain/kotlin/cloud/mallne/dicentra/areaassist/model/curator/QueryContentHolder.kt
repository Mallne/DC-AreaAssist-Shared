package cloud.mallne.dicentra.areaassist.model.curator

import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("content_type")
sealed class QueryContentHolder {

    @Serializable
    class STRING(val string: String?) : QueryContentHolder() {
        override fun toString(): String = string ?: ""
    }

    @Serializable
    class RANGE(val range: Pair<Double, Double>?) : QueryContentHolder() {
        override fun toString(): String = "${range?.first}..${range?.second}"
    }

    @Serializable
    class BOOLEAN(val boolean: Boolean?) : QueryContentHolder() {
        override fun toString(): String = "${boolean ?: false}"
    }

    fun sameAs(keyType: KeyType): Boolean {
        return when (keyType) {
            KeyType.STRING -> this is STRING
            KeyType.NUMBER -> this is RANGE
            KeyType.BOOLEAN -> this is BOOLEAN
            KeyType.NOTHING -> false
        }
    }

    companion object Parser
}
