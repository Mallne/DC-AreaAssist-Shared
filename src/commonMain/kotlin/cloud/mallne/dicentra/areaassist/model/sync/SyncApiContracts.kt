package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SyncAggregatePaging(
    val scope: String,
    val entries: Map<String, String> //maps the Fingerprint to the checksum
)

@Serializable
data class SyncAggregateRequest(
    val scope: String,
    val entries: List<String>? //Fingerprint if null, all entries
)

@Serializable
data class SyncUploadRequest<Entry : SyncEntryMinimalDomain>(
    val scope: String,
    val packets: List<Entry>
)

@Serializable
data class SyncUploadResponse<Entry : SyncEntryDomain>(
    val accepted: List<Entry>,
    val rejected: List<RejectedPacket>,
    val timestamp: Instant
)

@Serializable
data class SyncDownloadResponse<Entry : SyncEntryDomain>(
    val packets: List<Entry>,
    val timestamp: Instant
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

    @SerialName("permission_error")
    PERMISSION_ERROR,

    @SerialName("server_error")
    SERVER_ERROR
}

@Serializable
data class RejectedPacket(
    @SerialName("packet_fingerprint")
    val packetFingerprint: String,
    val reason: RejectionReason
)
