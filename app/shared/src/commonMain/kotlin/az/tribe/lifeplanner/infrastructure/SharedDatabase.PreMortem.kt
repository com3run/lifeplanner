package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.PreMortemPlanEntity
import az.tribe.lifeplanner.domain.model.PreMortemPlan

// Crystal Ball storage (schema v35): pre-mortem if-then plans + the follow-through forecast
// snapshot per goal. Mirrors the SharedDatabase.UserSituation.kt extension pattern.

suspend fun SharedDatabase.upsertPreMortemPlan(
    id: String,
    goalId: String,
    obstacle: String,
    ifCondition: String,
    thenAction: String,
    triggerType: String,
) {
    this { db ->
        db.lifePlannerDBQueries.upsertPreMortemPlan(
            id = id,
            goalId = goalId,
            obstacle = obstacle,
            ifCondition = ifCondition,
            thenAction = thenAction,
            triggerType = triggerType,
            timesSurfaced = 0,
            timesActedOn = 0,
            createdAt = nowTimestamp(),
            sync_updated_at = nowTimestamp(),
            is_deleted = 0,
            sync_version = 0,
            last_synced_at = null,
        )
    }
}

suspend fun SharedDatabase.preMortemPlansForGoal(goalId: String): List<PreMortemPlan> {
    return this { db ->
        db.lifePlannerDBQueries.getPreMortemPlansForGoal(goalId).executeAsList().map { it.toDomain() }
    }
}

/** Plans for one goal matching a fired ChoicePointTrigger (resurfacing lookup). */
suspend fun SharedDatabase.preMortemPlansForTrigger(goalId: String, triggerType: String): List<PreMortemPlan> {
    return this { db ->
        db.lifePlannerDBQueries.getPreMortemPlansForGoalAndTrigger(goalId, triggerType)
            .executeAsList().map { it.toDomain() }
    }
}

suspend fun SharedDatabase.markPreMortemSurfaced(id: String) {
    this { db -> db.lifePlannerDBQueries.incrementPreMortemSurfaced(nowTimestamp(), id) }
}

suspend fun SharedDatabase.markPreMortemActedOn(id: String) {
    this { db -> db.lifePlannerDBQueries.incrementPreMortemActedOn(nowTimestamp(), id) }
}

/** Snapshot of the wizard-time follow-through forecast (recompute lives in AdherenceForecastEngine). */
suspend fun SharedDatabase.upsertGoalForecast(
    goalId: String,
    adherencePct: Long,
    bandPct: Long,
    confidence: String,
    isColdStart: Long,
) {
    this { db ->
        db.lifePlannerDBQueries.upsertGoalForecast(
            goalId = goalId,
            adherencePct = adherencePct,
            bandPct = bandPct,
            confidence = confidence,
            isColdStart = isColdStart,
            computedAt = nowTimestamp(),
            sync_updated_at = nowTimestamp(),
            is_deleted = 0,
            sync_version = 0,
            last_synced_at = null,
        )
    }
}

private fun PreMortemPlanEntity.toDomain() = PreMortemPlan(
    id = id,
    goalId = goalId,
    obstacle = obstacle,
    ifCondition = ifCondition,
    thenAction = thenAction,
    triggerType = triggerType,
    timesSurfaced = timesSurfaced.toInt(),
    timesActedOn = timesActedOn.toInt(),
    createdAt = createdAt,
)
