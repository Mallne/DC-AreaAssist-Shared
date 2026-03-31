package cloud.mallne.dicentra.areaassist.model.sync

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import kotlinx.serialization.Serializable

@Serializable
data class SyncServiceOptions(
    val defaultSyncIntervalSeconds: Long = 900L,
    val maxBatchSize: Int = 100,
) : InflatedServiceOptions
