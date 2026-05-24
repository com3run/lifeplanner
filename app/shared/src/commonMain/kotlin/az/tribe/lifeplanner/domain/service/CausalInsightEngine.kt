package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.CausalInsight
import az.tribe.lifeplanner.domain.model.DayMetrics
import az.tribe.lifeplanner.domain.model.InsightConfidence
import az.tribe.lifeplanner.domain.model.InsightKind
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Pillar 4 (Causal Model), pure, on-device statistics (NO AI). Turns the user's own daily
 * history into plain-language causal insights:
 *  - correlations between signals (sleep × mood × focus × habits × energy × goal progress), and
 *  - the "innate" amplification spiral: a self-reinforcing weekly trajectory caught early.
 *
 * Deterministic over its input → fully unit-testable. Every insight carries its sample size +
 * [InsightConfidence] so the UI can stay honest about small data.
 */
class CausalInsightEngine(
    private val minSampleSize: Int = 7,
    private val minCorrelation: Double = 0.35,
    private val minSpiralWeeks: Int = 3
) {
    fun analyze(days: List<DayMetrics>): List<CausalInsight> =
        (correlationInsights(days) + spiralInsights(days))
            .sortedByDescending { it.strength }

    // ── Correlations ──────────────────────────────────────────────────────────

    private class Relationship(
        val a: (DayMetrics) -> Double?,
        val b: (DayMetrics) -> Double?,
        val positive: String,
        val negative: String
    )

    private val relationships = listOf(
        Relationship({ it.sleepHours }, { it.mood?.toDouble() },
            "On days you sleep more, your mood tends to run higher.",
            "More sleep is linked to lower mood here, worth a closer look."),
        Relationship({ it.sleepHours }, { it.focusMinutes?.toDouble() },
            "More sleep, more focus time the same day.",
            "More sleep coincides with less focus time, unexpected."),
        Relationship({ it.focusMinutes?.toDouble() }, { it.habitsCompleted?.toDouble() },
            "On higher-focus days you also complete more habits.",
            "More focus time coincides with fewer habits done."),
        Relationship({ it.habitsCompleted?.toDouble() }, { it.mood?.toDouble() },
            "Days you complete more habits, your mood is higher.",
            "More habits done coincides with lower mood, worth watching."),
        Relationship({ it.energy?.toDouble() }, { it.focusMinutes?.toDouble() },
            "Higher-energy days bring more focus time.",
            "Higher energy coincides with less focus time."),
        Relationship({ it.sleepHours }, { it.energy?.toDouble() },
            "More sleep, higher energy.",
            "More sleep coincides with lower energy, unusual."),
        Relationship({ it.focusMinutes?.toDouble() }, { it.goalProgressDelta },
            "More focus time, more goal progress.",
            "More focus time coincides with less goal progress.")
    )

    private fun correlationInsights(days: List<DayMetrics>): List<CausalInsight> =
        relationships.mapNotNull { rel ->
            val pairs = days.mapNotNull { d ->
                val x = rel.a(d)
                val y = rel.b(d)
                if (x != null && y != null) x to y else null
            }
            if (pairs.size < minSampleSize) return@mapNotNull null
            val r = pearson(pairs) ?: return@mapNotNull null
            if (abs(r) < minCorrelation) return@mapNotNull null
            CausalInsight(
                kind = InsightKind.CORRELATION,
                statement = if (r >= 0) rel.positive else rel.negative,
                strength = abs(r),
                sampleSize = pairs.size,
                confidence = confidenceFor(pairs.size)
            )
        }

    // ── Amplification spiral (trajectory) ─────────────────────────────────────

    private fun spiralInsights(days: List<DayMetrics>): List<CausalInsight> {
        val weekly = days
            .filter { it.habitsCompleted != null }
            .groupBy { it.date.toEpochDays() / 7 }
            .toSortedMap()
            .map { (_, ds) -> ds.sumOf { it.habitsCompleted ?: 0 } }
        if (weekly.size < minSpiralWeeks) return emptyList()

        val recent = weekly.takeLast(minSpiralWeeks)
        val first = recent.first().toDouble()
        val last = recent.last().toDouble()
        val n = days.count { it.habitsCompleted != null }
        val strictlyDown = recent.zipWithNext().all { (a, b) -> b < a }
        val strictlyUp = recent.zipWithNext().all { (a, b) -> b > a }

        return when {
            strictlyDown && first > 0 -> listOf(
                CausalInsight(
                    kind = InsightKind.AMPLIFICATION_SPIRAL,
                    statement = "Your habit momentum has slipped ${recent.size} weeks running, " +
                        "small now, but these losses compound. One win this week reverses the spiral.",
                    strength = (first - last) / first,
                    sampleSize = n,
                    confidence = confidenceFor(n)
                )
            )
            strictlyUp -> listOf(
                CausalInsight(
                    kind = InsightKind.AMPLIFICATION_SPIRAL,
                    statement = "Your habit momentum has climbed ${recent.size} weeks running, " +
                        "it's compounding in your favour. Keep it fed.",
                    strength = if (first > 0) (last - first) / first else 1.0,
                    sampleSize = n,
                    confidence = confidenceFor(n)
                )
            )
            else -> emptyList()
        }
    }

    // ── Stats helpers ─────────────────────────────────────────────────────────

    /** Pearson correlation; null when n < 2 or either series has zero variance. */
    private fun pearson(pairs: List<Pair<Double, Double>>): Double? {
        if (pairs.size < 2) return null
        val mx = pairs.sumOf { it.first } / pairs.size
        val my = pairs.sumOf { it.second } / pairs.size
        var cov = 0.0
        var vx = 0.0
        var vy = 0.0
        for ((x, y) in pairs) {
            val dx = x - mx
            val dy = y - my
            cov += dx * dy
            vx += dx * dx
            vy += dy * dy
        }
        if (vx == 0.0 || vy == 0.0) return null
        return cov / sqrt(vx * vy)
    }

    private fun confidenceFor(n: Int): InsightConfidence = when {
        n < 10 -> InsightConfidence.LOW
        n <= 20 -> InsightConfidence.MODERATE
        else -> InsightConfidence.HIGH
    }
}
