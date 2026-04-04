package cloud.mallne.dicentra.areaassist.model.sync

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
    val isManaged: Boolean
}

interface SyncEntryBaseDomain : SyncEntryIdentityDomain, SyncEntryMetadata
interface SyncEntryMinimalDomain : SyncEntryIdentityDomain, SyncEntryDataDomain

interface SyncEntryDomain : SyncEntryMinimalDomain, SyncEntryBaseDomain
