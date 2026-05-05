package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
sealed class SyncResult {
    @OptIn(ExperimentalTime::class)
    @Serializable
    data class Success(
        val scope: String,
        val uploaded: Int,
        val downloaded: Int,
        val deleted: Int,
        val conflicts: Int,
        val timestamp: Instant = Clock.System.now()
    ) : SyncResult()

    @Serializable
    data class Failure(
        val scope: String,
        val reason: String,
        val recoverable: Boolean
    ) : SyncResult()
}