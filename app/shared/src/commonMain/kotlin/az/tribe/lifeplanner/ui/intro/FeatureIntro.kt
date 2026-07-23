package az.tribe.lifeplanner.ui.intro

/**
 * The one-screen explanation a feature gets the first time the user touches it.
 *
 * Features currently arrive with no introduction: a card says "Look back at your week" and tapping
 * it drops the user into a screen they have never seen, with no idea what it does for them or what
 * it will ask of them. This is the missing layer, in the shape premium apps use: what it is, what
 * you get, what we will ask, one way forward.
 *
 * [asks] is not optional politeness. A feature that wants the user's time, answers, or data says so
 * before they commit, which is what separates an invitation from a trap.
 */
data class FeatureIntro(
    /** Stable key. Persisted once the intro has been seen, so never rename a shipped id. */
    val id: String,
    /** The hero icon, chosen per feature rather than borrowed from a benefit row. */
    val icon: IntroIcon,
    val eyebrow: String,
    val title: String,
    /** One plain line: what this actually is. No metaphors. */
    val whatItIs: String,
    /** Two or three concrete things the user gets. Each one is a promise the feature keeps. */
    val benefits: List<IntroBenefit>,
    /** What the feature will ask of the user, stated before they commit. */
    val asks: String,
    val ctaLabel: String,
)

data class IntroBenefit(val icon: IntroIcon, val text: String)

/** Icon vocabulary for benefit rows, kept as data so the catalog stays free of Compose types. */
enum class IntroIcon { TARGET, TREND, EYE, CLOCK, COMPASS, LOCK, SCALES, CHART }

/**
 * Every intro the app can show, keyed by [FeatureIntro.id].
 *
 * Copy rules that apply here: concrete plain labels over abstractions ("Look back at your week",
 * not "Reflection"), an info line under every claim, and no guilt. Add an entry when a feature
 * gains a front door; a feature with no entry simply opens directly, which is the old behavior.
 */
object FeatureIntroCatalog {

    const val VISION = "intro_vision"
    const val QUEST = "intro_quest"

    /** Shared by the feed's weekly review card and the You tab's Day Retrospective row: one screen, one intro. */
    const val WEEKLY_REVIEW = "intro_weekly_review"

    const val DECISION_JOURNAL = "intro_decision_journal"
    const val DECISION_REVIEW = "intro_decision_review"
    const val MY_PATTERNS = "intro_my_patterns"

    /** Shared by the goal detail button and the feed's stalled-goal card: one screen, one intro. */
    const val POSSIBILITY = "intro_possibility_mode"

    private val all: Map<String, FeatureIntro> = listOf(
        FeatureIntro(
            id = VISION,
            icon = IntroIcon.COMPASS,
            eyebrow = "YOUR COMPASS",
            title = "Name what matters to you",
            whatItIs = "A short list of values that every goal you set can point back to.",
            benefits = listOf(
                IntroBenefit(IntroIcon.COMPASS, "Your goals show the reason behind them, not just a due date."),
                IntroBenefit(IntroIcon.TARGET, "When you are stuck, the app suggests what serves you most."),
                IntroBenefit(IntroIcon.EYE, "You see which values you have been living, and which you have not."),
            ),
            asks = "We will ask you to pick a few values and put them in order. You can change them any time.",
            ctaLabel = "Name my values",
        ),
        FeatureIntro(
            id = QUEST,
            icon = IntroIcon.TARGET,
            eyebrow = "QUARTERLY QUEST",
            title = "Pick one goal for 90 days",
            whatItIs = "One goal with a finish line, instead of a list you never reach the bottom of.",
            benefits = listOf(
                IntroBenefit(IntroIcon.TARGET, "Your feed keeps this goal in front of you every day."),
                IntroBenefit(IntroIcon.TREND, "Your coach breaks it into milestones you can start this week."),
                IntroBenefit(IntroIcon.CLOCK, "Ninety days is long enough to matter and short enough to finish."),
            ),
            asks = "We will ask for the goal in your own words and roughly when you want it done.",
            ctaLabel = "Set my quest",
        ),
        FeatureIntro(
            id = WEEKLY_REVIEW,
            icon = IntroIcon.EYE,
            eyebrow = "WEEKLY REVIEW",
            title = "Look back at your week",
            whatItIs = "About ten minutes with the week you just had, gathered in one place.",
            benefits = listOf(
                IntroBenefit(IntroIcon.EYE, "See what moved: goals, habits, and focus sessions side by side."),
                IntroBenefit(IntroIcon.TREND, "Name the one thing worth changing, so next week starts decided."),
                IntroBenefit(IntroIcon.CLOCK, "Build a record of your weeks that is worth reading months later."),
            ),
            asks = "We will ask you to skim your week and answer a few short questions. Your answers stay on your device.",
            ctaLabel = "Start my review",
        ),
        FeatureIntro(
            id = DECISION_JOURNAL,
            icon = IntroIcon.COMPASS,
            eyebrow = "DECISION JOURNAL",
            title = "Write down the call you just made",
            whatItIs = "A log of your real decisions, with the reasoning you had at the time.",
            benefits = listOf(
                IntroBenefit(IntroIcon.EYE, "Read back what you were actually thinking, not what you remember thinking."),
                IntroBenefit(IntroIcon.TREND, "See which kinds of calls you tend to get right."),
                IntroBenefit(IntroIcon.CLOCK, "Takes a minute now and pays off months from now."),
            ),
            asks = "We will ask what you decided, why, and what you expect to happen. Two or three sentences is plenty.",
            ctaLabel = "Log a decision",
        ),
        FeatureIntro(
            id = DECISION_REVIEW,
            icon = IntroIcon.SCALES,
            eyebrow = "REVIEW DECISIONS",
            title = "Grade your thinking, not just the result",
            whatItIs = "A second look at decisions whose outcome you now know.",
            benefits = listOf(
                IntroBenefit(IntroIcon.SCALES, "Tell a good call from a lucky one, and a bad call from bad luck."),
                IntroBenefit(IntroIcon.TREND, "Spot the reasoning that keeps letting you down."),
                IntroBenefit(IntroIcon.EYE, "Compare what you expected with what actually happened."),
            ),
            asks = "We will show you decisions you logged yourself and ask how each one turned out.",
            ctaLabel = "Review my decisions",
        ),
        FeatureIntro(
            id = MY_PATTERNS,
            icon = IntroIcon.CHART,
            eyebrow = "MY PATTERNS",
            title = "See when you actually show up",
            whatItIs = "The times and days you open this app, turned into a picture of your week.",
            benefits = listOf(
                IntroBenefit(IntroIcon.CLOCK, "Find the hours when your follow-through is strongest."),
                IntroBenefit(IntroIcon.TARGET, "Anchor your most important habit to a window that already works."),
                IntroBenefit(IntroIcon.CHART, "Watch the picture sharpen the more you use the app."),
            ),
            asks = "This uses how you use this app only. Nothing leaves your device and no other app is looked at.",
            ctaLabel = "See my patterns",
        ),
        FeatureIntro(
            id = POSSIBILITY,
            icon = IntroIcon.EYE,
            eyebrow = "WHEN YOU ARE STUCK",
            title = "Widen your options",
            whatItIs = "A screen that turns one stuck goal into several concrete ways forward.",
            benefits = listOf(
                IntroBenefit(IntroIcon.EYE, "Genuinely different options, each labeled with the thinking angle behind it."),
                IntroBenefit(IntroIcon.TARGET, "Any option can become a new goal, a step on this one, or a logged decision."),
                IntroBenefit(IntroIcon.SCALES, "It widens your choices and never chooses for you."),
            ),
            asks = "Nothing new. It works from the goal you already wrote.",
            ctaLabel = "Show me options",
        ),
    ).associateBy { it.id }

    operator fun get(id: String?): FeatureIntro? = id?.let { all[it] }

    val ids: Set<String> get() = all.keys
}
