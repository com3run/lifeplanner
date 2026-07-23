package az.tribe.lifeplanner.usecases

import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
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
            val match = bestMatch(goal, values) ?: continue
            runCatching {
                goalRepository.updateGoal(goal.copy(valueId = match.id))
                linked++
            }
        }
        if (linked > 0) Logger.i(TAG) { "auto-linked $linked goal(s) to a life value" }
        return linked
    }

    private fun bestMatch(goal: Goal, values: List<LifeValue>): LifeValue? {
        val haystack = buildString {
            append(goal.title.lowercase()); append(' ')
            append(goal.description.lowercase()); append(' ')
            append(goal.category.name.lowercase()); append(' ')
            append(goal.category.displayName.lowercase())
        }
        val scored = values.map { value -> value to score(value, haystack) }
        val top = scored.maxByOrNull { it.second } ?: return null
        if (top.second < MIN_SCORE) return null
        // Ambiguous why is the user's call, not ours.
        if (scored.count { it.second == top.second } > 1) return null
        return top.first
    }

    private fun score(value: LifeValue, haystack: String): Int {
        val words = (value.title + " " + value.description)
            .lowercase()
            .split(' ', ',', '.', '&', '/')
            .filter { it.length >= MIN_WORD_LENGTH }
            .distinct()
        return words.count { it in haystack } * 2
    }

    companion object {
        private const val MIN_SCORE = 2
        private const val MIN_WORD_LENGTH = 4
        private const val TAG = "AutoLinkGoalValues"
    }
}
