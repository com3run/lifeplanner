package az.tribe.lifeplanner.domain.service

/**
 * What a lesson is about. Used to match lessons to what the user is actually doing, so the feed can
 * surface the ones most relevant to them (building habits, stalling goals, poor sleep, and so on).
 */
enum class KnowledgeTopic {
    HABITS, GOALS, FOCUS, SLEEP, DECISIONS, MINDSET, PLANNING, MOTIVATION,
}

/**
 * One curated, science-backed lesson for the "For You" feed. [minLevel] gates content by the user's
 * gamification level so beginners get foundations and advanced users get deeper ideas.
 *
 * [body] is the one-line teaser shown on the feed card. [detail] is the full lesson shown on the
 * Learn detail screen: a few short paragraphs, a [takeaway] the user can act on, and a [source].
 * [topics] tag what the lesson is about so the recommender can match it to the user's activity.
 */
data class KnowledgeBit(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String,
    val minLevel: Int,
    val readMin: Int = 2,
    /** The expanded lesson, one entry per paragraph. Empty falls back to just [body]. */
    val detail: List<String> = emptyList(),
    /** A single, do-it-now action that turns the idea into practice. */
    val takeaway: String = "",
    /** Where the idea comes from, shown as quiet attribution. */
    val source: String? = null,
    /** What this lesson is about, most relevant topic first. Drives personalized recommendations. */
    val topics: List<KnowledgeTopic> = emptyList(),
)

/**
 * A themed learning path, a small ordered set of lessons on one subject. The Learn hub shows these
 * as collections with their own progress ("3 of 6 read") and a Continue button to the next unread
 * lesson, so learning feels continuous instead of a flat list. New lessons slot into a collection as
 * the library grows.
 */
data class KnowledgeCollection(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val lessonIds: List<String>,
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
            "⏱️", minLevel = 1, readMin = 2,
            detail = listOf(
                "Every habit has a gateway action, the smallest possible version of it. The 2-minute rule says to scale any new habit down until it takes two minutes or less to start. 'Do yoga' becomes 'roll out the mat.' 'Study for the exam' becomes 'open the notes.'",
                "This works because the hardest part of any habit is not the doing, it is the starting. Once you have shown up, momentum usually carries you further. You rarely roll out the mat and walk away, and even if you do, you have still cast a vote for the person who shows up.",
                "The goal at first is not to achieve, it is to become the kind of person who never misses. Master the art of showing up, then scale the effort back up once the habit feels automatic.",
            ),
            takeaway = "Pick one habit and shrink its starting step to under two minutes. Do only that step for the first week.",
            source = "James Clear, Atomic Habits",
            topics = listOf(KnowledgeTopic.HABITS),
        ),
        KnowledgeBit(
            "tip_66days", "It is not 21 days",
            "The idea that habits form in 21 days is a myth. Researchers at University College London found the real average is 66 days, and it ranges from 18 to 254 days depending on the person and the habit.",
            "📅", minLevel = 1, readMin = 2,
            detail = listOf(
                "The 21-day idea traces back to a 1960s surgeon who noticed patients took about three weeks to adjust to a new face. That was an observation about adjustment, not a law about habits, but the number stuck and spread.",
                "In 2009, researchers at University College London tracked people forming everyday habits. The average time for a behavior to become automatic was 66 days, with a range from 18 days to 254, depending on the person and how hard the habit was.",
                "The practical lesson is to expect the first two months to take conscious effort, and not to judge yourself if it takes longer. Notably, missing a single day did not reset anyone's progress in the study. Consistency over time mattered far more than any one day.",
            ),
            takeaway = "Commit to a new habit for at least two months before deciding whether it fits you.",
            source = "Lally et al., University College London, 2009",
            topics = listOf(KnowledgeTopic.HABITS),
        ),
        KnowledgeBit(
            "tip_implementation_intention", "When X, I will Y",
            "Writing down 'I will [habit] at [time] in [place]' doubles or triples your follow-through. It is called an implementation intention, and it takes under 30 seconds to create.",
            "✍️", minLevel = 1, readMin = 2,
            detail = listOf(
                "An implementation intention is a simple sentence: 'I will [behavior] at [time] in [location].' For example, 'I will meditate for one minute at 7am in the kitchen.' Naming the when and where in advance removes the moment of hesitation that usually kills a habit.",
                "In studies, people who wrote down exactly when and where they would act were two to three times more likely to follow through than those who only intended to. The plan does the remembering for you, so willpower is no longer the bottleneck.",
                "You can stack it onto something you already do: 'After I pour my morning coffee, I will write down one priority.' The existing habit becomes the cue for the new one, so you are not relying on memory or motivation.",
            ),
            takeaway = "Write one sentence now: 'I will ___ at ___ in ___,' for the habit you care about most.",
            source = "Peter Gollwitzer, implementation intentions",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.PLANNING),
        ),
        KnowledgeBit(
            "tip_compound", "1% better every day",
            "Improve 1% each day for a year and you end up about 37 times better. Decline 1% each day and you drop close to zero. Tiny edges compound into massive results.",
            "📈", minLevel = 1, readMin = 2,
            detail = listOf(
                "The math is striking. Get 1% better every day for a year and, compounded, you end up roughly 37 times better than you started. Get 1% worse each day and you decline nearly to zero. That is 1.01 to the power of 365 versus 0.99 to the power of 365.",
                "Habits are the compound interest of self-improvement. A single good day is almost invisible. The same day repeated for months is the difference between who you are and who you could be.",
                "This is also why setbacks feel discouraging: progress is not linear. You often work for weeks before results show, crossing what looks like a plateau. The gains were accumulating the whole time, just below the surface.",
            ),
            takeaway = "Aim for tiny and repeatable over big and rare. One small rep today beats a perfect plan tomorrow.",
            source = "The compounding of marginal gains",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.MINDSET),
        ),
        KnowledgeBit(
            "tip_progress_principle", "Small wins fuel big ones",
            "Harvard research found the single biggest daily motivator is the progress principle: even tiny forward movement on meaningful work. Logging a small win lights up the same reward circuits as a major milestone.",
            "🏅", minLevel = 2, readMin = 2,
            detail = listOf(
                "Harvard researchers analyzed thousands of daily diary entries from workers and found the single biggest driver of a good day was progress: any sense of moving forward on meaningful work. They called it the progress principle.",
                "The size of the step mattered less than its direction. A small win lights up the same reward circuitry as a large milestone, and it feeds the motivation to take the next step. Momentum, it turns out, is built from evidence that you are making it.",
                "The flip side is that small losses, feeling stuck or slipping backward, hurt motivation more than small wins help it. Protecting a sense of daily progress, and noticing it when it happens, is one of the most reliable ways to stay motivated.",
            ),
            takeaway = "Log your small wins. Making progress visible is a large part of what keeps it going.",
            source = "Amabile & Kramer, The Progress Principle",
            topics = listOf(KnowledgeTopic.MOTIVATION, KnowledgeTopic.GOALS),
        ),
        KnowledgeBit(
            "tip_identity", "I am vs I want",
            "People who say 'I am a runner' stick to running more than those who say 'I want to run more.' Identity based habits are stickier, because every action becomes a vote for who you are.",
            "🪞", minLevel = 2, readMin = 2,
            detail = listOf(
                "There is a quiet difference between 'I want to run' and 'I am a runner.' The first is a wish. The second is an identity, and identities are what we protect. Every time you act in line with who you believe you are, you reinforce it.",
                "The most durable habits are tied to identity. You do not have to force a runner to lace up; it is simply what they do. So the useful question is not only 'what do I want to achieve?' but 'who do I want to become?'",
                "Each small action is a vote for a type of person. You do not need a unanimous result, just a majority. Two workouts do not make you fit, but they are two votes for 'I am someone who trains.' Cast enough of them and the belief becomes true.",
            ),
            takeaway = "Rephrase your goal as an identity: not 'I want to write more' but 'I am a writer.' Then act like one, once.",
            source = "Identity-based habits",
            topics = listOf(KnowledgeTopic.MINDSET, KnowledgeTopic.HABITS),
        ),
        KnowledgeBit(
            "tip_temptation_bundle", "Pair pain with pleasure",
            "Temptation bundling links a habit you struggle with to something you enjoy. Only listen to your favourite podcast while exercising. Only watch your show while folding laundry. It works.",
            "🎧", minLevel = 2, readMin = 2,
            detail = listOf(
                "Temptation bundling ties a habit you avoid to something you crave, so the two arrive together. You only listen to your favorite podcast while at the gym. You only watch your show while folding laundry. The thing you want becomes the reward for the thing you should do.",
                "Behavioral scientist Katherine Milkman tested this. People who could only listen to gripping audiobooks at the gym worked out noticeably more often than those who could listen anywhere. The bundle turned the hard thing into something to look forward to.",
                "The trick is to make the pleasure exclusive to the habit. If you can watch your show anytime, it loses its pull as a reward. Reserve it, and the habit borrows its appeal.",
            ),
            takeaway = "Pick one guilty pleasure and allow it only while doing a habit you keep skipping.",
            source = "Katherine Milkman, temptation bundling",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.FOCUS),
        ),
        KnowledgeBit(
            "tip_social_commitment", "Tell someone",
            "Publicly committing to a goal raises completion rates by up to 65%. Adding accountability, a friend, a coach, or even just logging it, pushes that toward 95%. Being seen changes the game.",
            "🤝", minLevel = 2, readMin = 2,
            detail = listOf(
                "Saying a goal out loud to another person changes your odds. Public commitment raises follow-through, because now your word and your reputation are on the line, not just your private intention.",
                "Often-cited work on goal setting found that people who merely thought about a goal had modest success, while those who wrote it down, told a friend, and sent weekly updates hit their goals far more often. Layering accountability onto commitment compounds the effect.",
                "You do not need a crowd. One person who will ask 'how did it go?' is enough. Even logging your progress somewhere you will see it works, because being witnessed, even by your future self, changes behavior.",
            ),
            takeaway = "Tell one person what you are working on this week, and ask them to check in on Friday.",
            source = "Goal commitment and accountability research",
            topics = listOf(KnowledgeTopic.GOALS, KnowledgeTopic.MOTIVATION),
        ),
        KnowledgeBit(
            "tip_sleep_memory", "Sleep consolidates skills",
            "While you sleep, your brain replays the day and moves learning into long term memory. Skipping sleep after learning something new can erase up to 40% of what you studied. Sleep is part of the skill.",
            "💤", minLevel = 3, readMin = 2,
            detail = listOf(
                "Learning does not end when you close the book. While you sleep, your brain replays the day's activity and moves fragile new memories into long-term storage. This is called consolidation, and it is when practice actually sticks.",
                "Studies show sleep after learning protects it. Skip sleep the night after studying something new and you can lose a large share of what you took in. Deep sleep strengthens facts, and REM sleep helps with skills and problem solving.",
                "The implication is practical. A shorter practice session followed by real sleep beats a long one that steals from it. Sleep is not the reward for the work, it is part of the work.",
            ),
            takeaway = "Protect your sleep the night after you learn something important. It is where the gains get saved.",
            source = "Sleep and memory consolidation research",
            topics = listOf(KnowledgeTopic.SLEEP, KnowledgeTopic.FOCUS),
        ),
        KnowledgeBit(
            "tip_planning_fallacy", "You are too optimistic",
            "The planning fallacy is real: we underestimate how long things take, even with experience. The fix is simple, multiply your estimate by 1.5 and add a buffer. You will be closer to right.",
            "🗓️", minLevel = 3, readMin = 2,
            detail = listOf(
                "The planning fallacy is our tendency to underestimate how long a task will take, even when we have done similar tasks before and even when we know we usually run over. Optimism about the future somehow survives the evidence of the past.",
                "Kahneman and Tversky named it after noticing that projects, from student essays to national infrastructure, routinely blow past their estimates. We picture the smooth version, not the interruptions, mistakes, and surprises that always show up.",
                "The fix is to look outside your own optimism. Ask how long things like this have actually taken you before, then use that number. A rough rule: take your gut estimate, multiply by 1.5, and add a buffer.",
            ),
            takeaway = "For your next deadline, base the estimate on how long the last similar task really took, not how long you hope.",
            source = "Kahneman & Tversky, the planning fallacy",
            topics = listOf(KnowledgeTopic.PLANNING, KnowledgeTopic.GOALS),
        ),
        KnowledgeBit(
            "tip_goldilocks", "The Goldilocks zone",
            "Motivation peaks when a task sits just above your current ability, not too easy, not too hard. That sweet spot is why levelling up keeps things engaging.",
            "🎯", minLevel = 4, readMin = 2,
            detail = listOf(
                "Motivation is highest when a task is just manageable, right at the edge of your current ability. Too easy and you drift into boredom, too hard and you tip into anxiety. The sweet spot in between, not too hot and not too cold, is the Goldilocks zone.",
                "It is closely tied to flow, the state of full absorption where time disappears. Flow tends to appear when the challenge of a task slightly exceeds your skill, pulling you to stretch without overwhelming you.",
                "This also explains why leveling up keeps things engaging. As you improve, the task that once challenged you becomes easy, so you need a slightly harder version to stay in the zone. Progress means the bar keeps rising with you.",
            ),
            takeaway = "If a habit feels boring, make it a little harder. If it feels overwhelming, make it a little easier. Aim for pleasantly difficult.",
            source = "The Goldilocks rule and flow",
            topics = listOf(KnowledgeTopic.MOTIVATION, KnowledgeTopic.GOALS),
        ),
        KnowledgeBit(
            "tip_decision_fatigue", "Decisions drain you",
            "The more choices you make in a day, the weaker your willpower gets. High performers automate low stakes decisions like meals and routines to save energy for what matters.",
            "⚡", minLevel = 4, readMin = 2,
            detail = listOf(
                "Every decision you make draws from the same limited well of mental energy. As the day fills with choices, from what to eat to what to reply, the quality of your decisions tends to drop. Psychologists call this decision fatigue.",
                "In one well-known study, judges granted parole far more often early in the day and right after breaks than late in a long session, when depleted minds defaulted to the safe 'no.' The cases had not changed, the judges' reserves had.",
                "High performers protect their willpower by removing trivial decisions. They eat similar meals, plan the day in advance, and turn recurring choices into routines. Automating the small stuff leaves energy for the choices that actually matter.",
            ),
            takeaway = "Turn one recurring decision into a default this week, so you spend that energy on something that matters.",
            source = "Decision fatigue research",
            topics = listOf(KnowledgeTopic.DECISIONS, KnowledgeTopic.FOCUS),
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

    /** The lesson with this [id], or null if it is not in the library (e.g. a stale deep link). */
    fun byId(id: String): KnowledgeBit? = all.firstOrNull { it.id == id }

    /**
     * The learning paths, in the order they should appear in the hub. Every lesson belongs to exactly
     * one collection; as content grows a path should build toward roughly eight lessons.
     */
    val collections: List<KnowledgeCollection> = listOf(
        KnowledgeCollection(
            "col_habits", "Building habits that stick",
            "The science of starting small and staying consistent.",
            "🧱",
            listOf(
                "tip_2min_rule", "tip_66days", "tip_implementation_intention",
                "tip_compound", "tip_identity", "tip_temptation_bundle",
            ),
        ),
        KnowledgeCollection(
            "col_motivation", "Staying motivated",
            "What actually keeps you going when willpower fades.",
            "🔥",
            listOf("tip_progress_principle", "tip_social_commitment", "tip_goldilocks"),
        ),
        KnowledgeCollection(
            "col_mind", "Focus and the mind",
            "Sleep, attention, and the limits of your willpower.",
            "🧠",
            listOf("tip_sleep_memory", "tip_planning_fallacy", "tip_decision_fatigue"),
        ),
    )

    /** The lessons of a collection, in order, resolved to real [KnowledgeBit]s. */
    fun lessonsOf(collection: KnowledgeCollection): List<KnowledgeBit> =
        collection.lessonIds.mapNotNull { byId(it) }

    /** The collection a lesson belongs to, or null if it is not filed under one. */
    fun collectionOf(lessonId: String): KnowledgeCollection? =
        collections.firstOrNull { lessonId in it.lessonIds }
}
