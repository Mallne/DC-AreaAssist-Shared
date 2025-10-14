package cloud.mallne.dicentra.areaassist.model.parcel.domain.data

import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelDomain
import cloud.mallne.geokit.geojson.Feature
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class ParcelEntity @OptIn(ExperimentalTime::class) constructor(
    override val parcelId: String,
    override val origin: String,
    override val json: Feature,
    override val fetchDate: Instant = Clock.System.now(),
) : ParcelDomain