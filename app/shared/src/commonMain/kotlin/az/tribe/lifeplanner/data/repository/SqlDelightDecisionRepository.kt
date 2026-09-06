package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.mapper.toDomain
import az.tribe.lifeplanner.data.mapper.toDomainDecisions
import az.tribe.lifeplanner.data.mapper.toEntity
import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.deleteDecisionById
import az.tribe.lifeplanner.infrastructure.getAllDecisions
import az.tribe.lifeplanner.infrastructure.getDecisionById
import az.tribe.lifeplanner.infrastructure.getDecisionsByGoal
import az.tribe.lifeplanner.infrastructure.insertDecision
import az.tribe.lifeplanner.infrastructure.observeAllDecisions
import az.tribe.lifeplanner.infrastructure.updateDecision
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightDecisionRepository(
    private val db: SharedDatabase,
    private val syncManager: SyncManager
) : DecisionRepository {

    override fun observeAllDecisions(): Flow<List<Decision>> =
        db.observeAllDecisions().map { it.toDomainDecisions() }

    override suspend fun getAllDecisions(): List<Decision> =
        db.getAllDecisions().toDomainDecisions()

    override suspend fun getDecisionById(id: String): Decision? =
        db.getDecisionById(id)?.toDomain()

    override suspend fun getDecisionsForGoal(goalId: String): List<Decision> =
        db.getDecisionsByGoal(goalId).toDomainDecisions()

    override suspend fun insertDecision(decision: Decision) {
        db.insertDecision(decision.toEntity())
        syncManager.requestSync()
    }

    override suspend fun updateDecision(decision: Decision) {
        db.updateDecision(decision.toEntity())
        syncManager.requestSync()
    }

    override suspend fun deleteDecisionById(id: String) {
        db.deleteDecisionById(id)
        syncManager.requestSync()
    }
}
