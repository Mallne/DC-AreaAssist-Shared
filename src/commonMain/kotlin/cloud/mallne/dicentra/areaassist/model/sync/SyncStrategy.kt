package cloud.mallne.dicentra.areaassist.model.sync

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