package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
enum class PacketType {
    @SerialName("parcel")
    PARCEL,

    @SerialName("setting")
    SETTING,

    @SerialName("contact_role")
    CONTACT_ROLE,

    @SerialName("role_mapping")
    ROLE_MAPPING,

    @SerialName("parcel_property")
    PARCEL_PROPERTY,

    @SerialName("task")
    TASK,

    @SerialName("note")
    NOTE,

    @SerialName("landmark")
    LANDMARK,

    @SerialName("land_usage")
    LAND_USAGE
}

@OptIn(ExperimentalTime::class)
@Serializable
data class SyncPacket(
    val scope: String,
    @SerialName("packet_id")
    val packetId: String,
    @SerialName("packet_type")
    val packetType: PacketType,
    @SerialName("is_managed")
    val isManaged: Boolean,
    val data: JsonElement,
    val checksum: String,
    val version: Int,
    val updated: Instant,
    val created: Instant
)

@OptIn(ExperimentalTime::class)
@Serializable
data class SyncMetadata(
    val scope: String,
    @SerialName("last_sync_timestamp")
    val lastSyncTimestamp: Instant?,
    @SerialName("sync_enabled")
    val syncEnabled: Boolean,
    @SerialName("enabled_packet_types")
    val enabledPacketTypes: Set<PacketType>,
    @SerialName("pending_uploads")
    val pendingUploads: Int,
    @SerialName("last_upload_timestamp")
    val lastUploadTimestamp: Instant?
)

@OptIn(ExperimentalTime::class)
@Serializable
data class ManagedPacket(
    val id: String,
    @SerialName("license_id")
    val licenseId: String,
    @SerialName("packet_type")
    val packetType: PacketType,
    @SerialName("is_enforced")
    val isEnforced: Boolean,
    val data: JsonElement,
    val version: Int,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("created_by")
    val createdBy: String
)
