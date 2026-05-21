package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.UserSituationSyncDto
import az.tribe.lifeplanner.database.UserSituationEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock

class UserSituationTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<UserSituationEntity, UserSituationSyncDto>(supabase) {

    override val tableName = "user_situations"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<UserSituationSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<UserSituationEntity> {
        return db { it.lifePlannerDBQueries.getUnsyncedUserSituations().executeAsList() }
    }

    override suspend fun getDeletedLocal(): List<UserSituationEntity> {
        return db { it.lifePlannerDBQueries.getDeletedUserSituations().executeAsList() }
    }

    override suspend fun localToRemote(local: UserSituationEntity, userId: String): UserSituationSyncDto {
        return UserSituationSyncDto(
            id = local.id,
            userId = userId,
            metaJson = local.meta_json,
            careerJson = local.career_json,
            moneyJson = local.money_json,
            bodyJson = local.body_json,
            peopleJson = local.people_json,
            purposeJson = local.purpose_json,
            lastUpdatedBy = local.last_updated_by,
            updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
            isDeleted = local.is_deleted != 0L,
            syncVersion = local.sync_version
        )
    }

    override suspend fun remoteToLocal(remote: UserSituationSyncDto): UserSituationEntity {
        return UserSituationEntity(
            id = remote.id,
            meta_json = remote.metaJson,
            career_json = remote.careerJson,
            money_json = remote.moneyJson,
            body_json = remote.bodyJson,
            people_json = remote.peopleJson,
            purpose_json = remote.purposeJson,
            last_updated_by = remote.lastUpdatedBy,
            sync_updated_at = remote.updatedAt,
            is_deleted = if (remote.isDeleted) 1L else 0L,
            sync_version = remote.syncVersion,
            last_synced_at = Clock.System.now().toString()
        )
    }

    override suspend fun upsertLocal(entity: UserSituationEntity) {
        db {
            it.lifePlannerDBQueries.upsertUserSituationFromSync(
                id = entity.id,
                meta_json = entity.meta_json,
                career_json = entity.career_json,
                money_json = entity.money_json,
                body_json = entity.body_json,
                people_json = entity.people_json,
                purpose_json = entity.purpose_json,
                last_updated_by = entity.last_updated_by,
                sync_updated_at = entity.sync_updated_at,
                is_deleted = entity.is_deleted,
                sync_version = entity.sync_version,
                last_synced_at = entity.last_synced_at
            )
        }
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markUserSituationSynced(now, id) }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedUserSituations() }
    }

    override suspend fun getEntityId(entity: UserSituationEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? {
        return settings.getStringOrNull("sync_pull_user_situations")
    }

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_user_situations", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()

        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<UserSituationSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserSituationSyncDto>()
        }

        remoteItems.forEach { remote -> upsertLocal(remoteToLocal(remote)) }

        setLastPullTimestamp(now)
        if (remoteItems.isNotEmpty()) {
            Logger.d("SyncEngine") { "Pulled ${remoteItems.size} items from $tableName" }
        }
        return remoteItems.size
    }
}
