package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.DecisionSyncDto
import az.tribe.lifeplanner.database.DecisionEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import co.touchlab.kermit.Logger
import kotlin.time.Clock

class DecisionTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<DecisionEntity, DecisionSyncDto>(supabase) {

    override val tableName = "decisions"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<DecisionSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<DecisionEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedDecisions().executeAsList() }

    override suspend fun getDeletedLocal(): List<DecisionEntity> =
        db { it.lifePlannerDBQueries.getDeletedDecisions().executeAsList() }

    override suspend fun localToRemote(local: DecisionEntity, userId: String) = DecisionSyncDto(
        id = local.id,
        userId = userId,
        question = local.question,
        optionsConsidered = local.optionsConsidered,
        chosenOption = local.chosenOption,
        reasoning = local.reasoning,
        relatedGoalId = local.relatedGoalId,
        expectedOutcome = local.expectedOutcome,
        confidence = local.confidence,
        decidedAt = local.decidedAt,
        actualOutcome = local.actualOutcome,
        outcomeReviewedAt = local.outcomeReviewedAt,
        outcomeQuality = local.outcomeQuality,
        source = local.source,
        status = local.status,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: DecisionSyncDto) = DecisionEntity(
        id = remote.id,
        question = remote.question,
        optionsConsidered = remote.optionsConsidered,
        chosenOption = remote.chosenOption,
        reasoning = remote.reasoning,
        relatedGoalId = remote.relatedGoalId,
        expectedOutcome = remote.expectedOutcome,
        confidence = remote.confidence,
        decidedAt = remote.decidedAt,
        actualOutcome = remote.actualOutcome,
        outcomeReviewedAt = remote.outcomeReviewedAt,
        outcomeQuality = remote.outcomeQuality,
        source = remote.source,
        status = remote.status,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: DecisionEntity) {
        db { it.lifePlannerDBQueries.upsertDecisionFromSync(
            id = entity.id,
            question = entity.question,
            optionsConsidered = entity.optionsConsidered,
            chosenOption = entity.chosenOption,
            reasoning = entity.reasoning,
            relatedGoalId = entity.relatedGoalId,
            expectedOutcome = entity.expectedOutcome,
            confidence = entity.confidence,
            decidedAt = entity.decidedAt,
            actualOutcome = entity.actualOutcome,
            outcomeReviewedAt = entity.outcomeReviewedAt,
            outcomeQuality = entity.outcomeQuality,
            source = entity.source,
            status = entity.status,
            sync_updated_at = entity.sync_updated_at,
            is_deleted = entity.is_deleted,
            sync_version = entity.sync_version,
            last_synced_at = entity.last_synced_at
        )}
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markDecisionSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<DecisionEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markDecisionSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedDecisions() }
    }

    override suspend fun getEntityId(entity: DecisionEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_decisions")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_decisions", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()
        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<DecisionSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<DecisionSyncDto>()
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

    private suspend fun getLocalById(id: String): DecisionEntity? = try {
        db { it.lifePlannerDBQueries.selectDecisionById(id).executeAsOneOrNull() }
    } catch (e: Exception) {
        null
    }
}
