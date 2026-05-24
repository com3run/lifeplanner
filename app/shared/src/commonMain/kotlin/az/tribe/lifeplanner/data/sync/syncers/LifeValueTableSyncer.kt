package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.LifeValueSyncDto
import az.tribe.lifeplanner.database.LifeValueEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import co.touchlab.kermit.Logger
import kotlin.time.Clock

class LifeValueTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<LifeValueEntity, LifeValueSyncDto>(supabase) {

    override val tableName = "life_values"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<LifeValueSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<LifeValueEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedLifeValues().executeAsList() }

    override suspend fun getDeletedLocal(): List<LifeValueEntity> =
        db { it.lifePlannerDBQueries.getDeletedLifeValues().executeAsList() }

    override suspend fun localToRemote(local: LifeValueEntity, userId: String) = LifeValueSyncDto(
        id = local.id,
        userId = userId,
        title = local.title,
        description = local.description,
        isActive = local.isActive != 0L,
        sortOrder = local.sortOrder,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: LifeValueSyncDto): LifeValueEntity = LifeValueEntity(
        id = remote.id,
        title = remote.title,
        description = remote.description,
        isActive = if (remote.isActive) 1L else 0L,
        sortOrder = remote.sortOrder,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: LifeValueEntity) {
        db { it.lifePlannerDBQueries.upsertLifeValueFromSync(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            isActive = entity.isActive,
            sortOrder = entity.sortOrder,
            sync_updated_at = entity.sync_updated_at,
            is_deleted = entity.is_deleted,
            sync_version = entity.sync_version,
            last_synced_at = entity.last_synced_at
        )}
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markLifeValueSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<LifeValueEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markLifeValueSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedLifeValues() }
    }

    override suspend fun getEntityId(entity: LifeValueEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_life_values")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_life_values", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()
        Logger.d("SyncEngine") { "Pull $tableName: userId=$userId, lastPull=$lastPull" }

        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<LifeValueSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<LifeValueSyncDto>()
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
        if (applied > 0) {
            Logger.d("SyncEngine") { "Applied $applied of ${remoteItems.size} pulled items from $tableName" }
        }
        return applied
    }

    /** Look up a local life value by ID for conflict comparison. Returns null if not found. */
    private suspend fun getLocalById(id: String): LifeValueEntity? = try {
        db { it.lifePlannerDBQueries.selectLifeValueById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
