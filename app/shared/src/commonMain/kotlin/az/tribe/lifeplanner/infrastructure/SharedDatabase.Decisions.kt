package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.DecisionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- DecisionEntity accessors ---

suspend fun SharedDatabase.getAllDecisions(): List<DecisionEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllDecisions().executeAsList() }

suspend fun SharedDatabase.getDecisionById(id: String): DecisionEntity? =
    this { db -> db.lifePlannerDBQueries.selectDecisionById(id).executeAsOneOrNull() }

suspend fun SharedDatabase.getDecisionsByGoal(goalId: String): List<DecisionEntity> =
    this { db -> db.lifePlannerDBQueries.selectDecisionsByGoal(goalId).executeAsList() }

suspend fun SharedDatabase.insertDecision(d: DecisionEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertDecision(
            id = d.id,
            question = d.question,
            optionsConsidered = d.optionsConsidered,
            chosenOption = d.chosenOption,
            reasoning = d.reasoning,
            relatedGoalId = d.relatedGoalId,
            expectedOutcome = d.expectedOutcome,
            confidence = d.confidence,
            decidedAt = d.decidedAt,
            actualOutcome = d.actualOutcome,
            outcomeReviewedAt = d.outcomeReviewedAt,
            outcomeQuality = d.outcomeQuality,
            source = d.source,
            status = d.status,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateDecision(d: DecisionEntity) {
    this { db ->
        db.lifePlannerDBQueries.updateDecision(
            question = d.question,
            optionsConsidered = d.optionsConsidered,
            chosenOption = d.chosenOption,
            reasoning = d.reasoning,
            relatedGoalId = d.relatedGoalId,
            expectedOutcome = d.expectedOutcome,
            confidence = d.confidence,
            decidedAt = d.decidedAt,
            actualOutcome = d.actualOutcome,
            outcomeReviewedAt = d.outcomeReviewedAt,
            outcomeQuality = d.outcomeQuality,
            status = d.status,
            id = d.id
        )
    }
}

suspend fun SharedDatabase.deleteDecisionById(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteDecision(nowTimestamp(), id) }
}

fun SharedDatabase.observeAllDecisions(): Flow<List<DecisionEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllDecisions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
