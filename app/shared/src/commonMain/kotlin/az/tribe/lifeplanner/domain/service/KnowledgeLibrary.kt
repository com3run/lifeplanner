package az.tribe.lifeplanner.domain.service

/**
 * One curated, science-backed micro-lesson for the "For You" feed. [minLevel] gates content by the
 * user's gamification level so beginners get foundations and advanced users get deeper ideas.
 */
data class KnowledgeBit(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val minLevel: Int,
    val readMin: Int = 2,
)

/**
 * The curated knowledge library behind the feed. Pure data, no I/O. Content is rotated by day so the
 * feed stays fresh, and filtered by level so it matches where the user is. Sourced from the science
 * cards the app already shipped in the daily recap stories.
 */
object KnowledgeLibrary {

    val all: List<KnowledgeBit> = listOf(
        KnowledgeBit(
            "tip_2min_rule", "The 2-minute rule",
            "Any habit can be started in 2 minutes. 'Read before bed' becomes 'open the book.' 'Run 5km' becomes 'put on your shoes.' Make starting frictionless and momentum takes care of the rest.",
            "⏱️", minLevel = 1, readMin = 1,
        ),
        KnowledgeBit(
            "tip_66days", "It is not 21 days",
            "The idea that habits form in 21 days is a myth. Researchers at University College London found the real average is 66 days, and it ranges from 18 to 254 days depending on the person and the habit.",
            "📅", minLevel = 1, readMin = 2,
        ),
        KnowledgeBit(
            "tip_implementation_intention", "When X, I will Y",
            "Writing down 'I will [habit] at [time] in [place]' doubles or triples your follow-through. It is called an implementation intention, and it takes under 30 seconds to create.",
            "✍️", minLevel = 1, readMin = 1,
        ),
        KnowledgeBit(
            "tip_compound", "1% better every day",
            "Improve 1% each day for a year and you end up about 37 times better. Decline 1% each day and you drop close to zero. Tiny edges compound into massive results.",
            "📈", minLevel = 1, readMin = 2,
        ),
        KnowledgeBit(
            "tip_progress_principle", "Small wins fuel big ones",
            "Harvard research found the single biggest daily motivator is the progress principle: even tiny forward movement on meaningful work. Logging a small win lights up the same reward circuits as a major milestone.",
            "🏅", minLevel = 2, readMin = 2,
        ),
        KnowledgeBit(
            "tip_identity", "I am vs I want",
            "People who say 'I am a runner' stick to running more than those who say 'I want to run more.' Identity based habits are stickier, because every action becomes a vote for who you are.",
            "🪞", minLevel = 2, readMin = 2,
        ),
        KnowledgeBit(
            "tip_temptation_bundle", "Pair pain with pleasure",
            "Temptation bundling links a habit you struggle with to something you enjoy. Only listen to your favourite podcast while exercising. Only watch your show while folding laundry. It works.",
            "🎧", minLevel = 2, readMin = 2,
        ),
        KnowledgeBit(
            "tip_social_commitment", "Tell someone",
            "Publicly committing to a goal raises completion rates by up to 65%. Adding accountability, a friend, a coach, or even just logging it, pushes that toward 95%. Being seen changes the game.",
            "🤝", minLevel = 2, readMin = 2,
        ),
        KnowledgeBit(
            "tip_sleep_memory", "Sleep consolidates skills",
            "While you sleep, your brain replays the day and moves learning into long term memory. Skipping sleep after learning something new can erase up to 40% of what you studied. Sleep is part of the skill.",
            "💤", minLevel = 3, readMin = 2,
        ),
        KnowledgeBit(
            "tip_planning_fallacy", "You are too optimistic",
            "The planning fallacy is real: we underestimate how long things take, even with experience. The fix is simple, multiply your estimate by 1.5 and add a buffer. You will be closer to right.",
            "🗓️", minLevel = 3, readMin = 2,
        ),
        KnowledgeBit(
            "tip_goldilocks", "The Goldilocks zone",
            "Motivation peaks when a task sits just above your current ability, not too easy, not too hard. That sweet spot is why levelling up keeps things engaging.",
            "🎯", minLevel = 4, readMin = 2,
        ),
        KnowledgeBit(
            "tip_decision_fatigue", "Decisions drain you",
            "The more choices you make in a day, the weaker your willpower gets. High performers automate low stakes decisions like meals and routines to save energy for what matters.",
            "⚡", minLevel = 4, readMin = 2,
        ),
    )

    /**
     * Up to [count] bits the user has unlocked at [level], rotated by [daySeed] (use day-of-year) so
     * the feed shows different lessons each day without repeating until the pool is exhausted.
     */
    fun forLevel(level: Int, daySeed: Int, count: Int): List<KnowledgeBit> {
        val unlocked = all.filter { it.minLevel <= level }
        if (unlocked.isEmpty()) return emptyList()
        val start = ((daySeed % unlocked.size) + unlocked.size) % unlocked.size
        return (0 until minOf(count, unlocked.size)).map { i -> unlocked[(start + i) % unlocked.size] }
    }
}
