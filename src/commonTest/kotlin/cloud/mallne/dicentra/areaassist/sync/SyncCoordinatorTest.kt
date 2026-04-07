package cloud.mallne.dicentra.areaassist.sync

import cloud.mallne.dicentra.areaassist.model.sync.RejectedPacket
import cloud.mallne.dicentra.areaassist.model.sync.RejectionReason
import cloud.mallne.dicentra.areaassist.model.sync.SyncAggregatePaging
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncResult
import cloud.mallne.dicentra.areaassist.model.sync.SyncState
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class SyncCoordinatorTest {

    @Test
    fun `incremental sync uploads dangling packets`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)
        val danglingPacket = SyncPacket.SettingPacket("dangling1", JsonPrimitive("val"))

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = listOf(danglingPacket)
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            uploadRejections = emptyList()
        )

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(1, result.uploaded)
        assertEquals(0, result.downloaded)
        assertEquals(0, result.deleted)
        assertEquals(danglingPacket, network.uploadedPackets.single())
    }

    @Test
    fun `incremental sync downloads new server entries`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1", "fp2" to "chk2")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)
        val serverEntry = fakeEntry("fp2", "chk2", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            downloadEntries = listOf(serverEntry)
        )

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(0, result.uploaded)
        assertEquals(1, result.downloaded)
        assertEquals(0, result.deleted)
        assertEquals("fp2", network.requestedFingerprints.single())
        assertEquals(serverEntry, storage.upsertedEntries.single())
    }

    @Test
    fun `incremental sync deletes locally stale entries`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val staleEntry = fakeEntry("fp1", "chk1", stale = true)

        val storage = FakeStorage(
            allEntries = listOf(staleEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(0, result.uploaded)
        assertEquals(0, result.downloaded)
        assertEquals(1, result.deleted)
        assertEquals("fp1", network.deletedFingerprints.single())
        assertEquals("fp1", storage.deletedEntryFingerprints.single())
    }

    @Test
    fun `incremental sync detects server-side deletions`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry1 = fakeEntry("fp1", "chk1", stale = false)
        val localEntry2 = fakeEntry("fp2", "chk2", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry1, localEntry2),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(1, result.deleted)
        assertEquals("fp2", storage.deletedEntryFingerprints.single())
    }

    @Test
    fun `incremental sync does nothing when in sync`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(0, result.uploaded)
        assertEquals(0, result.downloaded)
        assertEquals(0, result.deleted)
        assertEquals(0, network.uploadedPackets.size)
        assertEquals(0, network.requestedFingerprints.size)
    }

    @Test
    fun `overwrite local downloads all server entries`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1", "fp2" to "chk2")
        val serverEntry1 = fakeEntry("fp1", "chk1", stale = false)
        val serverEntry2 = fakeEntry("fp2", "chk2", stale = false)

        val storage = FakeStorage(
            allEntries = emptyList(),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            downloadEntries = listOf(serverEntry1, serverEntry2)
        )

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.OVERWRITE_LOCAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(0, result.uploaded)
        assertEquals(2, result.downloaded)
        assertEquals(2, network.requestedFingerprints.size)
    }

    @Test
    fun `overwrite local deletes entries not on server`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry1 = fakeEntry("fp1", "chk1", stale = false)
        val localEntry2 = fakeEntry("fp2", "chk2", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry1, localEntry2),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.OVERWRITE_LOCAL)

        assertIs<SyncResult.Success>(result)
        assertEquals(1, result.deleted)
        assertEquals("fp2", storage.deletedEntryFingerprints.single())
    }

    @Test
    fun `overwrite local skips upload phase`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)
        val danglingPacket = SyncPacket.SettingPacket("dangling1", JsonPrimitive("val"))

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = listOf(danglingPacket)
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.OVERWRITE_LOCAL)

        assertEquals(0, network.uploadedPackets.size)
    }

    @Test
    fun `overwrite server uploads all local packets`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val localEntry = fakeEntry("fp1", "chk1", stale = false)
        val danglingPacket = SyncPacket.SettingPacket("dangling1", JsonPrimitive("val"))

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = listOf(danglingPacket)
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.OVERWRITE_SERVER)

        assertIs<SyncResult.Success>(result)
        assertEquals(2, result.uploaded)
        assertEquals(0, result.downloaded)
        assertEquals(2, network.uploadedPackets.size)
    }

    @Test
    fun `overwrite server deletes server orphans`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1", "orphan" to "chk3")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test", SyncStrategy.OVERWRITE_SERVER)

        assertIs<SyncResult.Success>(result)
        assertEquals(1, result.deleted)
        assertEquals("orphan", network.deletedFingerprints.single())
    }

    @Test
    fun `overwrite server skips download phase`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val serverEntry = fakeEntry("fp1", "chk1", stale = false)

        val storage = FakeStorage(
            allEntries = emptyList(),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            downloadEntries = listOf(serverEntry)
        )

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.OVERWRITE_SERVER)

        assertEquals(0, network.requestedFingerprints.size)
    }

    @Test
    fun `permanent rejection deletes packet locally`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val danglingPacket = SyncPacket.SettingPacket("dangling1", JsonPrimitive("val"))
        val rejection = UploadRejection(
            packet = danglingPacket,
            rejection = RejectedPacket("dangling1", RejectionReason.SCOPE_MISMATCH)
        )

        val storage = FakeStorage(
            allEntries = emptyList(),
            danglingPackets = listOf(danglingPacket)
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            uploadRejections = listOf(rejection)
        )

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertEquals(danglingPacket, storage.deletedPackets.single())
    }

    @Test
    fun `transient rejection keeps packet locally`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val danglingPacket = SyncPacket.SettingPacket("dangling1", JsonPrimitive("val"))
        val rejection = UploadRejection(
            packet = danglingPacket,
            rejection = RejectedPacket("dangling1", RejectionReason.VERSION_CONFLICT)
        )

        val storage = FakeStorage(
            allEntries = emptyList(),
            danglingPackets = listOf(danglingPacket)
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            uploadRejections = listOf(rejection)
        )

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertEquals(0, storage.deletedPackets.size)
    }

    @Test
    fun `sync failure sets FAILED state`() = runTest {
        val storage = FakeStorage(
            allEntries = emptyList(),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = mapOf(),
            shouldThrow = true
        )

        val coordinator = SyncCoordinator(storage, network)
        val result = coordinator.sync("user:test")

        assertIs<SyncResult.Failure>(result)
        assertEquals(SyncState.FAILED, coordinator.state.value)
    }

    @Test
    fun `state ends in COMPLETED after successful sync`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertEquals(SyncState.COMPLETED, coordinator.state.value)
    }

    @Test
    fun `lastResult is set after successful sync`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val storage = FakeStorage(allEntries = emptyList(), danglingPackets = emptyList())
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test")

        assertIs<SyncResult.Success>(coordinator.lastResult.value)
    }

    @Test
    fun `reset clears state and lastResult`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val storage = FakeStorage(allEntries = emptyList(), danglingPackets = emptyList())
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test")
        coordinator.reset()

        assertEquals(SyncState.IDLE, coordinator.state.value)
        assertNull(coordinator.lastResult.value)
    }

    @Test
    fun `sync clears lastResult at start`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val storage = FakeStorage(allEntries = emptyList(), danglingPackets = emptyList())
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test")
        assertNotNull(coordinator.lastResult.value)

        val failingNetwork = FakeNetwork(aggregate = serverFingerprints, shouldThrow = true)
        val coordinator2 = SyncCoordinator(storage, failingNetwork)
        coordinator2.sync("user:test")

        assertIs<SyncResult.Failure>(coordinator2.lastResult.value)
    }

    @Test
    fun `overwrite server excludes stale entries from upload`() = runTest {
        val serverFingerprints = mapOf<String, String>()
        val staleEntry = fakeEntry("fp1", "chk1", stale = true)
        val validPacket = SyncPacket.SettingPacket("valid1", JsonPrimitive("val"))

        val storage = FakeStorage(
            allEntries = listOf(staleEntry),
            danglingPackets = listOf(validPacket)
        )
        val network = FakeNetwork(aggregate = serverFingerprints)

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.OVERWRITE_SERVER)

        assertEquals(listOf<SyncPacket>(validPacket), network.uploadedPackets)
    }

    @Test
    fun `download skips entries with matching checksum`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk1")
        val localEntry = fakeEntry("fp1", "chk1", stale = false)
        val serverEntry = fakeEntry("fp1", "chk1", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            downloadEntries = listOf(serverEntry)
        )

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertEquals(0, storage.upsertedEntries.size)
    }

    @Test
    fun `download updates entries with different checksum`() = runTest {
        val serverFingerprints = mapOf("fp1" to "chk2", "fp2" to "chk3")
        val localEntry = fakeEntry("fp2", "chk3", stale = false)
        val serverEntry1 = fakeEntry("fp1", "chk2", stale = false)
        val serverEntry2 = fakeEntry("fp2", "chk3", stale = false)

        val storage = FakeStorage(
            allEntries = listOf(localEntry),
            danglingPackets = emptyList()
        )
        val network = FakeNetwork(
            aggregate = serverFingerprints,
            downloadEntries = listOf(serverEntry1, serverEntry2)
        )

        val coordinator = SyncCoordinator(storage, network)
        coordinator.sync("user:test", SyncStrategy.INCREMENTAL)

        assertEquals(1, storage.upsertedEntries.size)
        assertEquals(serverEntry1, storage.upsertedEntries.single())
    }
}

private fun fakeEntry(fingerprint: String, checksum: String, stale: Boolean): SyncEntryDomain {
    return TestEntry(
        fingerprint = fingerprint,
        checksum = checksum,
        stale = stale
    )
}

private class TestEntry(
    override val fingerprint: String,
    override val checksum: String,
    private val stale: Boolean,
) : SyncEntryDomain {
    override val scope: String = "user:test"
    override val version: Int = 1
    override val created: Instant = Instant.fromEpochSeconds(0)
    override val updated: Instant = Instant.fromEpochSeconds(0)
    override val blame: String = "test"
    override val isManaged: Boolean = false
    override val packet: SyncPacket = SyncPacket.SettingPacket(fingerprint, JsonPrimitive("data"))
    override suspend fun isStale(): Boolean = stale
}

private class FakeStorage(
    private val allEntries: List<SyncEntryDomain>,
    private val danglingPackets: List<SyncPacket>,
) : SyncStorage {
    val upsertedEntries = mutableListOf<SyncEntryDomain>()
    val deletedEntryFingerprints = mutableListOf<String>()
    val deletedPackets = mutableListOf<SyncPacket>()

    override suspend fun getAllEntries(scope: String): List<SyncEntryDomain> = allEntries
    override suspend fun getEntry(fingerprint: String): SyncEntryDomain? =
        allEntries.find { it.fingerprint == fingerprint }

    override suspend fun getDanglingPackets(scope: String): List<SyncPacket> = danglingPackets
    override suspend fun upsertEntry(entry: SyncEntryDomain) {
        upsertedEntries.add(entry)
    }

    override suspend fun deleteEntry(fingerprint: String, propagate: Boolean) {
        deletedEntryFingerprints.add(fingerprint)
    }

    override suspend fun deletePacket(packet: SyncPacket) {
        deletedPackets.add(packet)
    }
}

private class FakeNetwork(
    private val aggregate: Map<String, String>,
    private val uploadRejections: List<UploadRejection> = emptyList(),
    private val downloadEntries: List<SyncEntryDomain> = emptyList(),
    private val shouldThrow: Boolean = false,
    val stateCollector: MutableList<SyncState>? = null,
) : SyncNetwork {
    val uploadedPackets = mutableListOf<SyncPacket>()
    val requestedFingerprints = mutableListOf<String>()
    val deletedFingerprints = mutableListOf<String>()

    override suspend fun getAggregate(scope: String): SyncAggregatePaging {
        if (shouldThrow) throw RuntimeException("Network error")
        return SyncAggregatePaging(scope, aggregate)
    }

    override suspend fun uploadPackets(scope: String, packets: List<SyncPacket>): List<UploadRejection> {
        uploadedPackets.addAll(packets)
        return uploadRejections.filter { r -> packets.any { it == r.packet } }
    }

    override suspend fun requestPackets(scope: String, fingerprints: List<String>): List<SyncEntryDomain> {
        requestedFingerprints.addAll(fingerprints)
        return downloadEntries.filter { it.fingerprint in fingerprints }
    }

    override suspend fun deletePackets(scope: String, fingerprints: List<String>) {
        deletedFingerprints.addAll(fingerprints)
    }
}
