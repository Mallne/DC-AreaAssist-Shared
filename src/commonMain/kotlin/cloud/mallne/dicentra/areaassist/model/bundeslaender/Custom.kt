package cloud.mallne.dicentra.areaassist.model.bundeslaender

import cloud.mallne.dicentra.areaassist.model.Point

internal object Custom : BundeslandDefinition {
    override val roughBoundaries: List<Point<Double>> = listOf()
    override val iso3166_2 = "CUSTOM"
    override val deBId = null
}
