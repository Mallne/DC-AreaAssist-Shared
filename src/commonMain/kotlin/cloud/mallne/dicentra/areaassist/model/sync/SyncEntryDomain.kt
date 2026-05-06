package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.Serializable
import kotlin.time.Instant

interface SyncEntryUniqueIdentityDomain {
    val fingerprint: String
}

interface SyncEntryCommonIdentityDomain {
    val scope: String
}

interface SyncEntryIdentityDomain : SyncEntryUniqueIdentityDomain, SyncEntryCommonIdentityDomain

interface SyncEntryDataDomain {
    val packet: SyncPacket
}

// The metadata is constructed on the Server. The Server is the Source of Truth, for this
interface SyncEntryMetadata {
    val checksum: String
    val version: Int
    val created: Instant
    val updated: Instant
    val blame: String
}

interface AutogenerateSyncEntry : SyncEntryMetadata, SyncEntryDataDomain, SyncEntryCommonIdentityDomain

interface SyncEntryBaseDomain : SyncEntryIdentityDomain, SyncEntryMetadata
interface SyncEntryMinimalDomain : SyncEntryIdentityDomain, SyncEntryDataDomain

interface SyncEntryDomain : SyncEntryMinimalDomain, SyncEntryBaseDomain

@Serializable
data class NewSyncEntry(
    override val checksum: String,
    override val version: Int,
    override val created: Instant,
    override val updated: Instant,
    override val blame: String,
    override val packet: SyncPacket,
    override val scope: String
) : AutogenerateSyncEntry

@Serializable
data class SyncEntryFull(
    override val scope: String,
    override val fingerprint: String,
    override val packet: SyncPacket,
    override val checksum: String,
    override val version: Int,
    override val created: Instant,
    override val updated: Instant,
    override val blame: String
) : SyncEntryDomain