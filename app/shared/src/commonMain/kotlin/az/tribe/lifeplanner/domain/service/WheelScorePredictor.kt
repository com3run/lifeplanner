package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import az.tribe.lifeplanner.domain.model.WheelScore
import az.tribe.lifeplanner.domain.model.roundToHalf

/**
 * What the app knows about one area, gathered by the repository and handed here as plain numbers so
 * the prediction stays pure and testable.
 */
data class AreaSignals(
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    /** Goals with a due date ahead of them: the user's plan for this area, not just their history. */
    val plannedGoals: Int = 0,
    val habits: Int = 0,
    /** 0.0..1.0 over the trailing month. */
    val habitCompletionRate: Double = 0.0,
    val longestStreak: Int = 0,
)

/**
 * Signals that are not tied to a goal category, so cannot be read from [AreaSignals].
 */
data class GlobalSignals(
    /** Mean recorded journal mood over the trailing month, on Mood's own 1..5 scale. Null if none. */
    val averageMood: Double? = null,
    val journalEntriesThisMonth: Int = 0,
    /** Health: mean daily steps over the trailing week. Null when Health is not connected. */
    val averageDailySteps: Int? = null,
    /** Health: mean hours slept over the trailing week. Null when unavailable. */
    val averageSleepHours: Double? = null,
    /** Learn hub lessons finished in the trailing month, the clearest Growth signal we hold. */
    val lessonsReadThisMonth: Int = 0,
    /** Abilities the user is levelling. */
    val abilitiesInProgress: Int = 0,
    val focusSessionsThisMonth: Int = 0,
)

/**
 * Predicts a Wheel of Life score for each area from what the user already has in the app.
 *
 * The wheel is a subjective instrument, so nothing here claims to measure someone's life. What it
 * does is put a defensible number in each slot so the user edits ten numbers instead of entering
 * them, and says plainly how sure it is. An area we have no signal for is marked
 * [ScoreSource.ESTIMATED] and left at the neutral midpoint rather than dressed up as a reading.
 *
 * The scale is anchored on [WheelArea.rubric]: 5 is "unremarkable", and evidence moves it either
 * way. It is not an engagement score, so having no goals in an area is not itself evidence that the
 * area is bad. Plenty of people have excellent friendships and zero friendship goals.
 */
object WheelScorePredictor {

    /** Where an area with nothing behind it sits: the middle, honestly labelled. */
    private const val NEUTRAL = 5.0

    fun predict(
        signalsByArea: Map<WheelArea, AreaSignals>,
        global: GlobalSignals,
        /** Scores the user has already set. These win outright and are passed through untouched. */
        userScores: Map<WheelArea, Double> = emptyMap(),
    ): List<WheelScore> {
        val predicted = WheelArea.segments().map { area ->
            userScores[area]?.let { return@map userSet(area, it) }
            when (area) {
                WheelArea.PHYSICAL -> physical(signalsByArea[area], global)
                WheelArea.MENTAL -> mental(signalsByArea[area], global)
                WheelArea.GROWTH -> growth(signalsByArea[area], global)
                else -> fromGoalActivity(area, signalsByArea[area])
            }
        }

        val joy = userScores[WheelArea.JOY]
            ?.let { userSet(WheelArea.JOY, it) }
            ?: joy(predicted, global)

        return predicted + joy
    }

    private fun userSet(area: WheelArea, score: Double) = WheelScore(
        area = area,
        score = score.roundToHalf(),
        source = ScoreSource.USER,
        confidence = 1.0,
        basis = "You set this.",
    )

    /**
     * The default reading: a maintained area scores above the middle, a neglected one slightly
     * below. Kept gentle on purpose. Absence of goals is weak evidence, so it moves the number a
     * little and the confidence a lot.
     */
    private fun fromGoalActivity(area: WheelArea, signals: AreaSignals?): WheelScore {
        val s = signals ?: AreaSignals()
        val hasAnything = s.activeGoals + s.completedGoals + s.habits + s.plannedGoals > 0
        if (!hasAnything) {
            return WheelScore(
                area = area,
                score = NEUTRAL,
                source = ScoreSource.ESTIMATED,
                confidence = 0.0,
                basis = "Nothing here yet, so this is a placeholder rather than a reading.",
            )
        }

        var score = NEUTRAL
        val reasons = mutableListOf<String>()

        if (s.completedGoals > 0) {
            score += (s.completedGoals * 0.5).coerceAtMost(1.5)
            reasons += "${s.completedGoals} goal${plural(s.completedGoals)} finished"
        }
        if (s.habits > 0) {
            // Keeping habits is the strongest thing we can see. Breaking them cuts the other way.
            score += ((s.habitCompletionRate - 0.5) * 3).coerceIn(-1.5, 1.5)
            reasons += "${s.habits} habit${plural(s.habits)} at ${(s.habitCompletionRate * 100).toInt()}%"
        }
        if (s.longestStreak >= 14) {
            score += 0.5
            reasons += "a ${s.longestStreak}-day streak"
        }
        if (s.plannedGoals > 0) {
            // Having a plan says the user is on it, not that the area is already good.
            score += 0.25
            reasons += "${s.plannedGoals} planned"
        }

        return WheelScore(
            area = area,
            score = score.roundToHalf(),
            source = ScoreSource.PREDICTED,
            confidence = confidenceFor(s),
            basis = reasons.joinToString(", ").replaceFirstChar { it.uppercase() } + ".",
        )
    }

    /** Physical is the one area with objective outside evidence, when Health is connected. */
    private fun physical(signals: AreaSignals?, global: GlobalSignals): WheelScore {
        val base = fromGoalActivity(WheelArea.PHYSICAL, signals)
        val steps = global.averageDailySteps
        val sleep = global.averageSleepHours
        if (steps == null && sleep == null) return base

        var score = base.score
        val reasons = mutableListOf<String>()
        steps?.let {
            score += when {
                it >= 10_000 -> 1.5
                it >= 7_000 -> 0.75
                it >= 4_000 -> 0.0
                else -> -1.0
            }
            reasons += "$it steps a day"
        }
        sleep?.let {
            score += when {
                it >= 7.0 -> 1.0
                it >= 6.0 -> 0.0
                else -> -1.0
            }
            reasons += "${formatOneDecimal(it)}h sleep"
        }

        return base.copy(
            score = score.roundToHalf(),
            source = ScoreSource.PREDICTED,
            // Health data is measured rather than inferred, so it earns real confidence.
            confidence = (base.confidence + 0.4).coerceAtMost(0.9),
            basis = (reasons + base.basis.trimEnd('.').lowercase().ifBlank { null }.orEmpty())
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .replaceFirstChar { it.uppercase() } + ".",
        )
    }

    /** Mental leans on recorded mood, which is the closest thing we have to a direct report. */
    private fun mental(signals: AreaSignals?, global: GlobalSignals): WheelScore {
        val base = fromGoalActivity(WheelArea.MENTAL, signals)
        val mood = global.averageMood ?: return base

        // Mood is 1..5 against the rubric's 0..10, and it is self-reported at the moment of
        // writing, so it anchors the score rather than merely nudging it.
        val fromMood = ((mood - 1.0) / 4.0) * 10.0
        val blended = if (base.source == ScoreSource.ESTIMATED) fromMood else (fromMood * 0.7 + base.score * 0.3)

        return base.copy(
            score = blended.roundToHalf(),
            source = ScoreSource.PREDICTED,
            confidence = confidenceFromEntries(global.journalEntriesThisMonth),
            basis = "Average mood ${formatOneDecimal(mood)}/5 across " +
                "${global.journalEntriesThisMonth} entr${if (global.journalEntriesThisMonth == 1) "y" else "ies"}.",
        )
    }

    /** Growth is the area the Learn hub and Abilities actually evidence. */
    private fun growth(signals: AreaSignals?, global: GlobalSignals): WheelScore {
        val base = fromGoalActivity(WheelArea.GROWTH, signals)
        val lessons = global.lessonsReadThisMonth
        val abilities = global.abilitiesInProgress
        if (lessons == 0 && abilities == 0) return base

        var score = if (base.source == ScoreSource.ESTIMATED) NEUTRAL else base.score
        val reasons = mutableListOf<String>()
        if (lessons > 0) {
            score += when {
                lessons >= 8 -> 1.5
                lessons >= 3 -> 1.0
                else -> 0.5
            }
            reasons += "$lessons lesson${plural(lessons)} read"
        }
        if (abilities > 0) {
            score += (abilities * 0.25).coerceAtMost(1.0)
            reasons += "$abilities abilit${if (abilities == 1) "y" else "ies"} in progress"
        }

        return base.copy(
            score = score.roundToHalf(),
            source = ScoreSource.PREDICTED,
            confidence = (base.confidence + 0.3).coerceAtMost(0.85),
            basis = reasons.joinToString(", ").replaceFirstChar { it.uppercase() } + ".",
        )
    }

    /**
     * Joy reads the whole rather than a slice of it, matching how the original places it outside
     * the wheel: how the rest of life is going, corrected by how the user actually reports feeling.
     */
    private fun joy(segments: List<WheelScore>, global: GlobalSignals): WheelScore {
        val measured = segments.filter { it.source != ScoreSource.ESTIMATED }
        if (measured.isEmpty() && global.averageMood == null) {
            return WheelScore(
                area = WheelArea.JOY,
                score = NEUTRAL,
                source = ScoreSource.ESTIMATED,
                confidence = 0.0,
                basis = "Not enough yet to read this. Tell us where you are.",
            )
        }

        val wheelMean = measured.takeIf { it.isNotEmpty() }?.map { it.score }?.average()
        val moodScore = global.averageMood?.let { ((it - 1.0) / 4.0) * 10.0 }

        val score = when {
            wheelMean != null && moodScore != null -> wheelMean * 0.5 + moodScore * 0.5
            moodScore != null -> moodScore
            else -> wheelMean!!
        }

        val basis = when {
            wheelMean != null && moodScore != null ->
                "Your wheel averages ${formatOneDecimal(wheelMean)} and your mood ${formatOneDecimal(global.averageMood!!)}/5."
            moodScore != null -> "Based on your recorded mood."
            else -> "Based on the rest of your wheel."
        }

        return WheelScore(
            area = WheelArea.JOY,
            score = score.roundToHalf(),
            source = ScoreSource.PREDICTED,
            confidence = if (moodScore != null) 0.6 else 0.4,
            basis = basis,
        )
    }

    /** More evidence, more confidence, capped short of certainty because this stays a guess. */
    private fun confidenceFor(s: AreaSignals): Double {
        var c = 0.0
        if (s.habits > 0) c += 0.3
        if (s.habitCompletionRate > 0.0) c += 0.15
        if (s.activeGoals + s.completedGoals > 0) c += 0.2
        if (s.longestStreak >= 7) c += 0.1
        return c.coerceIn(0.0, 0.8)
    }

    private fun confidenceFromEntries(entries: Int): Double = when {
        entries >= 12 -> 0.8
        entries >= 5 -> 0.6
        entries >= 1 -> 0.4
        else -> 0.2
    }

    private fun plural(n: Int) = if (n == 1) "" else "s"

    private fun formatOneDecimal(value: Double): String {
        val rounded = kotlin.math.round(value * 10) / 10
        val whole = rounded.toInt()
        val tenth = kotlin.math.round((rounded - whole) * 10).toInt()
        return if (tenth == 0) "$whole" else "$whole.$tenth"
    }

    /** Areas worth nudging first: lowest slice, and anything we could only estimate. */
    fun suggestedFocus(scores: List<WheelScore>): List<WheelArea> {
        val segments = scores.filter { it.area.isWheelSegment }
        val weakest = segments.filter { it.source != ScoreSource.ESTIMATED }.minByOrNull { it.score }
        return listOfNotNull(weakest?.area) + segments.filter { it.needsConfirmation }.map { it.area }
    }

    /** The goal categories to draw on when the user wants to act on an area. */
    fun categoriesFor(area: WheelArea): List<GoalCategory> = area.categories
}
