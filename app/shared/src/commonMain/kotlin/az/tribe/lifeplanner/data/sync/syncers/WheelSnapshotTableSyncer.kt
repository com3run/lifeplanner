package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.WheelSnapshotSyncDto
import az.tribe.lifeplanner.database.WheelSnapshotEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock

/**
 * Wheel of Life history.
 *
 * Synced for the same reason the scores are: a new phone should not mean starting your history
 * again. Rows are keyed by date, so the same day arriving from two devices merges rather than
 * duplicating, and conflict resolution falls out of the usual sync_version comparison.
 */
class WheelSnapshotTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<WheelSnapshotEntity, WheelSnapshotSyncDto>(supabase) {

    override val tableName = "wheel_snapshots"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<WheelSnapshotSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<WheelSnapshotEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedWheelSnapshots().executeAsList() }

    override suspend fun getDeletedLocal(): List<WheelSnapshotEntity> =
        db { it.lifePlannerDBQueries.getDeletedWheelSnapshots().executeAsList() }

    override suspend fun localToRemote(local: WheelSnapshotEntity, userId: String) = WheelSnapshotSyncDto(
        id = local.id,
        userId = userId,
        scores = local.scores,
        capturedAt = local.capturedAt,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: WheelSnapshotSyncDto): WheelSnapshotEntity = WheelSnapshotEntity(
        id = remote.id,
        scores = remote.scores,
        capturedAt = remote.capturedAt,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: WheelSnapshotEntity) {
        db {
            it.lifePlannerDBQueries.upsertWheelSnapshotFromSync(
                id = entity.id,
                scores = entity.scores,
                capturedAt = entity.capturedAt,
                sync_updated_at = entity.sync_updated_at,
                is_deleted = entity.is_deleted,
                sync_version = entity.sync_version,
                last_synced_at = entity.last_synced_at
            )
        }
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markWheelSnapshotSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<WheelSnapshotEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markWheelSnapshotSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedWheelSnapshots() }
    }

    override suspend fun getEntityId(entity: WheelSnapshotEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_wheel_snapshots")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_wheel_snapshots", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()

        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<WheelSnapshotSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<WheelSnapshotSyncDto>()
        }

        var applied = 0
        remoteItems.forEach { remote ->
            val existingLocal = getLocalById(remote.id)
            if (existingLocal == null || remote.syncVersion >= existingLocal.sync_version) {
                upsertLocal(remoteToLocal(remote))
                applied++
            }
        }
        setLastPullTimestamp(now)
        return applied
    }

    private suspend fun getLocalById(id: String): WheelSnapshotEntity? = try {
        db { it.lifePlannerDBQueries.selectWheelSnapshotById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
