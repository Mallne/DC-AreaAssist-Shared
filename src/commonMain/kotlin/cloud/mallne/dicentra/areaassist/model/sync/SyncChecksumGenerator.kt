package cloud.mallne.dicentra.areaassist.model.sync

/**
 * Interface for computing checksums from sync packets.
 * Implementations should produce deterministic, collision-resistant hashes.
 */
interface SyncChecksumGenerator {
    fun compute(packet: SyncPacket): String
}