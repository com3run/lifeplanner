package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.data.repository.KnowledgeContentStore
import az.tribe.lifeplanner.domain.enum.BadgeType

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

    /**
     * The live library: whatever Supabase last published, falling back to [bundledLessons] until
     * the fetcher has anything to offer. Every existing caller reads through here, so moving the
     * content to the server changed no call site.
     */
    val all: List<KnowledgeBit>
        get() = KnowledgeContentStore.currentLessons(bundledLessons)

    /**
     * The lessons compiled into this build. They are the offline floor and the first-launch content,
     * not the source of truth: edit lessons in Supabase (see `supabase/knowledge_lessons.sql`), and
     * regenerate this seed with `KnowledgeSeedGeneratorTest` if you change them here.
     */
    val bundledLessons: List<KnowledgeBit> = listOf(
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
        KnowledgeBit(
            "tip_never_twice", "Never miss twice",
            "Missing one day is an accident. Missing two is the start of a new habit. The rule that protects a streak is not perfection, it is getting back the very next day.",
            "🔁", minLevel = 1, readMin = 2,
            detail = listOf(
                "Everyone misses. Travel, illness, a day that gets away from you. The research on habit formation is clear that a single missed day does almost nothing to the habit you are building, so the guilt is misplaced.",
                "What does damage is the second miss, and then the third. That is the point where the old behavior quietly reclaims the slot. So the rule worth keeping is not 'never miss', which no one manages, but 'never miss twice'.",
                "This also changes how a bad day feels. Instead of a broken streak and a reason to give up, it becomes one data point, and the only thing that matters is what you do tomorrow.",
            ),
            takeaway = "If you missed yesterday, do the smallest possible version today. Getting back matters more than the size of the rep.",
            source = "Habit formation research on lapses",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.MINDSET),
        ),
        KnowledgeBit(
            "tip_habit_stacking", "Anchor it to something you already do",
            "New habits stick better when they hang off an existing one. 'After I brush my teeth, I will meditate for one minute.' The old habit becomes the reminder, so you stop relying on memory.",
            "🔗", minLevel = 1, readMin = 2,
            detail = listOf(
                "The hardest part of a new habit is remembering it at the right moment. Habit stacking solves that by attaching the new behavior to something already automatic: 'After [current habit], I will [new habit].'",
                "Your existing routine is full of reliable cues you never have to think about, brushing your teeth, pouring coffee, closing your laptop. Borrowing one of those means the reminder is built into your day rather than into your willpower.",
                "Pick an anchor that happens at the right time and at the right frequency. If you want a daily habit, anchor it to something you genuinely do daily, and keep the new habit small enough that the anchor can carry it.",
            ),
            takeaway = "Write one sentence: 'After I ___, I will ___.' Use something you already do without fail.",
            source = "BJ Fogg, Tiny Habits",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.PLANNING),
        ),
        KnowledgeBit(
            "tip_light_anchor", "Morning light sets your clock",
            "Your body clock is set mostly by light, not by bedtime. Ten minutes of daylight soon after waking makes falling asleep that night noticeably easier.",
            "🌅", minLevel = 2, readMin = 2,
            detail = listOf(
                "Your circadian rhythm is anchored by light hitting the eyes, especially in the morning. Bright daylight shortly after waking tells the body when 'day' starts, which in turn sets when melatonin rises that evening.",
                "This is why a consistent wake time does more for sleep than a consistent bedtime. Get light early and the sleepy signal arrives on schedule; sleep in behind curtains and it drifts later.",
                "Outdoor light is far brighter than indoor lighting even on an overcast day, so stepping outside beats sitting by a window. Ten minutes is enough to matter, and it costs nothing.",
            ),
            takeaway = "Get outside for ten minutes within an hour of waking, before you reach for a screen.",
            source = "Circadian rhythm and light exposure research",
            topics = listOf(KnowledgeTopic.SLEEP, KnowledgeTopic.HABITS),
        ),
        KnowledgeBit(
            "tip_wind_down", "Your brain needs a runway",
            "Sleep is not a switch. The hour before bed is when the body starts the process, and bright screens and hard problems keep it from starting at all.",
            "🌙", minLevel = 2, readMin = 2,
            detail = listOf(
                "Falling asleep is a gradual handover, not a switch you flip. Core temperature drops, melatonin rises, and attention loosens. All of that takes time, and it will not begin while you are still solving problems or staring at a bright screen.",
                "A wind-down routine gives that process a runway. The specifics matter less than the consistency: dimmer light, no work, something undemanding. Repeated nightly, the routine itself becomes a cue that sleep is coming.",
                "If your mind races once the lights are off, that is often unfinished thinking looking for a place to go. Writing tomorrow's list before bed reliably shortens the time it takes to fall asleep.",
            ),
            takeaway = "Pick a fixed time to stop working, and write tomorrow's three priorities before you do.",
            source = "Sleep onset and pre-sleep cognition research",
            topics = listOf(KnowledgeTopic.SLEEP, KnowledgeTopic.PLANNING),
        ),
        KnowledgeBit(
            "tip_attention_residue", "Switching costs more than you think",
            "Part of your attention stays behind on the last task for several minutes after you switch. Frequent switching means you are rarely running at full capacity on anything.",
            "🧲", minLevel = 2, readMin = 2,
            detail = listOf(
                "When you move from one task to another, a portion of your attention stays stuck on the previous one. Researcher Sophie Leroy called this attention residue, and it explains why a quick check of messages costs far more than the minute it took.",
                "The residue is worse when the first task was left unfinished or unclear. An interrupted piece of work keeps running in the background, quietly consuming the capacity you are trying to give to the new thing.",
                "The practical fix is to close loops before you switch, even artificially: note where you stopped and what comes next. Batching similar work and protecting one uninterrupted block beats scattering the same minutes across the day.",
            ),
            takeaway = "Before switching tasks, write one line on where you left off. It clears the loop so the next task gets your full attention.",
            source = "Sophie Leroy, attention residue",
            topics = listOf(KnowledgeTopic.FOCUS, KnowledgeTopic.PLANNING),
        ),
        KnowledgeBit(
            "tip_breath_pause", "The exhale calms you",
            "Slowing your out-breath tips the nervous system toward calm. It is the fastest deliberate way to lower arousal, and it takes about a minute.",
            "🫁", minLevel = 1, readMin = 2,
            detail = listOf(
                "Breathing is one of the few automatic systems you can take over at will, and the out-breath is the lever. Heart rate rises slightly as you inhale and falls as you exhale, so lengthening the exhale nudges the whole system toward rest.",
                "This is why paced breathing patterns work: they are all variations on making the out-breath as long as, or longer than, the in-breath. A few slow cycles are usually enough to feel the shift.",
                "It is most useful as a transition, between meetings, before a hard conversation, at the start of focused work. Not a cure for stress, but a reliable way to change state in under a minute.",
            ),
            takeaway = "Before your next difficult task, take six breaths where the exhale is twice as long as the inhale.",
            source = "Respiratory physiology and vagal tone research",
            topics = listOf(KnowledgeTopic.MINDSET, KnowledgeTopic.FOCUS),
        ),
        KnowledgeBit(
            "tip_self_compassion", "Being hard on yourself backfires",
            "Self-criticism after a slip predicts giving up. Self-compassion predicts getting back to it. Being kind to yourself is the more effective strategy, not the softer one.",
            "🤍", minLevel = 3, readMin = 2,
            detail = listOf(
                "The instinct after breaking a streak is to be harsh, on the theory that guilt will drive you back. The evidence points the other way: harsh self-criticism after a lapse predicts more avoidance, not more effort.",
                "Kristin Neff's work on self-compassion found that people who responded to a slip with understanding rather than judgment returned to the behavior sooner and stuck with it longer. Shame makes you want to avoid the whole subject, including the habit.",
                "This is not permission to drift. Self-compassion pairs kindness with honesty: acknowledge the miss, skip the character verdict, and decide the next small step.",
            ),
            takeaway = "After a missed day, write what you would say to a friend in the same spot, then follow your own advice.",
            source = "Kristin Neff, self-compassion research",
            topics = listOf(KnowledgeTopic.MINDSET, KnowledgeTopic.MOTIVATION),
        ),
        KnowledgeBit(
            "tip_fresh_start", "Fresh starts really do help",
            "Mondays, birthdays, and the first of the month genuinely raise the odds of starting something. The effect is real, and it is worth spending rather than waiting for.",
            "🗓️", minLevel = 3, readMin = 2,
            detail = listOf(
                "Researchers at Wharton found that people are measurably more likely to pursue a goal right after a temporal landmark: a Monday, the start of a month, a birthday. They called it the fresh start effect.",
                "The mechanism is psychological distance. A landmark creates a break between the old you who kept slipping and the new one starting now, which makes the goal feel more achievable.",
                "The catch is that waiting for a landmark is a form of delay, and the effect fades if the plan behind it is vague. Use the date for the motivation, but attach a concrete, small first action to it.",
            ),
            takeaway = "Name the next landmark on your calendar and decide now what tiny action starts that day.",
            source = "Dai, Milkman & Riis, the fresh start effect",
            topics = listOf(KnowledgeTopic.MOTIVATION, KnowledgeTopic.PLANNING),
        ),
        KnowledgeBit(
            "tip_friction", "Make it easier or harder",
            "Behaviour follows the path of least resistance. Twenty seconds of added friction is often enough to stop a habit you want to break, and removing twenty is enough to start one.",
            "🪤", minLevel = 2, readMin = 2,
            detail = listOf(
                "Most behavior is decided by convenience rather than intention. The habit that is one tap away wins over the one that takes three steps, regardless of which you value more.",
                "Shawn Achor described a 20-second rule: add about twenty seconds of friction to a habit you want less of, and remove about twenty from one you want more of. Put the guitar on its stand; put the phone in another room.",
                "This is more reliable than resolve, because it works when you are tired and not thinking clearly, which is exactly when habits are decided.",
            ),
            takeaway = "Change one thing in your environment tonight so tomorrow's habit is easier to start than to skip.",
            source = "Shawn Achor, the 20-second rule",
            topics = listOf(KnowledgeTopic.HABITS, KnowledgeTopic.DECISIONS),
        ),
        KnowledgeBit(
            "tip_premortem", "Assume it failed, then ask why",
            "Before committing, imagine it is three months later and the plan collapsed. Naming the reasons in advance surfaces risks that optimism hides.",
            "🔮", minLevel = 4, readMin = 2,
            detail = listOf(
                "A premortem inverts the usual review. Instead of asking what could go wrong, you assume it already did and ask why. Gary Klein found this framing surfaces concerns people otherwise keep to themselves.",
                "The shift matters because 'what might go wrong' invites reassurance, while 'it failed, explain how' invites specifics. The imagined certainty of failure gives people permission to name doubts.",
                "Applied to a habit, it is quick: picture yourself having quit in three weeks, then list the reasons. Most will be obvious in hindsight and easy to design around now.",
            ),
            takeaway = "Imagine this habit collapsed in a month. Write the three likeliest reasons, then remove one of them today.",
            source = "Gary Klein, the premortem",
            topics = listOf(KnowledgeTopic.DECISIONS, KnowledgeTopic.PLANNING),
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

    /** The published learning paths, falling back to [bundledCollections]. */
    val collections: List<KnowledgeCollection>
        get() = KnowledgeContentStore.currentCollections(bundledCollections)

    /**
     * The learning paths, in the order they should appear in the hub. Every lesson belongs to exactly
     * one collection; as content grows a path should build toward roughly eight lessons.
     */
    val bundledCollections: List<KnowledgeCollection> = listOf(
        KnowledgeCollection(
            "col_habits", "Building habits that stick",
            "The science of starting small and staying consistent.",
            "🧱",
            listOf(
                "tip_2min_rule", "tip_66days", "tip_implementation_intention",
                "tip_compound", "tip_identity", "tip_temptation_bundle",
                "tip_habit_stacking", "tip_friction",
            ),
        ),
        KnowledgeCollection(
            "col_motivation", "Staying motivated",
            "What actually keeps you going when willpower fades.",
            "🔥",
            listOf(
                "tip_progress_principle", "tip_social_commitment", "tip_goldilocks",
                "tip_never_twice", "tip_self_compassion", "tip_fresh_start",
            ),
        ),
        KnowledgeCollection(
            "col_mind", "Focus and the mind",
            "Attention, decisions, and the limits of your willpower.",
            "🧠",
            listOf(
                "tip_planning_fallacy", "tip_decision_fatigue", "tip_attention_residue",
                "tip_breath_pause", "tip_premortem",
            ),
        ),
        KnowledgeCollection(
            "col_rest", "Rest and recovery",
            "Sleep is where the day's practice actually gets saved.",
            "🌙",
            listOf("tip_sleep_memory", "tip_light_anchor", "tip_wind_down"),
        ),
    )

    /** The lessons of a collection, in order, resolved to real [KnowledgeBit]s. */
    fun lessonsOf(collection: KnowledgeCollection): List<KnowledgeBit> =
        collection.lessonIds.mapNotNull { byId(it) }

    /** The collection a lesson belongs to, or null if it is not filed under one. */
    fun collectionOf(lessonId: String): KnowledgeCollection? =
        collections.firstOrNull { lessonId in it.lessonIds }

    /**
     * The badge earned for clearing a path. Null for any collection that has no badge of its own,
     * so adding a collection is not silently a promise of a reward.
     */
    fun badgeFor(collectionId: String): BadgeType? = when (collectionId) {
        "col_habits" -> BadgeType.LEARN_HABITS
        "col_motivation" -> BadgeType.LEARN_MOTIVATION
        "col_mind" -> BadgeType.LEARN_MIND
        "col_rest" -> BadgeType.LEARN_REST
        else -> null
    }

    /** How many lessons stand between the user and [badge], for the achievements progress bar. */
    fun lessonCountFor(badge: BadgeType): Int =
        collections.firstOrNull { badgeFor(it.id) == badge }?.lessonIds?.size ?: 0
}
