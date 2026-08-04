package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.WheelArea

/**
 * One thing to do about a weak area.
 *
 * [action] is imperative and small enough to finish today. A suggestion you cannot complete in one
 * sitting is a second goal, and someone whose Friends score is 3 does not need another goal.
 * [because] is the reason this, now — the same job [ActionOption.fitReason] does elsewhere, and the
 * part that stops it reading as a fortune cookie.
 */
data class WheelSuggestion(
    val area: WheelArea,
    val urgency: NudgeUrgency,
    val action: String,
    val because: String,
)

/**
 * Authored suggestions, chosen over generating them.
 *
 * Generating these would cost a call per view, need a network round trip on a screen that renders
 * instantly, and reliably produce the bland advice this is trying to avoid. Written text is free,
 * offline, and can be argued about before anyone reads it.
 *
 * **Only Friends is written.** Every other area returns null and the UI stays quiet, which is the
 * correct behaviour rather than a gap to paper over: filler advice would teach people to ignore the
 * card before the real copy arrived. Add areas one at a time, and make each one specific enough
 * that it could only have been written for that area.
 *
 * Each suggestion aims at the area's own rubric rather than at the idea of the area. The Friends
 * rubric is "someone knows what you are actually dealing with right now, not the version of you
 * from a year ago", so none of these say "make more friends". They are all about being known.
 */
object WheelSuggestions {

    private val friends = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.GENTLE,
                action = "Give one person the real answer to \"how are you\" today.",
                because = "Being known is not about frequency. It goes when the answer is always fine.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.GENTLE,
                action = "Tell someone about the thing you are in the middle of, before it resolves.",
                because = "Reporting things afterwards keeps people at the edge of your life.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.GENTLE,
                action = "Put one specific plan in the calendar. A date, not \"soon\".",
                because = "\"We should catch up\" is where most friendships quietly stall.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.MODERATE,
                action = "Message the person you have been meaning to message for weeks.",
                because = "You already know who. That is usually the whole problem, not who to pick.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.MODERATE,
                action = "Send one sentence about your actual week. No preamble, no apology for the gap.",
                because = "The apology is what makes it feel like a big message to write.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.MODERATE,
                action = "Ring someone instead of typing. Ten minutes is enough.",
                because = "Text keeps people updated. It does not do much for being known.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.SERIOUS,
                action = "Pick one name. Send anything at all, however short.",
                because = "When it has been a while, the message feels enormous to write and is small to receive.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.SERIOUS,
                action = "Reply to the oldest unanswered message you still feel bad about.",
                because = "The gap is doing more damage in your head than in theirs.",
            ),
            WheelSuggestion(
                area = WheelArea.FRIENDS,
                urgency = NudgeUrgency.SERIOUS,
                action = "Go somewhere other people are, without needing to talk to them.",
                because = "Some weeks the honest first step is not being alone in a room.",
            ),
        ),
    )

    /**
     * A suggestion for the area, or null when nothing is written for it yet.
     *
     * [rotation] picks between the options for an urgency. Pass a day number so the suggestion holds
     * for the day and changes tomorrow: a card that reshuffles on every recomposition reads as
     * noise, and one that never changes stops being read at all.
     */
    fun forArea(area: WheelArea, urgency: NudgeUrgency, rotation: Int = 0): WheelSuggestion? {
        val options = when (area) {
            WheelArea.FRIENDS -> friends[urgency]
            else -> null
        } ?: return null
        if (options.isEmpty()) return null
        // Guards against a negative rotation, since a caller passing a day offset can hand us one.
        val index = ((rotation % options.size) + options.size) % options.size
        return options[index]
    }

    /** Areas with copy written. Everything else stays silent on purpose. */
    val covered: Set<WheelArea> = setOf(WheelArea.FRIENDS)
}
