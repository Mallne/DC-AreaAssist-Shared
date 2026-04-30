package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SyncAggregateAttestation(
    val entries: List<AttestationEntry>
)

@Serializable
data class AttestationEntry(val fingerprint: String, val checksum: String, val scope: String)

typealias SyncAggregateRequest = List<String>? //Fingerprint if null, all entries

@Serializable
data class SyncAggregateResponse<Entry : SyncEntryDomain>(
    val entries: List<Entry>,
    val timestamp: Instant
)

@Serializable
data class SyncUploadResponse<Entry : SyncEntryDomain>(
    val accepted: Map<String, Entry>,
    val rejected: Map<String, RejectionReason>,
    val timestamp: Instant
)

@Serializable
data class SyncDeleteResponse<Entry : SyncEntryDomain>(
    val accepted: List<String>,
    val rejected: List<RejectedPacket<Entry>>,
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
data class RejectedPacket<Entry : SyncEntryDomain>(
    @SerialName("packet_fingerprint")
    val entry: Entry,
    val reason: RejectionReason
)
