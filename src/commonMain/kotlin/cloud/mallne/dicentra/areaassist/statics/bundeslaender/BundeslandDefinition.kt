package cloud.mallne.dicentra.areaassist.statics.bundeslaender

import cloud.mallne.dicentra.areaassist.model.Point

@Suppress( "PropertyName")
interface BundeslandDefinition {
    val roughBoundaries: List<Point<Double>>
    val iso3166_2: String
    val deBId: Int?
}