package az.tribe.lifeplanner.ui.foryou

import az.tribe.lifeplanner.core.FeatureFlags

import az.tribe.lifeplanner.domain.model.ActionOptionType
import az.tribe.lifeplanner.domain.model.FeedItem
import az.tribe.lifeplanner.domain.model.FeedKind
import az.tribe.lifeplanner.domain.model.InsightConfidence
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.domain.repository.IdentityStatementRepository
import az.tribe.lifeplanner.domain.service.CausalInsightProvider
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import az.tribe.lifeplanner.domain.service.PossibilityContextProvider
import az.tribe.lifeplanner.domain.service.PossibilityEngine
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.usecases.ComputeValueAlignmentUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** The three bands of the feed. Used for section headers and the filter chips. */
enum class FeedSection(val label: String) {
    DO("Right now"),
    REFLECT("Reflect on today"),
    LEARN("Learn"),
}

fun FeedKind.section(): FeedSection = when (this) {
    FeedKind.DO_NEXT -> FeedSection.DO
    FeedKind.INSIGHT, FeedKind.BECOMING, FeedKind.PATTERN, FeedKind.MOMENTUM, FeedKind.POSSIBILITY -> FeedSection.REFLECT
    FeedKind.KNOWLEDGE -> FeedSection.LEARN
}

/**
 * Assembles the ranked "For You" home feed from the engines the app already has. The point is to
 * pull the reflective "You" functions (causal insights, becoming, patterns) onto the front door and
 * let the user reflect on today, instead of burying them in a tab. Each reflect card also deep-links
 * to its full screen. Everything is best-effort: a failing source is skipped, never crashes the feed.
 */
class HomeFeedBuilder(
    private val causalInsightProvider: CausalInsightProvider,
    private val computeValueAlignment: ComputeValueAlignmentUseCase,
    private val identityRepo: IdentityStatementRepository,
    private val possibilityEngine: PossibilityEngine,
    private val possibilityContextProvider: PossibilityContextProvider,
    private val gamificationRepository: GamificationRepository,
    private val behaviorRepository: BehaviorRepository,
    private val goalRepository: GoalRepository,
    private val lifeValueRepository: LifeValueRepository,
) {

    suspend fun build(): List<FeedItem> {
        val progress = runCatching { gamificationRepository.getUserProgress().first() }.getOrNull()
        val level = progress?.currentLevel ?: 1
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val daySeed = today.dayOfYear

        val items = mutableListOf<FeedItem>()
        val ctx = runCatching { possibilityContextProvider.currentContext() }.getOrNull()

        // Pillar 1 lookup: goal -> the value it serves. This is what turns the feed's most
        // prominent card from a task ("fits (overdue)") into a reason ("Toward Family"), which is
        // D1's P4 (always show the why) and the difference between this and a generic planner.
        val goals = runCatching { goalRepository.getAllGoals() }.getOrDefault(emptyList())
        val valueTitleById = runCatching { lifeValueRepository.getAllLifeValues() }
            .getOrDefault(emptyList())
            .associate { it.id to it.title }
        fun whyFor(goalId: String?): String? = goalId
            ?.let { id -> goals.firstOrNull { it.id == id } }
            ?.valueId
            ?.let { valueTitleById[it] }

        // ── Right now: the single best next moves ───────────────────────────
        runCatching { ctx?.let { possibilityEngine.rank(it, limit = 2) } ?: emptyList() }
            .getOrDefault(emptyList())
            .forEachIndexed { i, o ->
                items += FeedItem(
                    id = "do_${o.type}_${o.refId}",
                    kind = FeedKind.DO_NEXT,
                    eyebrow = "DO NEXT",
                    title = o.title,
                    // Lead with the why when the goal has one, and drop fitReason entirely there:
                    // it restates the title and ends in "(overdue)", which is scheduling logic
                    // plus a guilt note that D1's P3 rejects. Without a value link there is no why
                    // to show yet, so the fit reason still earns its place.
                    body = whyFor(o.goalId)?.let { "Toward $it." } ?: o.fitReason,
                    category = o.category,
                    actionLabel = if (o.type == ActionOptionType.HABIT) "Check in" else null,
                    actionHabitId = if (o.type == ActionOptionType.HABIT) o.refId else null,
                    route = when (o.type) {
                        ActionOptionType.HABIT -> "habit_detail_redesign/${o.refId}"
                        ActionOptionType.GOAL -> "goal_detail_redesign/${o.refId}"
                        ActionOptionType.MILESTONE, ActionOptionType.FOCUS ->
                            o.goalId?.let { "goal_detail_redesign/$it" }
                    },
                    score = 100.0 - i,
                )
            }

        // Pillar 6 stall trigger: surface the most stalled goal as a Possibility Mode prompt.
        ctx?.dueOrStalledGoals
            ?.takeIf { FeatureFlags.PILLAR_POSSIBILITY }
            ?.firstOrNull { (it.progress ?: 0L) < 25L && it.createdAt.date.daysUntil(today) > 14 }
            ?.let { g ->
                items += FeedItem(
                    id = "possibility_${g.id}",
                    kind = FeedKind.POSSIBILITY,
                    eyebrow = "FEELING STUCK?",
                    title = "\"${g.title}\" has not moved lately",
                    body = "Widen the options instead of forcing the same path. Tap to explore possibilities.",
                    route = "possibility_mode/${g.id}",
                    score = 90.0,
                )
            }

        // ── Reflect: insights about you (deep-link to the full You screens) ──
        runCatching { causalInsightProvider.insights(windowDays = 90) }
            .getOrDefault(emptyList())
            .filter { FeatureFlags.PILLAR_CAUSAL && it.confidence != InsightConfidence.LOW }
            .take(2)
            .forEachIndexed { i, ins ->
                items += FeedItem(
                    id = "insight_${ins.kind}_$i",
                    kind = FeedKind.INSIGHT,
                    eyebrow = "INSIGHT ABOUT YOU",
                    title = ins.statement,
                    body = "Based on ${ins.sampleSize} days, ${ins.confidence.name.lowercase()} confidence. Tap to explore what is moving your goals.",
                    route = Screen.CausalInsights.route,
                    score = 82.0 - i,
                )
            }

        // Becoming: top value you are living, else an active identity statement
        val alignments = runCatching { computeValueAlignment() }.getOrDefault(emptyList())
        val topAlign = alignments.firstOrNull { it.completedGoalCount > 0 }
        val statements = runCatching { identityRepo.getAll() }.getOrDefault(emptyList()).filter { it.isActive }
        when {
            !FeatureFlags.PILLAR_BECOMING -> Unit
            topAlign != null -> items += FeedItem(
                id = "becoming_${topAlign.valueId}",
                kind = FeedKind.BECOMING,
                eyebrow = "BECOMING",
                title = "You are living \"${topAlign.valueTitle}\"",
                body = "${topAlign.completedGoalCount} completed ${plural(topAlign.completedGoalCount, "goal", "goals")} served this value. Every action is a vote for who you are.",
                route = Screen.Becoming.route,
                score = 74.0,
            )
            statements.isNotEmpty() -> {
                val s = statements[daySeed % statements.size]
                items += FeedItem(
                    id = "becoming_stmt_${s.id}",
                    kind = FeedKind.BECOMING,
                    eyebrow = "BECOMING",
                    title = s.statement,
                    body = "Keep taking small actions that vote for this identity.",
                    route = Screen.Becoming.route,
                    score = 74.0,
                )
            }
        }

        // Pattern: when the user actually shows up
        runCatching { behaviorRepository.getPattern() }.getOrNull()?.takeIf { it.hasEnoughData }?.let { p ->
            items += FeedItem(
                id = "pattern_peak",
                kind = FeedKind.PATTERN,
                eyebrow = "YOUR PATTERN",
                title = "You show up most in the ${p.peakHourLabel()}",
                body = "Your follow-through is strongest then. Try anchoring your most important habit to that window.",
                route = Screen.ScreenTimeInsight.route,
                score = 64.0,
            )
        }

        // Momentum: streak + level progress
        progress?.takeIf { it.currentStreak >= 2 }?.let { p ->
            items += FeedItem(
                id = "momentum_streak",
                kind = FeedKind.MOMENTUM,
                eyebrow = "MOMENTUM",
                title = "${p.currentStreak} day streak",
                body = "Level ${p.currentLevel}, ${p.title}. ${p.xpInCurrentLevel}/${p.xpForCurrentLevel} XP to your next level.",
                route = Screen.Achievements.route,
                score = 60.0,
            )
        }

        // ── Cold start: invitations where a pillar has no data yet ──────────
        //
        // Every distinctive card above is gated behind history a new user does not have
        // (Possibility needs a 14-day stall, Insight needs samples, Becoming needs a completed
        // goal, Pattern needs enough data). Without this block the only cards that can fire in
        // week one are DO_NEXT and KNOWLEDGE, so the app introduces itself as a to-do list with a
        // blog and the pillars are invisible exactly when the impression forms.
        //
        // An invitation is not filler: it is the pillar asking for the one input that switches it
        // on. Capped at two so the feed never becomes a chore list of setup prompts.
        val present = items.map { it.kind }.toSet()
        val invitations = mutableListOf<FeedItem>()

        // Pillar 1: a goal with no value is the orphaned-goal nudge D1 P4 calls for.
        goals.firstOrNull { it.valueId == null }?.let { orphan ->
            invitations += FeedItem(
                id = "why_invite_${orphan.id}",
                kind = FeedKind.INSIGHT,
                eyebrow = "YOUR WHY",
                title = "What is \"${orphan.title}\" really for?",
                body = "Connect it to something you value. Goals with a why are the ones you finish.",
                route = "goal_detail_redesign/${orphan.id}",
                score = 86.0,
            )
        }

        // Pillar 5: no completed goals and no identity statement yet.
        if (FeatureFlags.PILLAR_BECOMING && FeedKind.BECOMING !in present) {
            invitations += FeedItem(
                id = "becoming_invite",
                kind = FeedKind.BECOMING,
                eyebrow = "BECOMING",
                title = "Who are you becoming?",
                body = "Name the person your goals are building toward. Every action becomes a vote for it.",
                route = Screen.Becoming.route,
                score = 76.0,
            )
        }

        // Pillar 7: no decision profile inferred yet.
        if (FeatureFlags.PILLAR_WIRING && ctx?.profile == null) {
            invitations += FeedItem(
                id = "wiring_invite",
                kind = FeedKind.INSIGHT,
                eyebrow = "YOUR WIRING",
                title = "How do you actually decide?",
                body = "The app learns your decision profile as you log choices, then adapts what it suggests.",
                route = Screen.YourWiring.route,
                score = 68.0,
            )
        }

        items += invitations.take(2)

        // ── Learn: curated, leveled knowledge ───────────────────────────────
        KnowledgeLibrary.forLevel(level, daySeed, count = 3).forEachIndexed { i, k ->
            items += FeedItem(
                id = k.id,
                kind = FeedKind.KNOWLEDGE,
                eyebrow = "LEARN · ${k.readMin} min",
                title = k.title,
                body = k.body,
                emoji = k.emoji,
                score = 50.0 - i,
            )
        }

        Logger.d("HomeFeedBuilder") { "Built feed: ${items.size} items (level $level)" }
        // Group into bands (Right now, Reflect, Learn), strongest first within each.
        return items.sortedWith(compareBy({ it.kind.section().ordinal }, { -it.score }))
    }

    private fun plural(n: Int, one: String, many: String) = if (n == 1) one else many
}
