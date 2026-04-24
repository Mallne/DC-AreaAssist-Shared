package cloud.mallne.dicentra.areaassist.model.sync

import kotlinx.serialization.Serializable

@Serializable
sealed interface SyncUpsert {
    val packet: SyncPacket
    val scope: String?

    @Serializable
    data class Update(val fingerprint: String, override val packet: SyncPacket, override val scope: String?) :
        SyncUpsert

    @Serializable
    data class New(override val packet: SyncPacket, override val scope: String?) : SyncUpsert
}