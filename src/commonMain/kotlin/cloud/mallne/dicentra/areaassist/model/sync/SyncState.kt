package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
