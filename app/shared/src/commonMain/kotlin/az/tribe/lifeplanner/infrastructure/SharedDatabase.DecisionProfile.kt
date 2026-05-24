package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import az.tribe.lifeplanner.database.DecisionProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- DecisionProfileEntity accessors (one row per user) ---

suspend fun SharedDatabase.getDecisionProfile(): DecisionProfileEntity? =
    this { db -> db.lifePlannerDBQueries.selectDecisionProfile().executeAsOneOrNull() }

suspend fun SharedDatabase.upsertDecisionProfile(p: DecisionProfileEntity) {
    this { db ->
        db.lifePlannerDBQueries.upsertDecisionProfile(
            id = p.id,
            confidenceThresholdValue = p.confidenceThresholdValue,
            confidenceThresholdConfidence = p.confidenceThresholdConfidence,
            confidenceThresholdSamples = p.confidenceThresholdSamples,
            noveltySalienceValue = p.noveltySalienceValue,
            noveltySalienceConfidence = p.noveltySalienceConfidence,
            noveltySalienceSamples = p.noveltySalienceSamples,
            delayDiscountingValue = p.delayDiscountingValue,
            delayDiscountingConfidence = p.delayDiscountingConfidence,
            delayDiscountingSamples = p.delayDiscountingSamples,
            punishmentSensitivityValue = p.punishmentSensitivityValue,
            punishmentSensitivityConfidence = p.punishmentSensitivityConfidence,
            punishmentSensitivitySamples = p.punishmentSensitivitySamples,
            rewardSensitivityValue = p.rewardSensitivityValue,
            rewardSensitivityConfidence = p.rewardSensitivityConfidence,
            rewardSensitivitySamples = p.rewardSensitivitySamples,
            riskAversionValue = p.riskAversionValue,
            riskAversionConfidence = p.riskAversionConfidence,
            riskAversionSamples = p.riskAversionSamples,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

fun SharedDatabase.observeDecisionProfile(): Flow<DecisionProfileEntity?> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectDecisionProfile()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
    )
}
