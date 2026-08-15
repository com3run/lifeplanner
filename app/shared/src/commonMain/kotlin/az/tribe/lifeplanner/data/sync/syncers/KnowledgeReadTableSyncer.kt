package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.KnowledgeReadSyncDto
import az.tribe.lifeplanner.database.KnowledgeReadEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import co.touchlab.kermit.Logger
import kotlin.time.Clock

class KnowledgeReadTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<KnowledgeReadEntity, KnowledgeReadSyncDto>(supabase) {

    override val tableName = "knowledge_reads"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<KnowledgeReadSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<KnowledgeReadEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedKnowledgeReads().executeAsList() }

    override suspend fun getDeletedLocal(): List<KnowledgeReadEntity> =
        db { it.lifePlannerDBQueries.getDeletedKnowledgeReads().executeAsList() }

    override suspend fun localToRemote(local: KnowledgeReadEntity, userId: String) = KnowledgeReadSyncDto(
        id = local.id,
        userId = userId,
        readAt = local.readAt,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: KnowledgeReadSyncDto): KnowledgeReadEntity = KnowledgeReadEntity(
        id = remote.id,
        readAt = remote.readAt,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: KnowledgeReadEntity) {
        db { it.lifePlannerDBQueries.upsertKnowledgeReadFromSync(
            id = entity.id, readAt = entity.readAt,
            sync_updated_at = entity.sync_updated_at, is_deleted = entity.is_deleted,
            sync_version = entity.sync_version, last_synced_at = entity.last_synced_at
        )}
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markKnowledgeReadSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<KnowledgeReadEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markKnowledgeReadSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedKnowledgeReads() }
    }

    override suspend fun getEntityId(entity: KnowledgeReadEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_knowledge_reads")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_knowledge_reads", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()
        Logger.d("SyncEngine") { "Pull $tableName: userId=$userId, lastPull=$lastPull" }

        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<KnowledgeReadSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<KnowledgeReadSyncDto>()
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

    private suspend fun getLocalById(id: String): KnowledgeReadEntity? = try {
        db { it.lifePlannerDBQueries.selectKnowledgeReadById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
