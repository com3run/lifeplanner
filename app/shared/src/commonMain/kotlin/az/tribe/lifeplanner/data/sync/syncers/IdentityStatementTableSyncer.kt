package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.IdentityStatementSyncDto
import az.tribe.lifeplanner.database.IdentityStatementEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock

class IdentityStatementTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<IdentityStatementEntity, IdentityStatementSyncDto>(supabase) {

    override val tableName = "identity_statements"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<IdentityStatementSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<IdentityStatementEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedIdentityStatements().executeAsList() }

    override suspend fun getDeletedLocal(): List<IdentityStatementEntity> =
        db { it.lifePlannerDBQueries.getDeletedIdentityStatements().executeAsList() }

    override suspend fun localToRemote(local: IdentityStatementEntity, userId: String) = IdentityStatementSyncDto(
        id = local.id,
        userId = userId,
        statement = local.statement,
        valueId = local.valueId,
        isActive = local.isActive != 0L,
        sortOrder = local.sortOrder,
        createdAt = local.createdAt,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: IdentityStatementSyncDto) = IdentityStatementEntity(
        id = remote.id,
        statement = remote.statement,
        valueId = remote.valueId,
        isActive = if (remote.isActive) 1L else 0L,
        sortOrder = remote.sortOrder,
        createdAt = remote.createdAt,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: IdentityStatementEntity) {
        db { it.lifePlannerDBQueries.upsertIdentityStatementFromSync(
            id = entity.id,
            statement = entity.statement,
            valueId = entity.valueId,
            isActive = entity.isActive,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt,
            sync_updated_at = entity.sync_updated_at,
            is_deleted = entity.is_deleted,
            sync_version = entity.sync_version,
            last_synced_at = entity.last_synced_at
        )}
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markIdentityStatementSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<IdentityStatementEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markIdentityStatementSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedIdentityStatements() }
    }

    override suspend fun getEntityId(entity: IdentityStatementEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_identity_statements")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_identity_statements", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()
        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<IdentityStatementSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<IdentityStatementSyncDto>()
        }
        var applied = 0
        remoteItems.forEach { remote ->
            val existing = getLocalById(remote.id)
            if (existing == null || remote.syncVersion >= existing.sync_version) {
                upsertLocal(remoteToLocal(remote))
                applied++
            }
        }
        setLastPullTimestamp(now)
        return applied
    }

    private suspend fun getLocalById(id: String): IdentityStatementEntity? = try {
        db { it.lifePlannerDBQueries.selectIdentityStatementById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
