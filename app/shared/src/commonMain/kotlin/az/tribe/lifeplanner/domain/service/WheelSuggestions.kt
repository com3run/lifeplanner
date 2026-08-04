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
 * **Only Friends and Money are written.** Every other area returns null and the UI stays quiet,
 * which is the behaviour I want rather than a gap to paper over: filler advice would teach people
 * to ignore the card before the real copy arrived. Add areas one at a time, and make each one
 * specific enough that it could only have been written for that area.
 *
 * Each set aims at that area's own rubric rather than at the idea of the area, which is what keeps
 * them from collapsing into the same three tips with the nouns swapped. Friends is "someone knows
 * what you are actually dealing with right now", so none of it says make more friends; it is all
 * about being known. Money is "not a daily worry, an unexpected bill would be annoying rather than
 * frightening", so none of it is about earning more; it is all about knowing numbers and absorbing
 * shocks.
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
     * Money is the easiest area to write badly. Two failure modes to stay out of.
     *
     * The first is assuming slack. "Cancel a subscription" or "save ten percent" is a sensible tip
     * to someone with room and an insult to someone without, and the app cannot tell which it is
     * talking to. So none of these assume there is spare money — the ones that mention putting
     * something aside deliberately do not name an amount.
     *
     * The second is drifting into advice we have no business giving. Where to put money so it grows
     * is regulated in most places and none of our business in the rest. Nothing here goes near it.
     *
     * The rubric is "money is not a daily worry, an unexpected bill would be annoying rather than
     * frightening", so these aim at the worry and the shock absorption rather than at the balance.
     * Knowing a number is the recurring theme, because the fog costs more than the figure usually
     * turns out to.
     */
    private val money = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.GENTLE,
                action = "Work out what one ordinary month actually costs you.",
                because = "Most people are guessing, and the guess is what the worry attaches to.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.GENTLE,
                action = "Find one recurring charge you have stopped noticing.",
                because = "The ones that stop being visible are the ones worth a second look.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.GENTLE,
                action = "Move your buffer somewhere you do not see day to day.",
                because = "A balance you check casually is one you spend casually.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.MODERATE,
                action = "Add up one month of essentials. Just the number, no plan yet.",
                because = "You cannot aim at a target you have never named.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.MODERATE,
                action = "Find the exact date of the bill that always seems to ambush you.",
                because = "Half of what that one costs you is the surprise, and the date is free.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.MODERATE,
                action = "Put something aside this week. The amount matters less than that it happened.",
                because = "A buffer is a habit before it is a sum.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.SERIOUS,
                action = "Write down everything you owe and to whom, in one place.",
                because = "Scattered across your head it is unbounded. On one page it has a size.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.SERIOUS,
                action = "Open the account and look at the real number.",
                because = "Not looking has its own cost, and it compounds quietly.",
            ),
            WheelSuggestion(
                area = WheelArea.MONEY,
                urgency = NudgeUrgency.SERIOUS,
                action = "Ask one company you owe what they can rearrange.",
                because = "Almost all of them would rather move a date than chase you. Asking is routine to them.",
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
            WheelArea.MONEY -> money[urgency]
            else -> null
        } ?: return null
        if (options.isEmpty()) return null
        // Guards against a negative rotation, since a caller passing a day offset can hand us one.
        val index = ((rotation % options.size) + options.size) % options.size
        return options[index]
    }

    /** Areas with copy written. Everything else stays silent on purpose. */
    val covered: Set<WheelArea> = setOf(WheelArea.FRIENDS, WheelArea.MONEY)
}
