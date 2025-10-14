package cloud.mallne.dicentra.areaassist.model.parcel.domain

import cloud.mallne.geokit.geojson.Feature
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface ParcelDomain {
    val parcelId: String
    val origin: String
    val json: Feature

    @OptIn(ExperimentalTime::class)
    val fetchDate: Instant

    companion object
}