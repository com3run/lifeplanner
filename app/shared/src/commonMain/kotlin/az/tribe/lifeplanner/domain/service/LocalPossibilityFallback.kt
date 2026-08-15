package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.PermutationKind
import az.tribe.lifeplanner.domain.model.Possibility
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 6 divergence without the network. When the ai-proxy cannot be reached, Possibility Mode
 * still has to widen the option set, so this produces one concrete option per cognitive
 * permutation, drawn from the goal's own milestones and life area. Pure and deterministic apart
 * from ids, which keeps it fully unit-testable; the screen tells the user when a set came from
 * here rather than from the AI.
 */
@OptIn(ExperimentalUuidApi::class)
class LocalPossibilityFallback {

    operator fun invoke(goal: Goal): List<Possibility> {
        val next = goal.milestones.firstOrNull { !it.isCompleted }?.title
        val title = goal.title
        return listOf(
            local(
                text = if (next != null)
                    "Do the first 10 minutes of \"$next\" today, then let yourself stop."
                else
                    "Shrink \"$title\" to a 10 minute version and do only that today.",
                permutation = PermutationKind.SHRINK,
                rationale = "Ten minutes is small enough that starting stops being the hard part.",
            ),
            local(
                text = "Write down the three things most likely keeping \"$title\" stuck, then remove one this week.",
                permutation = PermutationKind.INVERT,
                rationale = "Clearing a blocker often moves more than pushing harder.",
            ),
            local(
                text = "Spend one day on \"$title\" as if doing it perfectly did not matter, only showing up did.",
                permutation = PermutationKind.QUESTION_ASSUMPTION,
                rationale = "The rule that feels fixed is often the one keeping things stuck.",
            ),
            local(
                text = "Attach the first small step of \"$title\" to something you already do every day.",
                permutation = PermutationKind.RECOMBINE,
                rationale = "Borrowing an existing routine beats building a new one from scratch.",
            ),
            local(
                text = analogyFor(goal.category, title),
                permutation = PermutationKind.ANALOGY,
                rationale = "A different domain often carries the move you cannot see up close.",
            ),
        )
    }

    private fun local(text: String, permutation: PermutationKind, rationale: String) = Possibility(
        id = Uuid.random().toString(),
        text = text,
        permutation = permutation,
        rationale = rationale,
        isLocal = true,
    )

    private fun analogyFor(category: GoalCategory, title: String): String = when (category) {
        GoalCategory.CAREER -> "Plan \"$title\" like an athlete plans a season: one skill to drill this week, the rest can wait."
        GoalCategory.MONEY -> "Treat \"$title\" like training for a race: a small fixed amount on a schedule beats occasional big pushes."
        GoalCategory.BODY -> "Practice \"$title\" like a musician: the same short drill daily instead of one long weekly session."
        GoalCategory.PEOPLE -> "Tend \"$title\" like a plant: one small touch each week keeps it alive without a grand gesture."
        GoalCategory.WELLBEING -> "Recharge \"$title\" like a phone: several small top-ups through the day, not one overnight fix."
        GoalCategory.PURPOSE -> "Grow \"$title\" like a garden: plant one small thing now and let time do part of the work."
        GoalCategory.FAMILY -> "Protect \"$title\" like a standing date: same day, same time, no weekly renegotiation."
    }
}
