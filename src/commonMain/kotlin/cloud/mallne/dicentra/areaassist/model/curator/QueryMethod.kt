package cloud.mallne.dicentra.areaassist.model.curator

import kotlinx.serialization.Serializable

@Serializable
enum class QueryMethod(val urlParam: String) {
    AND("AND"), OR("OR");

    fun toggle() = when (this) {
        AND -> OR
        OR -> AND
    }
}