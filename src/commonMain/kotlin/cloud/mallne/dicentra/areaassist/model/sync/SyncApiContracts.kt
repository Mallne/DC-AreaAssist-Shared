package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncUploadRequest(
    val scope: String,
    val packets: List<SyncPacket>
)

@Serializable
data class SyncUploadResponse(
    val accepted: List<String>,
    val rejected: List<RejectedPacket>,
    val timestamp: Long
)

@Serializable
data class SyncDownloadRequest(
    val scope: String,
    @SerialName("since_timestamp")
    val sinceTimestamp: Long?
)

@Serializable
data class SyncDownloadResponse(
    val packets: List<SyncPacket>,
    @SerialName("managed_packets")
    val managedPackets: List<ManagedPacket>,
    val timestamp: Long
)

@Serializable
enum class RejectionReason {
    @SerialName("version_conflict")
    VERSION_CONFLICT,

    @SerialName("scope_mismatch")
    SCOPE_MISMATCH,

    @SerialName("checksum_invalid")
    CHECKSUM_INVALID,

    @SerialName("type_not_allowed")
    TYPE_NOT_ALLOWED,

    @SerialName("managed_read_only")
    MANAGED_READ_ONLY,

    @SerialName("server_error")
    SERVER_ERROR
}

@Serializable
data class RejectedPacket(
    @SerialName("packet_id")
    val packetId: String,
    val reason: RejectionReason,
    @SerialName("server_version")
    val serverVersion: Int? = null
)
