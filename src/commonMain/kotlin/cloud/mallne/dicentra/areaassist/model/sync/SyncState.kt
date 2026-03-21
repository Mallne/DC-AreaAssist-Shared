package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncState {
    @SerialName("idle")
    IDLE,

    @SerialName("checking")
    CHECKING,

    @SerialName("uploading")
    UPLOADING,

    @SerialName("downloading")
    DOWNLOADING,

    @SerialName("resolving")
    RESOLVING,

    @SerialName("completed")
    COMPLETED,

    @SerialName("failed")
    FAILED
}

@Serializable
sealed class SyncResult {
    @Serializable
    data class Success(
        val scope: String,
        val uploaded: Int,
        val downloaded: Int,
        val conflicts: Int,
        val timestamp: Long
    ) : SyncResult()

    @Serializable
    data class Failure(
        val scope: String,
        val reason: String,
        val recoverable: Boolean
    ) : SyncResult()
}
