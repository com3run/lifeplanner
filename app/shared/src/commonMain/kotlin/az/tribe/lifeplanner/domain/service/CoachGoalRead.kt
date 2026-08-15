package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalStatus

/**
 * What the coach can see about a goal right now.
 *
 * Deliberately a flat value object rather than the [az.tribe.lifeplanner.domain.model.Goal] itself,
 * so the read is a pure function of a handful of numbers and can be tested by writing them down.
 */
data class GoalSnapshot(
    val status: GoalStatus,
    val milestonesTotal: Int,
    val milestonesDone: Int,
    /** Title of the first unfinished step, or null when there is no plan or it is finished. */
    val nextStep: String?,
    /** Negative when the due date has passed. */
    val daysUntilDue: Int,
    val ageDays: Int,
    val reflections: Int = 0,
    /** Non-null when habits are linked and the goal is a practice rather than a checklist. */
    val practiceDay: Int? = null,
    val practiceStreak: Int? = null,
    val areaName: String,
    val areaIsLowest: Boolean = false,
)

/**
 * The coach's read on a goal as it actually stands today.
 *
 * The coach card used to say the same sentence forever: a name, a job title, and a line about how
 * this coach likes to work. True, and useless after the first read — it said nothing that would be
 * different tomorrow, or different for a goal that had stalled versus one nearly finished.
 *
 * This says the thing a person would say if they glanced at the goal: what state it is in, and the
 * one move that follows from it. Local and instant like [MilestoneCoach], so it is right on every
 * open rather than whenever a network call happens to land.
 *
 * The register matters as much as the facts. A goal that has not moved in a month does not want
 * encouragement, it wants someone to say so plainly and offer the smallest way back in. Nothing here
 * congratulates the user for a goal they have not touched.
 */
object CoachGoalRead {

    /** Below this, a goal that is merely young is not yet "stalled". */
    private const val STALE_AGE_DAYS = 21

    fun read(coachName: String, s: GoalSnapshot): String {
        val open = s.milestonesTotal - s.milestonesDone
        val head = headline(coachName, s, open)
        val area = areaClause(s)
        return if (area != null) "$head $area" else head
    }

    /**
     * The read, or null when all it would say is the progress the caller already shows.
     *
     * The Why-Chain's milestones node displays "2 of 7 done" and the next step itself, and the
     * plain in-progress read says exactly that sentence. A goal in any state worth remarking on
     * (stalled, overdue, one step left, a practice, a lowest area) still gets its read; a goal
     * that is simply moving along gets silence instead of an echo.
     */
    fun readBeyondProgress(coachName: String, s: GoalSnapshot): String? {
        val full = read(coachName, s)
        val plainProgress = "${s.milestonesDone} of ${s.milestonesTotal} done. Next is \"${s.nextStep}\"."
        return full.takeIf { it != plainProgress }
    }

    private fun headline(coachName: String, s: GoalSnapshot, open: Int): String = when {

        s.status == GoalStatus.COMPLETED ->
            if (s.reflections > 0) {
                "Done, and you wrote about it ${s.reflections} time${plural(s.reflections)} on the " +
                    "way. That record is worth more than the tick."
            } else {
                "Done. Worth remembering how it felt at the start, next time something looks too big."
            }

        // Everything ticked but the goal still open. The user has finished and not noticed.
        s.milestonesTotal > 0 && open == 0 ->
            "Every step is ticked. This is finished unless you know something the plan does not."

        s.daysUntilDue < 0 && open > 0 ->
            "The date passed ${-s.daysUntilDue} day${plural(-s.daysUntilDue)} ago and $open step" +
                "${plural(open)} ${isAre(open)} still open. Move the date or cut the plan. A goal " +
                "that quietly expires teaches you nothing."

        s.daysUntilDue < 0 ->
            "Past its date with nothing left outstanding. Close it or give it a new one."

        // A practice is kept, not completed, so the read is about whether it is still running.
        s.practiceDay != null -> when {
            (s.practiceStreak ?: 0) == 0 ->
                "Day ${s.practiceDay}, but the streak is at zero. Starting again today counts for " +
                    "exactly as much as never stopping would have."
            (s.practiceStreak ?: 0) >= 7 ->
                "${s.practiceStreak} days running, ${s.practiceDay} into this. It is starting to " +
                    "be something you do rather than something you decided."
            else ->
                "Day ${s.practiceDay}, ${s.practiceStreak} in a row. Short streaks are the fragile " +
                    "part — today is the one that matters."
        }

        s.milestonesTotal >= 2 && open == 1 ->
            "One step left: \"${s.nextStep}\". Everything before it is already done."

        // Old and untouched. Said plainly, because pretending otherwise is how an app loses trust.
        s.milestonesDone == 0 && s.ageDays > STALE_AGE_DAYS && s.milestonesTotal > 0 ->
            "Nothing has moved here in the ${weeks(s.ageDays)} since you wrote it. That usually " +
                "means the first step is too big, not that you are lazy. \"${s.nextStep}\" — " +
                "what would the ten-minute version be?"

        s.milestonesDone > 0 ->
            "${s.milestonesDone} of ${s.milestonesTotal} done. Next is \"${s.nextStep}\"."

        // A goal with no plan is allowed to stay that way; this offers rather than demands.
        s.milestonesTotal == 0 ->
            if (s.ageDays > STALE_AGE_DAYS) {
                "Still just an intention after ${weeks(s.ageDays)}. That is allowed. If you want it " +
                    "to move, one concrete step is usually enough to start."
            } else {
                "Named, not yet planned. $coachName would rather you start with one step you are " +
                    "sure of than a plan you are guessing at."
            }

        s.ageDays <= 2 ->
            "Fresh. \"${s.nextStep}\" is the first move."

        else ->
            "Nothing ticked yet. \"${s.nextStep}\" is the one that starts it."
    }

    /**
     * The wheel score, but only when it changes what the user should do.
     *
     * Mentioning a healthy area every time would be noise; mentioning the weakest one is the whole
     * reason a goal carries an area at all.
     */
    private fun areaClause(s: GoalSnapshot): String? = when {
        !s.areaIsLowest -> null
        s.status == GoalStatus.COMPLETED -> "${s.areaName} was your lowest area, so this one counted."
        else -> "${s.areaName} is your lowest area right now, which makes this the goal with the " +
            "most to move."
    }

    private fun weeks(days: Int): String {
        val w = days / 7
        return if (w < 2) "$days days" else "$w weeks"
    }

    private fun plural(n: Int) = if (n == 1) "" else "s"

    private fun isAre(n: Int) = if (n == 1) "is" else "are"
}
