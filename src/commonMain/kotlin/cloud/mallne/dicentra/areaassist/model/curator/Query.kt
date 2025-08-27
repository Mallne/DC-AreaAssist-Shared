package cloud.mallne.dicentra.areaassist.model.curator

import cloud.mallne.dicentra.areaassist.model.parcel.ParcelKey
import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Query @OptIn(ExperimentalUuidApi::class) constructor(
    val method: QueryMethod = QueryMethod.AND,
    val field: ParcelKeyProxy? = null,
    val content: QueryContentHolder? = null,
    val key: String = Uuid.random().toHexString(),
) {

    companion object {
        fun getEnabledFeatures(keys: List<List<ParcelKey>>): List<ParcelKey> {
            val all = mutableListOf<ParcelKey>()
            val requiredDups = keys.size
            val flat =
                keys.flatten().filter { it.type != KeyType.Nothing }
            val distincts = flat.distinctBy { it.identifier }
            for (key in distincts) {
                if (flat.count { it.identifier == key.identifier } == requiredDups) {
                    all.add(key)
                }
            }
            return all
        }
    }
}