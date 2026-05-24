package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.DayMetrics
import az.tribe.lifeplanner.domain.model.InsightKind
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CausalInsightEngineTest {

    private val engine = CausalInsightEngine()
    private val base = LocalDate(2026, 1, 5)
    private fun day(i: Int) = base.plus(i, DateTimeUnit.DAY)
    private fun week(i: Int) = base.plus(7 * i, DateTimeUnit.DAY) // 7-day spacing → distinct week buckets

    @Test
    fun `strong positive sleep-mood correlation surfaces`() {
        val sleeps = listOf(6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5)
        val moods = listOf(2, 2, 3, 3, 4, 4, 5, 5)
        val days = sleeps.indices.map { i ->
            DayMetrics(date = day(i), sleepHours = sleeps[i], mood = moods[i])
        }
        val corr = engine.analyze(days).filter { it.kind == InsightKind.CORRELATION }
        assertTrue(corr.isNotEmpty(), "expected a correlation insight")
        assertTrue(
            corr.any { it.statement.contains("sleep", true) && it.statement.contains("mood", true) },
            "expected a sleep↔mood statement"
        )
        assertTrue(corr.all { it.strength >= 0.35 }, "correlations below threshold should be filtered")
    }

    @Test
    fun `too few days yields no correlation`() {
        val days = (0 until 5).map { i ->
            DayMetrics(date = day(i), sleepHours = 6.0 + i, mood = (1 + i).coerceAtMost(5))
        }
        assertTrue(engine.analyze(days).none { it.kind == InsightKind.CORRELATION })
    }

    @Test
    fun `weak correlation is filtered out`() {
        // mood alternates independently of sleep → near-zero correlation
        val days = (0 until 10).map { i ->
            DayMetrics(date = day(i), sleepHours = 7.0, mood = if (i % 2 == 0) 3 else 4)
        }
        // sleep has zero variance here → pearson null → no insight anyway; also covers that edge.
        assertTrue(engine.analyze(days).none { it.kind == InsightKind.CORRELATION })
    }

    @Test
    fun `declining weekly habits flags a downward spiral`() {
        val counts = listOf(6, 3, 1)
        val days = counts.indices.map { i -> DayMetrics(date = week(i), habitsCompleted = counts[i]) }
        val spiral = engine.analyze(days).firstOrNull { it.kind == InsightKind.AMPLIFICATION_SPIRAL }
        assertTrue(spiral != null, "expected a spiral insight")
        assertTrue(spiral!!.statement.contains("slipped", true), "expected a downward-spiral warning")
        assertTrue(spiral.strength > 0.0)
    }

    @Test
    fun `rising weekly habits flags an upward spiral`() {
        val counts = listOf(1, 3, 6)
        val days = counts.indices.map { i -> DayMetrics(date = week(i), habitsCompleted = counts[i]) }
        val spiral = engine.analyze(days).firstOrNull { it.kind == InsightKind.AMPLIFICATION_SPIRAL }
        assertTrue(spiral != null && spiral.statement.contains("climbed", true))
    }

    @Test
    fun `flat weekly habits is not a spiral`() {
        val days = (0..2).map { DayMetrics(date = week(it), habitsCompleted = 3) }
        assertTrue(engine.analyze(days).none { it.kind == InsightKind.AMPLIFICATION_SPIRAL })
    }

    @Test
    fun `empty input yields no insights`() {
        assertEquals(emptyList(), engine.analyze(emptyList()))
    }
}
