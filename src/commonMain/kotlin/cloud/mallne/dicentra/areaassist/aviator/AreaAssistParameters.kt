package cloud.mallne.dicentra.areaassist.aviator

import cloud.mallne.dicentra.areaassist.model.curator.Query
import cloud.mallne.dicentra.aviator.core.execution.RequestParameter
import cloud.mallne.dicentra.aviator.core.execution.RequestParameters
import cloud.mallne.geokit.Boundary

object AreaAssistParameters {
    const val BOUNDARY = "DCAA_BOUNDARY"
    fun boundary(boundary: Boundary): Pair<String, RequestParameter> = BOUNDARY to RequestParameter.Single(boundary)
    const val QUERIES = "DCAA_QUERIES"
    fun queries(queries: List<Query> = listOf()): Pair<String, RequestParameter> =
        QUERIES to RequestParameter.Multi(queries)

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
}