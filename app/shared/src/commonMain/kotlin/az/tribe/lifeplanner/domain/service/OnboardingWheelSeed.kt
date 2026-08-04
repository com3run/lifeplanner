package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.WheelArea

/**
 * Turns nine numbers given at sign-up into everything the rest of onboarding used to ask for.
 *
 * Registration used to open with "which areas of life matter most to you right now? Select at least
 * 3" — an abstract question, from a blank page, before the user has seen anything the app does. It
 * also told us almost nothing: a list of categories with no sense of how any of them are going.
 *
 * Rating the wheel is less work (a tap per area) and worth far more. It seeds real
 * [az.tribe.lifeplanner.domain.model.ScoreSource.USER] scores, so the wheel is the user's own from
 * the first launch instead of nine invented fives, and every part of the app that reads the wheel —
 * the nudge picker, the coach's read on a goal, the suggested first goal — has something true to
 * work from on day one.
 *
 * Priorities are then derived rather than asked for. Someone who rates Money 3 and Family 9 has
 * already told us where the work is.
 */
object OnboardingWheelSeed {

    /** How many areas become the user's starting focus. */
    const val PRIORITY_COUNT = 3

    /**
     * The categories to focus on, weakest area first.
     *
     * Areas with no goal category of their own (Romance, and Joy which is never rated) simply do
     * not contribute — there is nothing for a goal in that category to be filed under.
     * Deduplicated, because Mission and Growth are both Career and a user who rated both badly
     * should not get Career twice at the expense of a third area.
     */
    fun prioritiesFrom(ratings: Map<WheelArea, Double>): List<GoalCategory> {
        if (ratings.isEmpty()) return emptyList()

        return ratings.entries
            .filter { it.key.isWheelSegment }
            // Ties broken by the wheel's own order so the result is stable rather than arbitrary.
            .sortedWith(compareBy({ it.value }, { it.key.order }))
            .flatMap { it.key.categories }
            .distinct()
            .take(PRIORITY_COUNT)
    }

    /**
     * The single area to open the conversation about: the weakest one the user actually rated.
     *
     * Joy is excluded for the same reason it is everywhere else — it reads the whole wheel rather
     * than being a slice you can act on.
     */
    fun weakestArea(ratings: Map<WheelArea, Double>): WheelArea? =
        ratings.entries
            .filter { it.key.isWheelSegment }
            .minWithOrNull(compareBy({ it.value }, { it.key.order }))
            ?.key

    /**
     * Whether we have enough to seed a wheel worth showing.
     *
     * Two or three answers is not a wheel, it is a wheel with holes in it, and drawing it as though
     * it were complete would misrepresent the user's own data back to them.
     */
    fun isEnoughToSeed(ratings: Map<WheelArea, Double>): Boolean =
        ratings.keys.count { it.isWheelSegment } >= WheelArea.segments().size - 2
}
