package cloud.mallne.dicentra.areaassist.sync

import cloud.mallne.dicentra.areaassist.model.sync.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class SyncCoordinatorTest {

    @Test
    fun `SyncState enum contains all expected states in order`() {
        val values = SyncState.entries
        assertEquals(7, values.size)
        assertEquals(SyncState.IDLE, values[0])
        assertEquals(SyncState.CHECKING, values[1])
        assertEquals(SyncState.UPLOADING, values[2])
        assertEquals(SyncState.DOWNLOADING, values[3])
        assertEquals(SyncState.RESOLVING, values[4])
        assertEquals(SyncState.COMPLETED, values[5])
        assertEquals(SyncState.FAILED, values[6])
    }

    @Test
    fun `SyncResult Success stores counters correctly`() {
        val now = Instant.fromEpochSeconds(1000)
        val result = SyncResult.Success(
            scope = "user:test",
            uploaded = 3,
            downloaded = 5,
            deleted = 2,
            conflicts = 0,
            timestamp = now
        )
        assertIs<SyncResult>(result)
        assertEquals("user:test", result.scope)
        assertEquals(3, result.uploaded)
        assertEquals(5, result.downloaded)
        assertEquals(2, result.deleted)
        assertEquals(0, result.conflicts)
        assertEquals(now, result.timestamp)
    }

    @Test
    fun `SyncResult Failure stores reason and recoverable flag`() {
        val result = SyncResult.Failure(
            scope = "user:test",
            reason = "Network error",
            recoverable = true
        )
        assertIs<SyncResult>(result)
        assertEquals("user:test", result.scope)
        assertEquals("Network error", result.reason)
        assertEquals(true, result.recoverable)
    }

    @Test
    fun `SyncStrategy has three variants`() {
        assertEquals(3, listOf(SyncStrategy.INCREMENTAL, SyncStrategy.OVERWRITE_LOCAL, SyncStrategy.OVERWRITE_SERVER).size)
        assertIs<SyncStrategy>(SyncStrategy.INCREMENTAL)
        assertIs<SyncStrategy>(SyncStrategy.OVERWRITE_LOCAL)
        assertIs<SyncStrategy>(SyncStrategy.OVERWRITE_SERVER)
    }

    @Test
    fun `SyncUpsert New holds packet and scope`() {
        val packet = SyncPacket.NotePacket(
            link = SyncPacket.ParcelLink("parcel-1", "test-service"),
            note = "test note"
        )
        val upsert = SyncUpsert.New(packet = packet, scope = "user:test")
        assertIs<SyncUpsert>(upsert)
        assertEquals(packet, upsert.packet)
        assertEquals("user:test", upsert.scope)
    }

    @Test
    fun `SyncUpsert Update holds fingerprint packet and scope`() {
        val packet = SyncPacket.NotePacket(
            link = SyncPacket.ParcelLink("parcel-1", "test-service"),
            note = "test note"
        )
        val upsert = SyncUpsert.Update(fingerprint = "fp-1", packet = packet, scope = "user:test")
        assertIs<SyncUpsert>(upsert)
        assertEquals("fp-1", upsert.fingerprint)
        assertEquals(packet, upsert.packet)
        assertEquals("user:test", upsert.scope)
    }

    @Test
    fun `SyncUpsert scope can be null`() {
        val packet = SyncPacket.NotePacket(
            link = SyncPacket.ParcelLink("parcel-1", "test-service"),
            note = "test note"
        )
        val upsert = SyncUpsert.New(packet = packet, scope = null)
        assertNull(upsert.scope)
    }

    @Test
    fun `SyncEntryFull stores all fields`() {
        val now = Instant.fromEpochSeconds(1000)
        val packet = SyncPacket.NotePacket(
            link = SyncPacket.ParcelLink("parcel-1", "test-service"),
            note = "sync test entry"
        )
        val entry = SyncEntryFull(
            scope = "user:test",
            fingerprint = "fp-1",
            packet = packet,
            checksum = "abc123",
            version = 1,
            created = now,
            updated = now,
            blame = "tester"
        )
        assertEquals("user:test", entry.scope)
        assertEquals("fp-1", entry.fingerprint)
        assertEquals(packet, entry.packet)
        assertEquals("abc123", entry.checksum)
        assertEquals(1, entry.version)
        assertEquals(now, entry.created)
        assertEquals(now, entry.updated)
        assertEquals("tester", entry.blame)
    }

    @Test
    fun `NewSyncEntry omits fingerprint`() {
        val now = Instant.fromEpochSeconds(1000)
        val packet = SyncPacket.NotePacket(
            link = SyncPacket.ParcelLink("parcel-1", "test-service"),
            note = "new upload test"
        )
        val entry = NewSyncEntry(
            checksum = "def456",
            version = 1,
            created = now,
            updated = now,
            blame = "tester",
            packet = packet,
            scope = "user:test"
        )
        assertEquals("def456", entry.checksum)
        assertEquals(1, entry.version)
        assertEquals("tester", entry.blame)
        assertEquals(packet, entry.packet)
        assertEquals("user:test", entry.scope)
        assertIs<AutogenerateSyncEntry>(entry)
    }

    @Test
    fun `SyncAggregateAttestation holds list of attestation entries`() {
        val attestations = SyncAggregateAttestation(
            entries = listOf(
                AttestationEntry(fingerprint = "fp-1", checksum = "chk-1", scope = "user:test"),
                AttestationEntry(fingerprint = "fp-2", checksum = "chk-2", scope = "user:test")
            )
        )
        assertEquals(2, attestations.entries.size)
        assertEquals("fp-1", attestations.entries[0].fingerprint)
        assertEquals("chk-1", attestations.entries[0].checksum)
        assertEquals("user:test", attestations.entries[0].scope)
    }

    @Test
    fun `SyncAggregateResponse holds entries and timestamp`() {
        val now = Instant.fromEpochSeconds(2000)
        val response = SyncAggregateResponse(entries = emptyList(), timestamp = now)
        assertEquals(0, response.entries.size)
        assertEquals(now, response.timestamp)
    }

    @Test
    fun `SyncUploadResponse distinguishes accepted from rejected`() {
        val now = Instant.fromEpochSeconds(3000)
        val response = SyncUploadResponse(
            accepted = emptyMap(),
            rejected = mapOf("k1" to RejectionReason.PERMISSION_ERROR),
            timestamp = now
        )
        assertEquals(0, response.accepted.size)
        assertEquals(1, response.rejected.size)
        assertEquals(RejectionReason.PERMISSION_ERROR, response.rejected["k1"])
    }

    @Test
    fun `SyncDeleteResponse distinguishes accepted from rejected`() {
        val now = Instant.fromEpochSeconds(4000)
        val entry = SyncEntryFull(
            scope = "user:test", fingerprint = "fp-1",
            packet = SyncPacket.NotePacket(SyncPacket.ParcelLink("p-1", "s"), "del"),
            checksum = "chk", version = 1,
            created = now, updated = now, blame = "t"
        )
        val response = SyncDeleteResponse(
            accepted = listOf("fp-1"),
            rejected = listOf(RejectedPacket(entry = entry, reason = RejectionReason.PERMISSION_ERROR)),
            timestamp = now
        )
        assertEquals(1, response.accepted.size)
        assertEquals("fp-1", response.accepted[0])
        assertEquals(1, response.rejected.size)
        assertEquals(RejectionReason.PERMISSION_ERROR, response.rejected[0].reason)
    }

    @Test
    fun `SyncDownloadResponse holds packets and timestamp`() {
        val now = Instant.fromEpochSeconds(5000)
        val response = SyncDownloadResponse(packets = emptyList(), timestamp = now)
        assertEquals(0, response.packets.size)
        assertEquals(now, response.timestamp)
    }

    @Test
    fun `RejectionReason has six values`() {
        val reasons = RejectionReason.entries
        assertEquals(6, reasons.size)
        assertEquals(RejectionReason.VERSION_CONFLICT, reasons[0])
        assertEquals(RejectionReason.SCOPE_MISMATCH, reasons[1])
        assertEquals(RejectionReason.CHECKSUM_INVALID, reasons[2])
        assertEquals(RejectionReason.TYPE_NOT_ALLOWED, reasons[3])
        assertEquals(RejectionReason.PERMISSION_ERROR, reasons[4])
        assertEquals(RejectionReason.SERVER_ERROR, reasons[5])
    }

    @Test
    fun `SyncServiceOptions has defaults`() {
        val options = SyncServiceOptions()
        assertEquals(900L, options.defaultSyncIntervalSeconds)
        assertEquals(100, options.maxBatchSize)
    }

    @Test
    fun `SyncPacket NotePacket stores link and note`() {
        val link = SyncPacket.ParcelLink("parcel-id", "service")
        val packet = SyncPacket.NotePacket(link = link, note = "hello")
        assertEquals(link, packet.link)
        assertEquals("hello", packet.note)
    }

    @Test
    fun `SyncPacket ParcelLink stores forParcel and serviceHint`() {
        val link = SyncPacket.ParcelLink("parcel-42", "wfs-service")
        assertEquals("parcel-42", link.forParcel)
        assertEquals("wfs-service", link.serviceHint)
    }

    @Test
    fun `SyncEntryFull implements SyncEntryDomain interface`() {
        val now = Instant.fromEpochSeconds(0)
        val entry = SyncEntryFull(
            scope = "user:test", fingerprint = "fp-1",
            packet = SyncPacket.NotePacket(SyncPacket.ParcelLink("p", "s"), "test"),
            checksum = "chk", version = 1,
            created = now, updated = now, blame = "t"
        )
        assertIs<SyncEntryDomain>(entry)
        assertIs<SyncEntryBaseDomain>(entry)
        assertIs<SyncEntryMinimalDomain>(entry)
    }
}
