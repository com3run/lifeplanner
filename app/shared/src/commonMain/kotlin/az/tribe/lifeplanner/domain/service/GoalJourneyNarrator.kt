package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import kotlin.math.abs
import kotlin.math.min

/**
 * Local v1 of the "goal as a journal" narrative: turns the user's simple input (title,
 * milestones, progress) into a short journal-style story about where they are, plus a
 * teaser of what the next milestone brings. The final milestone gets a warmer, more
 * meaningful tone. Deterministic per goal (variants keyed off the goal id) so the text
 * is stable between visits but differs between goals. No AI dependency; an AI-enhanced
 * version can replace the copy later without touching the card.
 */
object GoalJourneyNarrator {

    data class Journey(
        val chapterLabel: String,
        val story: String,
        val teaserLabel: String?,
        val teaser: String?,
        val isFinalStep: Boolean,
        val isComplete: Boolean,
    )

    /**
     * @param isPractice the goal is kept through habits rather than finished through milestones.
     *   A practice goal with no milestones is not missing a map, so it must not be told it is.
     */
    fun narrate(goal: Goal, isPractice: Boolean = false): Journey {
        val title = goal.title.trim().removeSuffix(".")
        val milestones = goal.milestones
        val total = milestones.size
        val done = milestones.count { it.isCompleted }
        val next = milestones.firstOrNull { !it.isCompleted }
        val variant = abs(goal.id.hashCode())
        val isComplete = goal.status == GoalStatus.COMPLETED || (total > 0 && done == total)
        val isFinalStep = !isComplete && next != null && done == total - 1 && total > 1

        return when {
            isComplete -> Journey(
                chapterLabel = "The full story",
                story = "\"$title\" is no longer a goal, it is a story you finished." +
                    (if (total > 0) " $total steps, every one of them yours." else "") +
                    " Let it remind you what you are capable of the next time a blank page stares back.",
                teaserLabel = null,
                teaser = null,
                isFinalStep = false,
                isComplete = true,
            )

            // A goal you keep. There is no map to draw because there is no destination to reach,
            // and the old copy told these users to go sketch milestones they will never need.
            total == 0 && isPractice -> Journey(
                chapterLabel = "An ongoing chapter",
                story = "\"$title\" is not a thing you finish. It is a thing you keep doing, and " +
                    "the habits below are how it happens. There is nothing here to tick off, " +
                    "which is exactly right.",
                teaserLabel = null,
                teaser = null,
                isFinalStep = false,
                isComplete = false,
            )

            total == 0 -> Journey(
                chapterLabel = "Chapter 1",
                story = "You have named the destination: \"$title\". Where it goes next is open. " +
                    "Add a few milestones when you know what they are, or leave it as an " +
                    "intention and let it be one.",
                teaserLabel = null,
                teaser = null,
                isFinalStep = false,
                isComplete = false,
            )

            isFinalStep -> Journey(
                chapterLabel = "The last chapter",
                story = "You are standing at the last page of \"$title\". Everything you have " +
                    "done so far, all ${total - 1} steps of it, led to this one remaining move. " +
                    "Finish it, and this stops being a goal and becomes part of who you are.",
                teaserLabel = "The ending",
                teaser = "One step left: \"${next!!.title}\". However it goes, you will remember " +
                    "the day you closed this chapter. Go write the ending.",
                isFinalStep = true,
                isComplete = false,
            )

            done == 0 -> Journey(
                chapterLabel = "Chapter 1 of $total",
                story = pick(
                    variant,
                    "Every story worth telling starts exactly here, at the moment before the " +
                        "first move. You wrote down \"$title\" because some part of you already " +
                        "knows it matters. The page is blank and the pen is in your hand.",
                    "This is chapter one of \"$title\". Nothing has happened yet, which means " +
                        "nothing has gone wrong yet either. The whole journey is still yours " +
                        "to shape.",
                ),
                teaserLabel = "Where it begins",
                teaser = next?.let { teaserFor(variant, it.title, title) },
                isFinalStep = false,
                isComplete = false,
            )

            else -> Journey(
                chapterLabel = "Chapter ${min(done + 1, total)} of $total",
                story = pick(
                    variant,
                    "You are $done ${if (done == 1) "step" else "steps"} into \"$title\", and " +
                        "the story has started to move. What felt like a wish on day one is " +
                        "turning into a track record. Momentum is quiet, but it is on your side.",
                    "Looking back, $done of $total steps are already behind you. That did not " +
                        "happen by accident, it happened because you kept showing up for " +
                        "\"$title\".",
                ),
                teaserLabel = "Next in your story",
                teaser = next?.let { teaserFor(variant, it.title, title) },
                isFinalStep = false,
                isComplete = false,
            )
        }
    }

    private fun teaserFor(variant: Int, nextTitle: String, goalTitle: String): String = pick(
        variant,
        "Up next: \"$nextTitle\". Once it is done, the distance between you and " +
            "\"$goalTitle\" gets noticeably shorter.",
        "The next scene is \"$nextTitle\". Future you is already grateful you did not skip it.",
    )

    private fun pick(variant: Int, vararg options: String): String = options[variant % options.size]
}
