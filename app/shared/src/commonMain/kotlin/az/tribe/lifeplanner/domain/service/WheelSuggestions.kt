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
 * All nine segments are written. Joy is deliberately not: it is a reading of the whole wheel
 * rather than a slice with actions of its own, and "have more joy" is not advice.
 *
 * The rule while writing these was that each set had to be specific enough that it could only have
 * been written for that area. The failure mode for authored copy like this is the same three tips
 * with the nouns swapped, and a test asserts no two areas share a line.
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
     * Rubric: your work matters to you and you can say why, in a sentence, without hesitating.
     *
     * So this is about being able to answer, not about liking your job. Plenty of people with good
     * jobs score low here and the fix is articulation, not resignation. Nothing below tells anyone
     * to quit: that is a life decision an app has no standing to push, and the ones who should are
     * rarely the ones reading a tip.
     */
    private val mission = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.GENTLE,
                action = "Write the sentence. Why this work, in one line, no hedging.",
                because = "If it takes a paragraph, that is the finding.",
            ),
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.GENTLE,
                action = "Name the part of last week that felt worth doing.",
                because = "Purpose is easier to spot in specifics than in the abstract.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.MODERATE,
                action = "Find one person your work actually helped this month.",
                because = "Impact gets abstract fast when you only see your own end of it.",
            ),
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.MODERATE,
                action = "Write down what you would want to be doing instead. Just the description.",
                because = "Naming it is not committing to it, and vagueness is what keeps it stuck.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.SERIOUS,
                action = "Separate what you dislike into the work itself and everything around it.",
                because = "Those two get blamed on each other for years, and only one of them needs the bigger change.",
            ),
            WheelSuggestion(
                area = WheelArea.MISSION,
                urgency = NudgeUrgency.SERIOUS,
                action = "Name one thing you would keep, even now.",
                because = "At this point the picture is usually all-or-nothing, and it rarely is.",
            ),
        ),
    )

    /**
     * Rubric: nothing with your family is sitting unresolved, whether you are close or keep your
     * distance.
     *
     * Deliberately does not push anyone towards contact. Some distance is the resolution, and a
     * card telling an estranged person to call their mother would be both presumptuous and, for
     * some, actively harmful. Everything here works whether the answer is closer or further.
     */
    private val family = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.GENTLE,
                action = "Ask one of them something you have never actually asked.",
                because = "Long relationships run on assumptions that stopped being checked years ago.",
            ),
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.GENTLE,
                action = "Say the appreciative thing out loud rather than assuming it is known.",
                because = "It usually is not, and it is cheap to say.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.MODERATE,
                action = "Name the thing you keep steering around.",
                because = "You do not have to raise it. Knowing what it is makes the steering conscious.",
            ),
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.MODERATE,
                action = "Decide what contact you actually want, rather than what is expected.",
                because = "Most of the strain here is the gap between those two.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.SERIOUS,
                action = "Write what you would say if there were no consequences. For you, not to send.",
                because = "Unsent is still said, and the drafting is most of the relief.",
            ),
            WheelSuggestion(
                area = WheelArea.FAMILY,
                urgency = NudgeUrgency.SERIOUS,
                action = "Decide what you want the distance to be, and let that be the answer.",
                because = "Distance can be the resolution. Undecided distance is what keeps costing you.",
            ),
        ),
    )

    /**
     * Rubric: your romantic life is where you want it, whether that means a good relationship or a
     * contented single one.
     *
     * That clause is load-bearing and the copy has to hold it. Nothing here assumes a partner, and
     * nothing treats single as a problem to solve — a set of tips about dating aimed at someone
     * happily unattached would tell them the app was not listening. A test asserts none of it
     * assumes coupling.
     *
     * This is also the one area with no signal at all behind it, so every score here was typed by
     * the user. That earns more deference, not less.
     */
    private val romance = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.GENTLE,
                action = "Decide what you actually want here, separately from what you are told to want.",
                because = "Most of the dissatisfaction in this area is borrowed.",
            ),
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.GENTLE,
                action = "Do the thing you would do if this part of life were already settled.",
                because = "Waiting for it to resolve first is how years go.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.MODERATE,
                action = "Say the small unsaid thing, whoever it is to.",
                because = "The backlog is what makes it feel heavy, not any single item in it.",
            ),
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.MODERATE,
                action = "Work out whether you are lonely or just unpartnered. They need different things.",
                because = "Treating one as the other is why the usual advice does not land.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.SERIOUS,
                action = "Put one hour this week into something that has nothing to do with this.",
                because = "Attention spent entirely here makes it larger, not better.",
            ),
            WheelSuggestion(
                area = WheelArea.ROMANCE,
                urgency = NudgeUrgency.SERIOUS,
                action = "Ask whether the standard you are measuring against is one you chose.",
                because = "A low score against a borrowed standard is not the same as a problem.",
            ),
        ),
    )

    /**
     * Rubric: you have a practice that puts your problems in proportion, and you actually do it.
     *
     * Practice, not belief. The app has no business nudging anyone towards or away from a faith, so
     * nothing here names one. Proportion is the measurable part and it is what the rubric asks for.
     */
    private val spiritual = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Do the practice you already know works, today rather than when things calm down.",
                because = "It is the first thing dropped and the thing that made the rest manageable.",
            ),
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Spend ten minutes somewhere that makes your week look small.",
                because = "Scale is the whole mechanism, and it is mostly a matter of location.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Pick a fixed time rather than waiting to feel like it.",
                because = "Nobody feels like it. That is why the ones who keep a practice schedule it.",
            ),
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Notice what you reach for when things get heavy, honestly.",
                because = "Whatever it is has been your practice. Worth knowing if you want it to be.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Sit still for five minutes without fixing anything.",
                because = "When everything is urgent, five unproductive minutes is the entire exercise.",
            ),
            WheelSuggestion(
                area = WheelArea.SPIRITUAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Name one thing that will still matter in a year.",
                because = "Proportion is not a mood. It is a question you can answer on paper.",
            ),
        ),
    )

    /**
     * Rubric: you move most days, sleep properly, and your body is not the thing holding you back.
     *
     * No prescriptions, no numbers, nothing about weight. The app can see steps and sleep, which is
     * exactly why the copy should not moralise about them: being shown a figure and then lectured
     * about it is how people stop connecting their health data.
     */
    private val physical = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Go to bed at the time you already know you should.",
                because = "Sleep moves more of this than any change to the exercise does.",
            ),
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Take the version of today that involves walking somewhere.",
                because = "Consistency here is a series of small route choices, not a plan.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Move for ten minutes today. Anything, badly, counts.",
                because = "The block is almost always starting, and ten minutes is under the bar.",
            ),
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Pick the one thing that most reliably wrecks your sleep and change only that.",
                because = "One variable is a change you can keep.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Get outside once today, even briefly.",
                because = "When everything else is too much, this is the one that still works.",
            ),
            WheelSuggestion(
                area = WheelArea.PHYSICAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Book the appointment you have been putting off.",
                because = "If something specific is wrong, no amount of routine is the answer to it.",
            ),
        ),
    )

    /**
     * Rubric: you are better at something than you were six months ago, and you could name what.
     *
     * The naming is the point. Plenty of people are learning constantly and score low here because
     * nothing has been consolidated enough to point at, and the fix for that is finishing, not
     * starting more.
     */
    private val growth = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.GENTLE,
                action = "Name what you are better at than six months ago.",
                because = "If nothing comes, that is the finding rather than a failure.",
            ),
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.GENTLE,
                action = "Finish the one you are furthest through before opening another.",
                because = "Half-done things feel like progress and do not count as any.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.MODERATE,
                action = "Pick one thing to be visibly better at by winter. Only one.",
                because = "Three at once is the usual reason nothing lands.",
            ),
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.MODERATE,
                action = "Teach someone the last thing you learned.",
                because = "It is the quickest way to find out whether you actually learned it.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.SERIOUS,
                action = "Spend twenty minutes on something that is not work and not a screen.",
                because = "Growth stalls when every hour is already accounted for.",
            ),
            WheelSuggestion(
                area = WheelArea.GROWTH,
                urgency = NudgeUrgency.SERIOUS,
                action = "Read one thing properly rather than ten things badly.",
                because = "The feeling of falling behind is usually caused by the skimming, not cured by it.",
            ),
        ),
    )

    /**
     * Rubric: your head is a decent place to spend the day, and hard days pass instead of settling
     * in.
     *
     * The most careful set here, for two reasons.
     *
     * It must not read as treatment. This is an app suggesting small things, not a clinician, and
     * copy that sounds like therapy invites people to substitute it for the real thing. Nothing
     * below diagnoses, and nothing implies a low score is an illness.
     *
     * And at the serious end it has to say the true thing rather than the encouraging one. Someone
     * whose head has been a bad place for weeks is not going to be fixed by a walk, and pretending
     * otherwise is how an app loses the person it most wanted to help. So the serious copy points
     * outward, plainly and without drama. A test asserts that it does.
     */
    private val mental = mapOf(
        NudgeUrgency.GENTLE to listOf(
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Put the thing you keep turning over onto paper, so it stops circling.",
                because = "Written down it has edges. In your head it is the whole room.",
            ),
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.GENTLE,
                action = "Protect one hour that nothing is allowed to be scheduled into.",
                because = "Most of this is pace rather than mood.",
            ),
        ),
        NudgeUrgency.MODERATE to listOf(
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Tell one person that this stretch has been harder than usual.",
                because = "Carrying it privately is most of the weight.",
            ),
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.MODERATE,
                action = "Take the whole evening off. Not to catch up, to actually stop.",
                because = "Rest that is really deferred work does not do the job.",
            ),
        ),
        NudgeUrgency.SERIOUS to listOf(
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Tell one person how bad it has actually been.",
                because = "Not to fix it. So that one other person knows the real level.",
            ),
            WheelSuggestion(
                area = WheelArea.MENTAL,
                urgency = NudgeUrgency.SERIOUS,
                action = "Talk to a doctor or a therapist about this stretch.",
                because = "Weeks of this is not something to out-discipline, and getting help is the practical move rather than the last one.",
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
            WheelArea.MISSION -> mission[urgency]
            WheelArea.FAMILY -> family[urgency]
            WheelArea.ROMANCE -> romance[urgency]
            WheelArea.SPIRITUAL -> spiritual[urgency]
            WheelArea.PHYSICAL -> physical[urgency]
            WheelArea.GROWTH -> growth[urgency]
            WheelArea.MENTAL -> mental[urgency]
            // Joy is a reading of the whole wheel, not a slice with actions of its own.
            WheelArea.JOY -> null
        } ?: return null
        if (options.isEmpty()) return null
        // Guards against a negative rotation, since a caller passing a day offset can hand us one.
        val index = ((rotation % options.size) + options.size) % options.size
        return options[index]
    }

    /** Areas with copy written. Everything else stays silent on purpose. */
    val covered: Set<WheelArea> = WheelArea.segments().toSet()
}
