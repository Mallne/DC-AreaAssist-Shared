package cloud.mallne.dicentra.areaassist.model.sync

import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import io.ktor.openapi.*
import kotlinx.serialization.Serializable

@Serializable
data class SyncServiceOptions(
    val defaultSyncIntervalSeconds: Long = 900L,
    val maxBatchSize: Int = 100,
) : InflatedServiceOptions {
    override fun usable(): ServiceOptions {
        return GenericElementWrapper(this, serializer())
    }
}
