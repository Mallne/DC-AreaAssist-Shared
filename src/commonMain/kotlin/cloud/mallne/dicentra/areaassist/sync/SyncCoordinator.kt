package cloud.mallne.dicentra.areaassist.sync

import cloud.mallne.dicentra.areaassist.model.sync.RejectedPacket
import cloud.mallne.dicentra.areaassist.model.sync.RejectionReason
import cloud.mallne.dicentra.areaassist.model.sync.SyncAggregatePaging
import cloud.mallne.dicentra.areaassist.model.sync.SyncEntryDomain
import cloud.mallne.dicentra.areaassist.model.sync.SyncPacket
import cloud.mallne.dicentra.areaassist.model.sync.SyncResult
import cloud.mallne.dicentra.areaassist.model.sync.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Orchestrates the sync process between local storage and server.
 *
 * Uses a client-server model where the server is the golden truth for sync metadata
 * (fingerprints and checksums). Local storage maintains both the actual data packets
 * and sync state entries that link packets to their server-assigned fingerprints.
 *
 * Thread-safety: This class is not thread-safe. Call [sync] from a single coroutine
 * or ensure mutual exclusion.
 */
class SyncCoordinator(
    private val storage: SyncStorage,
    private val network: SyncNetwork,
) {
    private val _state = MutableStateFlow<SyncState>(SyncState.IDLE)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _lastResult = MutableStateFlow<SyncResult?>(null)
    val lastResult: StateFlow<SyncResult?> = _lastResult.asStateFlow()

    /**
     * Performs a sync operation for the given scope.
     *
     * State transitions: CHECKING → UPLOADING → DOWNLOADING → RESOLVING → COMPLETED (or FAILED)
     *
     * @param scope The sync scope (e.g., "user:john")
     * @param strategy The sync strategy to use
     * @return The sync result with counts of uploaded, downloaded, and deleted packets
     */
    suspend fun sync(scope: String, strategy: SyncStrategy = SyncStrategy.INCREMENTAL): SyncResult {
        _state.value = SyncState.CHECKING
        _lastResult.value = null

        return try {
            doSync(scope, strategy)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _state.value = SyncState.FAILED
            val failure = SyncResult.Failure(
                scope = scope,
                reason = e.message ?: "Unknown error",
                recoverable = e !is IllegalStateException
            )
            _lastResult.value = failure
            failure
        }
    }

    private suspend fun doSync(scope: String, strategy: SyncStrategy): SyncResult {
        val localEntries = storage.getAllEntries(scope)

        val deletedLocallyFingerprints = localEntries
            .filter { it.isStale() }
            .map { it.fingerprint }
            .toSet()
        val syncedFingerprints = localEntries.map { it.fingerprint }.toSet() - deletedLocallyFingerprints

        val dangling = storage.getDanglingPackets(scope)

        var uploaded = 0
        var downloaded = 0
        var deleted = 0

        _state.value = SyncState.UPLOADING
        if (strategy != SyncStrategy.OVERWRITE_LOCAL) {
            val packetsToUpload = when (strategy) {
                SyncStrategy.INCREMENTAL -> dangling
                SyncStrategy.OVERWRITE_SERVER -> {
                    val validPackets = localEntries
                        .filterNot { it.fingerprint in deletedLocallyFingerprints }
                        .map { it.packet }
                    dangling + validPackets
                }
            }
            if (packetsToUpload.isNotEmpty()) {
                val rejections = network.uploadPackets(scope, packetsToUpload)
                for (rejection in rejections) {
                    if (rejection.rejection.reason.isPermanent) {
                        storage.deletePacket(rejection.packet)
                    }
                }
                uploaded = packetsToUpload.size - rejections.size
            }
        }

        val postUploadServerFingerprints = network.getAggregate(scope).entries.keys

        _state.value = SyncState.DOWNLOADING
        if (strategy != SyncStrategy.OVERWRITE_SERVER) {
            val fingerprints = when (strategy) {
                SyncStrategy.INCREMENTAL -> (postUploadServerFingerprints - syncedFingerprints).toList()
                SyncStrategy.OVERWRITE_LOCAL -> postUploadServerFingerprints.toList()
            }
            if (fingerprints.isNotEmpty()) {
                val entries = network.requestPackets(scope, fingerprints)
                for (entry in entries) {
                    val existing = storage.getEntry(entry.fingerprint)
                    if (existing == null || existing.checksum != entry.checksum) {
                        storage.upsertEntry(entry)
                        downloaded++
                    }
                }
            }
        }

        _state.value = SyncState.RESOLVING
        when (strategy) {
            SyncStrategy.INCREMENTAL -> {
                if (deletedLocallyFingerprints.isNotEmpty()) {
                    network.deletePackets(scope, deletedLocallyFingerprints.toList())
                    deletedLocallyFingerprints.forEach { storage.deleteEntry(it, propagate = true) }
                    deleted += deletedLocallyFingerprints.size
                }

                val serverDeletedLocally = syncedFingerprints - postUploadServerFingerprints
                serverDeletedLocally.forEach { storage.deleteEntry(it, propagate = true) }
                deleted += serverDeletedLocally.size
            }

            SyncStrategy.OVERWRITE_LOCAL -> {
                if (deletedLocallyFingerprints.isNotEmpty()) {
                    network.deletePackets(scope, deletedLocallyFingerprints.toList())
                }
                deletedLocallyFingerprints.forEach { storage.deleteEntry(it, propagate = true) }
                deleted += deletedLocallyFingerprints.size

                val toDeleteLocally = syncedFingerprints - postUploadServerFingerprints
                toDeleteLocally.forEach { storage.deleteEntry(it, propagate = true) }
                deleted += toDeleteLocally.size
            }

            SyncStrategy.OVERWRITE_SERVER -> {
                val toDeleteOnServer =
                    (postUploadServerFingerprints - syncedFingerprints - deletedLocallyFingerprints) + deletedLocallyFingerprints
                if (toDeleteOnServer.isNotEmpty()) {
                    network.deletePackets(scope, toDeleteOnServer.toList())
                    deleted += toDeleteOnServer.size
                }
                deletedLocallyFingerprints.forEach { storage.deleteEntry(it, propagate = true) }
            }
        }

        val successResult = SyncResult.Success(
            scope = scope,
            uploaded = uploaded,
            downloaded = downloaded,
            deleted = deleted,
            conflicts = 0,
            timestamp = Clock.System.now()
        )

        _state.value = SyncState.COMPLETED
        _lastResult.value = successResult
        return successResult
    }

    /** Resets the sync state to IDLE and clears the last result. */
    fun reset() {
        _state.value = SyncState.IDLE
        _lastResult.value = null
    }
}

/**
 * Defines the sync strategy for reconciling local and server state.
 *
 * - [INCREMENTAL]: Only syncs differences. Uploads new local packets, downloads new server packets,
 *   and propagates deletions in both directions. Use for regular background syncs.
 *
 * - [OVERWRITE_LOCAL]: Server wins. Downloads all server data and overwrites local state.
 *   Local-only entries are deleted. Use when local data is corrupted or user wants a fresh start.
 *
 * - [OVERWRITE_SERVER]: Client wins. Uploads all local data and overwrites server state.
 *   Server-only entries are deleted. Use when migrating data to a new server.
 */
sealed interface SyncStrategy {
    data object INCREMENTAL : SyncStrategy
    data object OVERWRITE_LOCAL : SyncStrategy
    data object OVERWRITE_SERVER : SyncStrategy
}

/**
 * Local storage operations for sync state and data packets.
 *
 * Implementations must handle the separation between sync state (fingerprint, checksum, version)
 * and the actual data packets. A packet can exist without sync state (dangling) and vice versa
 * (stale entries where the packet was deleted locally).
 */
interface SyncStorage {
    /**
     * Returns all sync entries for the given scope. Entries may reference packets
     * that have been deleted locally (stale entries). Use [SyncEntryDomain.isStale]
     * to check validity.
     */
    suspend fun getAllEntries(scope: String): List<SyncEntryDomain>

    /** Gets a local entry by its server-assigned fingerprint, or null if not found. */
    suspend fun getEntry(fingerprint: String): SyncEntryDomain?

    /** Gets all data packets that have not yet been synced (no associated sync entry). */
    suspend fun getDanglingPackets(scope: String): List<SyncPacket>

    /**
     * Persists or updates a synced entry (packet + server-assigned metadata).
     * The underlying packet data is stored alongside the sync state.
     */
    suspend fun upsertEntry(entry: SyncEntryDomain)

    /**
     * Deletes the sync entry for the given fingerprint.
     * If [propagate] is true, also deletes the underlying packet data.
     */
    suspend fun deleteEntry(fingerprint: String, propagate: Boolean = false)

    /**
     * Deletes only the underlying packet data. The sync entry (metadata) is preserved.
     * Use this to mark a packet as stale without losing its sync state.
     */
    suspend fun deletePacket(packet: SyncPacket)
}

/**
 * Network operations for communicating with the sync server.
 *
 * The server is the golden truth for sync metadata. All fingerprint assignment
 * and checksum validation happens server-side.
 */
interface SyncNetwork {
    /**
     * Returns the server-side fingerprint-to-checksum mapping for the scope.
     * The server is the authoritative source for which entries exist.
     */
    suspend fun getAggregate(scope: String): SyncAggregatePaging

    /**
     * Uploads packets to the server. The server assigns fingerprints to accepted packets.
     * Returns only the rejected packets with their rejection reasons.
     */
    suspend fun uploadPackets(scope: String, packets: List<SyncPacket>): List<UploadRejection>

    /**
     * Requests full entries for the given fingerprints from the server.
     * Returns entries with metadata populated by the server.
     */
    suspend fun requestPackets(scope: String, fingerprints: List<String>): List<SyncEntryDomain>

    /**
     * Notifies the server to delete the given packets.
     * The server will remove them from its aggregate.
     */
    suspend fun deletePackets(scope: String, fingerprints: List<String>)
}

/**
 * Represents a rejected packet upload with the reason for rejection.
 */
data class UploadRejection(
    val packet: SyncPacket,
    val rejection: RejectedPacket
)

private val RejectionReason.isPermanent: Boolean
    get() = this != RejectionReason.VERSION_CONFLICT && this != RejectionReason.SERVER_ERROR

/**
 * Interface for computing checksums from sync packets.
 * Implementations should produce deterministic, collision-resistant hashes.
 */
interface SyncChecksumGenerator {
    fun compute(packet: SyncPacket): String
}

/**
 * Interface for generating unique fingerprints for sync entries.
 * Fingerprints are assigned by the server.
 */
interface SyncFingerprintGenerator {
    fun generate(scope: String, checksum: String): String
}

object UuidSyncFingerprintGenerator : SyncFingerprintGenerator {
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    override fun generate(scope: String, checksum: String): String = Uuid.random().toString()
}

/**
 * Server-side endpoint interface for handling sync operations.
 * The server uses this to process incoming sync requests from clients.
 */
interface SyncEndpoint {
    suspend fun upload(scope: String, packets: List<SyncPacket>): List<AcceptedPacket>
    suspend fun download(scope: String, fingerprints: List<String>): List<SyncEntryDomain>
    suspend fun delete(scope: String, fingerprints: List<String>)
}

/**
 * Metadata for an accepted packet upload, assigned by the server.
 */
data class AcceptedPacket(
    val fingerprint: String,
    val checksum: String,
    val version: Int,
    val created: kotlin.time.Instant,
    val updated: kotlin.time.Instant,
    val blame: String,
    val isManaged: Boolean
)

/**
 * Server-side orchestrator for processing sync requests.
 * Combines storage operations with endpoint responses.
 */
class ServerSyncOrchestrator(
    private val storage: SyncStorage,
    private val endpoint: SyncEndpoint,
    private val checksumGenerator: SyncChecksumGenerator,
    private val fingerprintGenerator: SyncFingerprintGenerator,
) {
    private val _state = MutableStateFlow<SyncState>(SyncState.IDLE)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _lastResult = MutableStateFlow<SyncResult?>(null)
    val lastResult: StateFlow<SyncResult?> = _lastResult.asStateFlow()

    suspend fun processUpload(scope: String, packets: List<SyncPacket>): UploadResult {
        _state.value = SyncState.UPLOADING

        val accepted = endpoint.upload(scope, packets)
        var uploaded = accepted.size
        var conflicts = 0

        for (entry in accepted) {
            val existing = storage.getEntry(entry.fingerprint)
            if (existing != null && existing.checksum != entry.checksum) {
                conflicts++
            }

            val syncEntry = object : SyncEntryDomain {
                override val scope: String = scope
                override val fingerprint: String = entry.fingerprint
                override val packet: SyncPacket
                    get() = packets.first { checksumGenerator.compute(it) == entry.checksum }
                override val checksum: String = entry.checksum
                override val version: Int = entry.version
                override val created: kotlin.time.Instant = entry.created
                override val updated: kotlin.time.Instant = entry.updated
                override val blame: String = entry.blame
                override val isManaged: Boolean = entry.isManaged
                override suspend fun isStale(): Boolean = false
            }
            storage.upsertEntry(syncEntry)
        }

        _state.value = SyncState.COMPLETED
        val result = SyncResult.Success(
            scope = scope,
            uploaded = uploaded,
            downloaded = 0,
            deleted = 0,
            conflicts = conflicts,
            timestamp = Clock.System.now()
        )
        _lastResult.value = result
        return UploadResult(accepted, emptyList())
    }

    suspend fun processDelete(scope: String, fingerprints: List<String>) {
        _state.value = SyncState.RESOLVING
        endpoint.delete(scope, fingerprints)
        fingerprints.forEach { storage.deleteEntry(it, propagate = true) }
        _state.value = SyncState.COMPLETED
    }

    fun reset() {
        _state.value = SyncState.IDLE
        _lastResult.value = null
    }
}

data class UploadResult(
    val accepted: List<AcceptedPacket>,
    val rejected: List<RejectedPacket>
)
