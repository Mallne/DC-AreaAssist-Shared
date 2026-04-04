package cloud.mallne.dicentra.areaassist.model.sync

import cloud.mallne.dicentra.areaassist.model.SettingDomain
import cloud.mallne.dicentra.areaassist.model.parcel.KeyType
import cloud.mallne.dicentra.areaassist.model.parcel.TaskState
import cloud.mallne.dicentra.areaassist.model.parcel.UsageOwnership
import cloud.mallne.dicentra.areaassist.model.parcel.UsageType
import cloud.mallne.dicentra.areaassist.model.parcel.UsageUse
import cloud.mallne.dicentra.areaassist.model.parcel.domain.LandUsageDomain
import cloud.mallne.dicentra.areaassist.model.parcel.domain.LandmarkDomain
import cloud.mallne.dicentra.areaassist.model.parcel.domain.NoteDomain
import cloud.mallne.dicentra.areaassist.model.parcel.domain.ParcelPropertyDomain
import cloud.mallne.dicentra.areaassist.model.parcel.domain.TaskDomain
import cloud.mallne.geokit.Vertex
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant

@Serializable
sealed interface SyncPacket {

    @Serializable
    data class ParcelLink(val forParcel: String, val serviceHint: String?)

    @Serializable
    sealed interface ParcelSpecificSyncPacket : SyncPacket {
        val link: ParcelLink
    }

    // A packet can only be synced if it can be edited. if the SourceSet of the OpenData Source includes the managed
    // set or does not define the type at all, it should fail or be skipped. That's why there is no root Parcel Sync
    // Packet, it needs to be fetched from Source every time to avoid Duplicates. vice versa The update of an OpenData
    // Source will result in a compaction operation of all synced Features linked to that specific definition.
    // this operation can be very expensive

    @Serializable
    data class ParcelPropertyPacket(
        override val link: ParcelLink,
        override val key: String,
        override val value: JsonElement,
        override val type: KeyType,
        override val updated: Instant
    ) : ParcelPropertyDomain, ParcelSpecificSyncPacket

    @Serializable
    data class NotePacket(
        override val link: ParcelLink,
        override val note: String,
    ) : NoteDomain, ParcelSpecificSyncPacket

    @Serializable
    data class LandUsagePacket(
        override val link: ParcelLink,
        override val area: Double,
        override val type: UsageType,
        override val ownership: UsageOwnership,
        override val use: UsageUse,
    ) : LandUsageDomain, ParcelSpecificSyncPacket

    //special non exclusively Linked Types
    @Serializable
    data class LandmarkPacket(
        val linkedParcels: Set<ParcelLink>,
        override val point: Vertex,
    ) : LandmarkDomain, SyncPacket

    @Serializable
    data class TaskPacket(
        val linkedParcels: Set<ParcelLink>,
        override val title: String,
        override val description: String?,
        override val dueDate: Instant?,
        override val taskState: TaskState,
    ) : TaskDomain, SyncPacket

    @Serializable
    data class SettingPacket(
        override val key: String,
        override val value: JsonElement
    ) : SettingDomain, SyncPacket
}