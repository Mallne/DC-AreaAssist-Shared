package cloud.mallne.dicentra.areaassist.model.sync

import cloud.mallne.dicentra.areaassist.statics.Serialization
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class SyncServiceOptions(
    val defaultSyncIntervalSeconds: Long = 900L,
    val maxBatchSize: Int = 100,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)
}
