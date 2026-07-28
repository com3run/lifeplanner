package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.service.GoalValueInferrer
import co.touchlab.kermit.Logger

/**
 * "We know the why, do it for the user in advance": goals created without a linked life
 * value get one auto-linked when the match is unambiguous, so the app can present the why
 * (and the coach that goes with it) instead of asking. Conservative on purpose: a value is
 * linked only when it is the single best keyword match against the goal's title,
 * description, and category; ties or zero-score goals stay unlinked and keep the
 * "what is this really for?" nudge. Idempotent and cheap, runs at app start.
 */
class AutoLinkGoalValuesUseCase(
    private val goalRepository: GoalRepository,
    private val lifeValueRepository: LifeValueRepository,
) {
    suspend operator fun invoke(): Int {
        val values = runCatching { lifeValueRepository.getActiveLifeValues() }
            .getOrElse { return 0 }
        if (values.isEmpty()) return 0
        val goals = runCatching { goalRepository.getAllGoals() }.getOrElse { return 0 }

        var linked = 0
        for (goal in goals) {
            if (goal.valueId != null || goal.isArchived) continue
            // Single source of truth for the match, shared with creation-time and the detail nudge.
            val matchId = GoalValueInferrer.infer(goal.category, goal.title, goal.description, values) ?: continue
            runCatching {
                goalRepository.updateGoal(goal.copy(valueId = matchId))
                linked++
            }
        }
        if (linked > 0) Logger.i(TAG) { "auto-linked $linked goal(s) to a life value" }
        return linked
    }

    companion object {
        private const val TAG = "AutoLinkGoalValues"
    }
}
