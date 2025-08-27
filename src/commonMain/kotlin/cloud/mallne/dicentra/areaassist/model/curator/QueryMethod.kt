package cloud.mallne.dicentra.areaassist.model.curator

enum class QueryMethod(val urlParam: String) {
    AND("AND"), OR("OR");

    fun toggle() = when (this) {
        AND -> OR
        OR -> AND
    }
}