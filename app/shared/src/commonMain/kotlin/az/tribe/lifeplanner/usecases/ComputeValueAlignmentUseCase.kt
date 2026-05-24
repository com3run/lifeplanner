package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Decision
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.model.ValueAlignment
import az.tribe.lifeplanner.domain.repository.DecisionRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository

/**
 * Pillar 5 (P5.2), computes per-[LifeValue] alignment: how many completed goals and logged
 * decisions served each value. The "becoming" progress signal.
 */
class ComputeValueAlignmentUseCase(
    private val lifeValueRepository: LifeValueRepository,
    private val goalRepository: GoalRepository,
    private val decisionRepository: DecisionRepository,
) {
    suspend operator fun invoke(): List<ValueAlignment> =
        computeValueAlignment(
            values = lifeValueRepository.getActiveLifeValues(),
            allGoals = goalRepository.getAllGoals(),
            decisions = decisionRepository.getAllDecisions(),
        )
}

/**
 * Pure, deterministic core (testable). A completed goal serves the value it's tagged with;
 * a decision serves the value of its related goal. [goalShare] is this value's slice of all
 * value-tagged completed goals.
 */
internal fun computeValueAlignment(
    values: List<LifeValue>,
    allGoals: List<Goal>,
    decisions: List<Decision>,
): List<ValueAlignment> {
    val valueByGoalId = allGoals.associate { it.id to it.valueId }
    val completed = allGoals.filter { it.status == GoalStatus.COMPLETED }
    val totalCompletedWithValue = completed.count { it.valueId != null }.coerceAtLeast(1)
    return values.map { v ->
        val goalsForV = completed.count { it.valueId == v.id }
        val decisionsForV = decisions.count { d -> d.relatedGoalId?.let { valueByGoalId[it] == v.id } == true }
        ValueAlignment(
            valueId = v.id,
            valueTitle = v.title,
            completedGoalCount = goalsForV,
            decisionCount = decisionsForV,
            goalShare = goalsForV.toDouble() / totalCompletedWithValue,
        )
    }.sortedByDescending { it.completedGoalCount }
}
