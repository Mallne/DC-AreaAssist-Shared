package cloud.mallne.dicentra.areaassist.model.curator

import kotlinx.serialization.Serializable

@Serializable
data class ParcelKeyProxy(
    val identifier: String
) {
    override fun toString(): String = identifier
}