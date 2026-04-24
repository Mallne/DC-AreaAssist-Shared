package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.Serializable
import kotlin.time.Instant

interface SyncEntryIdentityDomain {
    val scope: String
    val fingerprint: String
}

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

interface SyncEntryBaseDomain : SyncEntryIdentityDomain, SyncEntryMetadata
interface SyncEntryMinimalDomain : SyncEntryIdentityDomain, SyncEntryDataDomain

interface SyncEntryDomain : SyncEntryMinimalDomain, SyncEntryBaseDomain

@Serializable
data class SyncEntryMinimal(
    override val scope: String,
    override val fingerprint: String,
    override val packet: SyncPacket
) : SyncEntryMinimalDomain

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