package cloud.mallne.dicentra.areaassist.model.sync

import kotlin.uuid.Uuid

/**
 * Interface for generating unique fingerprints for sync entries.
 * Fingerprints are assigned by the server.
 */
interface SyncFingerprintGenerator {
    fun generate(scope: String, checksum: String): String

    companion object Builtin {
        object UuidSyncFingerprintGenerator : SyncFingerprintGenerator {
            @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
            override fun generate(scope: String, checksum: String): String = Uuid.random().toHexDashString()
        }
    }
}