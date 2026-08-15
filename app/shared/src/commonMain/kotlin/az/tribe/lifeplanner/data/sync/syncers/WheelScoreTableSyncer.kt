package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.WheelScoreSyncDto
import az.tribe.lifeplanner.database.WheelScoreEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock

class WheelScoreTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<WheelScoreEntity, WheelScoreSyncDto>(supabase) {

    override val tableName = "wheel_scores"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<WheelScoreSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<WheelScoreEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedWheelScores().executeAsList() }

    override suspend fun getDeletedLocal(): List<WheelScoreEntity> =
        db { it.lifePlannerDBQueries.getDeletedWheelScores().executeAsList() }

    override suspend fun localToRemote(local: WheelScoreEntity, userId: String) = WheelScoreSyncDto(
        id = local.id,
        userId = userId,
        score = local.score,
        assessedAt = local.assessedAt,
        note = local.note,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: WheelScoreSyncDto): WheelScoreEntity = WheelScoreEntity(
        id = remote.id,
        score = remote.score,
        assessedAt = remote.assessedAt,
        note = remote.note,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: WheelScoreEntity) {
        db {
            it.lifePlannerDBQueries.upsertWheelScoreFromSync(
                id = entity.id,
                score = entity.score,
                assessedAt = entity.assessedAt,
                note = entity.note,
                sync_updated_at = entity.sync_updated_at,
                is_deleted = entity.is_deleted,
                sync_version = entity.sync_version,
                last_synced_at = entity.last_synced_at
            )
        }
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markWheelScoreSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<WheelScoreEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markWheelScoreSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedWheelScores() }
    }

    override suspend fun getEntityId(entity: WheelScoreEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_wheel_scores")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_wheel_scores", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()

        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<WheelScoreSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<WheelScoreSyncDto>()
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

    private suspend fun getLocalById(id: String): WheelScoreEntity? = try {
        db { it.lifePlannerDBQueries.selectWheelScoreById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
