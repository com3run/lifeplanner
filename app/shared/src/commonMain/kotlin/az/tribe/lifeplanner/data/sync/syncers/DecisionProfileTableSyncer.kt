package az.tribe.lifeplanner.data.sync.syncers

import az.tribe.lifeplanner.data.sync.TableSyncer
import az.tribe.lifeplanner.data.sync.dto.DecisionProfileSyncDto
import az.tribe.lifeplanner.database.DecisionProfileEntity
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock

/**
 * Pillar 7, syncs the user's single [DecisionProfileEntity] (one row per user) to the
 * `decision_profiles` table. Mirrors the standard soft-delete syncer; the only quirk is
 * that this table holds at most one row per user.
 */
class DecisionProfileTableSyncer(
    supabase: SupabaseClient,
    private val db: SharedDatabase
) : TableSyncer<DecisionProfileEntity, DecisionProfileSyncDto>(supabase) {

    override val tableName = "decision_profiles"
    private val settings = Settings()

    override suspend fun upsertRemote(dtos: List<DecisionProfileSyncDto>) {
        supabase.postgrest[tableName].upsert(dtos)
    }

    override suspend fun getUnsyncedLocal(): List<DecisionProfileEntity> =
        db { it.lifePlannerDBQueries.getUnsyncedDecisionProfiles().executeAsList() }

    override suspend fun getDeletedLocal(): List<DecisionProfileEntity> =
        db { it.lifePlannerDBQueries.getDeletedDecisionProfiles().executeAsList() }

    override suspend fun localToRemote(local: DecisionProfileEntity, userId: String) = DecisionProfileSyncDto(
        id = local.id,
        userId = userId,
        confidenceThresholdValue = local.confidenceThresholdValue,
        confidenceThresholdConfidence = local.confidenceThresholdConfidence,
        confidenceThresholdSamples = local.confidenceThresholdSamples,
        noveltySalienceValue = local.noveltySalienceValue,
        noveltySalienceConfidence = local.noveltySalienceConfidence,
        noveltySalienceSamples = local.noveltySalienceSamples,
        delayDiscountingValue = local.delayDiscountingValue,
        delayDiscountingConfidence = local.delayDiscountingConfidence,
        delayDiscountingSamples = local.delayDiscountingSamples,
        punishmentSensitivityValue = local.punishmentSensitivityValue,
        punishmentSensitivityConfidence = local.punishmentSensitivityConfidence,
        punishmentSensitivitySamples = local.punishmentSensitivitySamples,
        rewardSensitivityValue = local.rewardSensitivityValue,
        rewardSensitivityConfidence = local.rewardSensitivityConfidence,
        rewardSensitivitySamples = local.rewardSensitivitySamples,
        riskAversionValue = local.riskAversionValue,
        riskAversionConfidence = local.riskAversionConfidence,
        riskAversionSamples = local.riskAversionSamples,
        updatedAt = local.sync_updated_at ?: Clock.System.now().toString(),
        isDeleted = local.is_deleted != 0L,
        syncVersion = local.sync_version
    )

    override suspend fun remoteToLocal(remote: DecisionProfileSyncDto) = DecisionProfileEntity(
        id = remote.id,
        confidenceThresholdValue = remote.confidenceThresholdValue,
        confidenceThresholdConfidence = remote.confidenceThresholdConfidence,
        confidenceThresholdSamples = remote.confidenceThresholdSamples,
        noveltySalienceValue = remote.noveltySalienceValue,
        noveltySalienceConfidence = remote.noveltySalienceConfidence,
        noveltySalienceSamples = remote.noveltySalienceSamples,
        delayDiscountingValue = remote.delayDiscountingValue,
        delayDiscountingConfidence = remote.delayDiscountingConfidence,
        delayDiscountingSamples = remote.delayDiscountingSamples,
        punishmentSensitivityValue = remote.punishmentSensitivityValue,
        punishmentSensitivityConfidence = remote.punishmentSensitivityConfidence,
        punishmentSensitivitySamples = remote.punishmentSensitivitySamples,
        rewardSensitivityValue = remote.rewardSensitivityValue,
        rewardSensitivityConfidence = remote.rewardSensitivityConfidence,
        rewardSensitivitySamples = remote.rewardSensitivitySamples,
        riskAversionValue = remote.riskAversionValue,
        riskAversionConfidence = remote.riskAversionConfidence,
        riskAversionSamples = remote.riskAversionSamples,
        sync_updated_at = remote.updatedAt,
        is_deleted = if (remote.isDeleted) 1L else 0L,
        sync_version = remote.syncVersion,
        last_synced_at = Clock.System.now().toString()
    )

    override suspend fun upsertLocal(entity: DecisionProfileEntity) {
        db { it.lifePlannerDBQueries.upsertDecisionProfileFromSync(
            id = entity.id,
            confidenceThresholdValue = entity.confidenceThresholdValue,
            confidenceThresholdConfidence = entity.confidenceThresholdConfidence,
            confidenceThresholdSamples = entity.confidenceThresholdSamples,
            noveltySalienceValue = entity.noveltySalienceValue,
            noveltySalienceConfidence = entity.noveltySalienceConfidence,
            noveltySalienceSamples = entity.noveltySalienceSamples,
            delayDiscountingValue = entity.delayDiscountingValue,
            delayDiscountingConfidence = entity.delayDiscountingConfidence,
            delayDiscountingSamples = entity.delayDiscountingSamples,
            punishmentSensitivityValue = entity.punishmentSensitivityValue,
            punishmentSensitivityConfidence = entity.punishmentSensitivityConfidence,
            punishmentSensitivitySamples = entity.punishmentSensitivitySamples,
            rewardSensitivityValue = entity.rewardSensitivityValue,
            rewardSensitivityConfidence = entity.rewardSensitivityConfidence,
            rewardSensitivitySamples = entity.rewardSensitivitySamples,
            riskAversionValue = entity.riskAversionValue,
            riskAversionConfidence = entity.riskAversionConfidence,
            riskAversionSamples = entity.riskAversionSamples,
            sync_updated_at = entity.sync_updated_at,
            is_deleted = entity.is_deleted,
            sync_version = entity.sync_version,
            last_synced_at = entity.last_synced_at
        )}
    }

    override suspend fun markSynced(id: String, now: String) {
        db { it.lifePlannerDBQueries.markDecisionProfileSynced(now, id) }
    }

    override suspend fun markSyncedBatch(entities: List<DecisionProfileEntity>, now: String) {
        if (entities.isEmpty()) return
        db { d -> entities.forEach { d.lifePlannerDBQueries.markDecisionProfileSynced(now, it.id) } }
    }

    override suspend fun purgeDeleted() {
        db { it.lifePlannerDBQueries.purgeDeletedDecisionProfiles() }
    }

    override suspend fun getEntityId(entity: DecisionProfileEntity) = entity.id

    override suspend fun getLastPullTimestamp(): String? =
        settings.getStringOrNull("sync_pull_decision_profiles")

    override suspend fun setLastPullTimestamp(timestamp: String) {
        settings.putString("sync_pull_decision_profiles", timestamp)
    }

    override suspend fun pullRemoteChanges(userId: String): Int {
        val lastPull = getLastPullTimestamp()
        val now = Clock.System.now().toString()
        val remoteItems = if (lastPull != null) {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId); gt("updated_at", lastPull) } }
                .decodeList<DecisionProfileSyncDto>()
        } else {
            supabase.postgrest[tableName]
                .select { filter { eq("user_id", userId) } }
                .decodeList<DecisionProfileSyncDto>()
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

    private suspend fun getLocalById(id: String): DecisionProfileEntity? = try {
        db { it.lifePlannerDBQueries.selectDecisionProfile().executeAsOneOrNull()?.takeIf { e -> e.id == id } }
    } catch (e: Exception) {
        null
    }
}
