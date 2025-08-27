package cloud.mallne.dicentra.areaassist.model.curator

import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import kotlinx.serialization.Serializable

@Serializable
sealed class QueryContentHolder() {

    @Serializable
    class STRING(val string: String?) : QueryContentHolder()

    @Serializable
    class RANGE(val range: Pair<Double, Double>?) : QueryContentHolder()

    @Serializable
    class BOOLEAN(val boolean: Boolean?) : QueryContentHolder()

    fun sameAs(keyType: KeyType): Boolean {
        return when (keyType) {
            KeyType.String -> this is STRING
            KeyType.Number -> this is RANGE
            KeyType.Boolean -> this is BOOLEAN
            KeyType.Nothing -> false
        }
    }

    companion object Parser
}
