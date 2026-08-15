package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ChoicePointTrigger
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.testutil.testGoal
import az.tribe.lifeplanner.testutil.testHabit
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChoicePointDetectorTest {

    private val detector = ChoicePointDetector()
    private val today = LocalDate(2026, 6, 1)
    private fun daysAgo(n: Int) = today.minus(n, DateTimeUnit.DAY)

    // A reliably-inferred, clearly-high dial (confidence >= 0.5, n >= 10, value >= 0.6).
    private val reliableHigh = DialSetting(value = 0.8f, confidence = 0.6f, sampleSize = 12)
    private val unreliableHigh = DialSetting(value = 0.9f, confidence = 0.2f, sampleSize = 4)
    private fun punishmentSensitive() = DecisionProfile.neutral("p").copy(punishmentSensitivity = reliableHigh)
    private fun riskAverseProfile() = DecisionProfile.neutral("p").copy(riskAversion = reliableHigh)

    private fun missedHabit(days: Int) =
        listOf(testHabit(lastCompletedDate = daysAgo(days)) to false)

    @Test
    fun `no profile keeps default threshold and neutral wording`() {
        val cps = detector.detect(today, goals = emptyList(), habits = missedHabit(2))
        assertEquals(1, cps.size)
        assertEquals(ChoicePointTrigger.HABIT_STREAK_BREAK, cps.single().trigger)
        assertTrue(cps.single().prompt.contains("Keep going, or let it go?"))
    }

    @Test
    fun `punishment-sensitive user is not flagged after a single missed day`() {
        // 2 missed days would flag a neutral user; a punishment-sensitive one gets a grace day.
        val cps = detector.detect(today, emptyList(), missedHabit(2), profile = punishmentSensitive())
        assertTrue(cps.isEmpty(), "should not hammer a punishment-sensitive user at the default threshold")
    }

    @Test
    fun `punishment-sensitive user gets a gentle streak prompt once past the raised threshold`() {
        val cps = detector.detect(today, emptyList(), missedHabit(3), profile = punishmentSensitive())
        assertEquals(1, cps.size)
        val p = cps.single().prompt
        assertTrue(p.contains("set it aside", ignoreCase = true), "expected gentle wording, was: $p")
        assertTrue(!p.contains("let it go"), "should not use the blunt default wording")
    }

    @Test
    fun `risk-averse user keeps the normal threshold but gets gentler wording`() {
        val cps = detector.detect(today, emptyList(), missedHabit(2), profile = riskAverseProfile())
        assertEquals(1, cps.size, "risk aversion should not change frequency")
        assertTrue(cps.single().prompt.contains("set it aside", ignoreCase = true))
    }

    @Test
    fun `an unreliable profile behaves like no profile`() {
        val profile = DecisionProfile.neutral("p").copy(punishmentSensitivity = unreliableHigh)
        val cps = detector.detect(today, emptyList(), missedHabit(2), profile = profile)
        assertEquals(1, cps.size)
        assertTrue(cps.single().prompt.contains("Keep going, or let it go?"))
    }

    @Test
    fun `stalled-goal window is extended for a punishment-sensitive user`() {
        // Created 18 days ago, no progress, due in the future → a neutral user sees a stall.
        val goal = testGoal(
            progress = 0,
            createdAt = daysAgo(18).atTime(0, 0),
            dueDate = today.plus(30, DateTimeUnit.DAY),
        )
        val neutral = detector.detect(today, listOf(goal), emptyList())
        assertTrue(neutral.any { it.trigger == ChoicePointTrigger.GOAL_STALLED }, "neutral user should see a stall at 18 days")

        val gentle = detector.detect(today, listOf(goal), emptyList(), profile = punishmentSensitive())
        assertTrue(gentle.none { it.trigger == ChoicePointTrigger.GOAL_STALLED }, "punishment-sensitive window is 14+7 days")
    }

    @Test
    fun `passed deadline still surfaces with gentle wording under a sensitive profile`() {
        val overdue = testGoal(dueDate = daysAgo(3))
        val cps = detector.detect(today, listOf(overdue), emptyList(), profile = punishmentSensitive())
        val dl = cps.single { it.trigger == ChoicePointTrigger.DEADLINE_PASSED }
        assertTrue(dl.prompt.contains("No rush", ignoreCase = true), "expected gentle deadline wording, was: ${dl.prompt}")
    }
}
