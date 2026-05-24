package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.Decision
import kotlinx.coroutines.flow.Flow

interface DecisionRepository {
    fun observeAllDecisions(): Flow<List<Decision>>
    suspend fun getAllDecisions(): List<Decision>
    suspend fun getDecisionById(id: String): Decision?
    suspend fun getDecisionsForGoal(goalId: String): List<Decision>
    suspend fun insertDecision(decision: Decision)
    suspend fun updateDecision(decision: Decision)
    suspend fun deleteDecisionById(id: String)
}
