package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.ScoreSource
import az.tribe.lifeplanner.domain.model.WheelArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class WheelScorePredictorTest {

    @Test
    fun `a wheel always has ten areas with Joy last`() {
        val scores = WheelScorePredictor.predict(emptyMap(), GlobalSignals())

        assertEquals(10, scores.size)
        assertEquals(WheelArea.JOY, scores.last().area)
        assertEquals(9, scores.count { it.area.isWheelSegment })
    }

    @Test
    fun `an empty account is estimated at the midpoint, not scored as bad`() {
        val scores = WheelScorePredictor.predict(emptyMap(), GlobalSignals())

        // The old balance score read "no goals" as "this area is failing", which is why a fresh
        // account showed a flat 14/100 everywhere. Having no goals about your friends is not
        // evidence that your friendships are bad.
        scores.filter { it.area.isWheelSegment }.forEach {
            assertEquals(5.0, it.score, "${it.area} should sit at the midpoint")
            assertEquals(ScoreSource.ESTIMATED, it.source)
            assertTrue(it.needsConfirmation, "${it.area} should be flagged for confirmation")
        }
    }

    @Test
    fun `a user's own score is never overwritten by a prediction`() {
        val strongSignal = mapOf(
            WheelArea.PHYSICAL to AreaSignals(habits = 4, habitCompletionRate = 1.0, longestStreak = 60)
        )

        val scores = WheelScorePredictor.predict(
            signalsByArea = strongSignal,
            global = GlobalSignals(averageDailySteps = 15_000, averageSleepHours = 8.0),
            userScores = mapOf(WheelArea.PHYSICAL to 3.0),
        )

        val physical = scores.first { it.area == WheelArea.PHYSICAL }
        assertEquals(3.0, physical.score)
        assertEquals(ScoreSource.USER, physical.source)
        assertEquals(1.0, physical.confidence)
    }

    @Test
    fun `kept habits score above the midpoint and broken ones below`() {
        fun scoreFor(rate: Double) = WheelScorePredictor.predict(
            mapOf(WheelArea.MONEY to AreaSignals(habits = 2, habitCompletionRate = rate)),
            GlobalSignals(),
        ).first { it.area == WheelArea.MONEY }.score

        assertTrue(scoreFor(0.95) > 5.0, "a kept habit should read better than neutral")
        assertTrue(scoreFor(0.1) < 5.0, "an abandoned habit should read worse than neutral")
    }

    @Test
    fun `health data moves Physical and earns confidence`() {
        val sedentary = WheelScorePredictor.predict(
            mapOf(WheelArea.PHYSICAL to AreaSignals(habits = 1, habitCompletionRate = 0.5)),
            GlobalSignals(averageDailySteps = 1_500, averageSleepHours = 5.0),
        ).first { it.area == WheelArea.PHYSICAL }

        val active = WheelScorePredictor.predict(
            mapOf(WheelArea.PHYSICAL to AreaSignals(habits = 1, habitCompletionRate = 0.5)),
            GlobalSignals(averageDailySteps = 12_000, averageSleepHours = 8.0),
        ).first { it.area == WheelArea.PHYSICAL }

        assertTrue(active.score > sedentary.score, "12k steps should beat 1.5k")
        // Measured data, not inference, so it should not be asking the user to confirm.
        assertTrue(active.confidence > 0.5)
        assertEquals(ScoreSource.PREDICTED, active.source)
    }

    @Test
    fun `Mental follows recorded mood rather than goal activity`() {
        val low = WheelScorePredictor.predict(
            emptyMap(),
            GlobalSignals(averageMood = 1.5, journalEntriesThisMonth = 10),
        ).first { it.area == WheelArea.MENTAL }

        val high = WheelScorePredictor.predict(
            emptyMap(),
            GlobalSignals(averageMood = 4.8, journalEntriesThisMonth = 10),
        ).first { it.area == WheelArea.MENTAL }

        assertTrue(low.score < 4.0, "a low mood should read low, was ${low.score}")
        assertTrue(high.score > 8.0, "a high mood should read high, was ${high.score}")
        assertEquals(ScoreSource.PREDICTED, high.source)
    }

    @Test
    fun `Growth reads the Learn hub and Abilities`() {
        val idle = WheelScorePredictor.predict(emptyMap(), GlobalSignals())
            .first { it.area == WheelArea.GROWTH }

        val learning = WheelScorePredictor.predict(
            emptyMap(),
            GlobalSignals(lessonsReadThisMonth = 10, abilitiesInProgress = 3),
        ).first { it.area == WheelArea.GROWTH }

        assertTrue(learning.score > idle.score)
        assertEquals(ScoreSource.PREDICTED, learning.source)
        assertTrue(learning.basis.contains("lesson"), "basis should say why: ${learning.basis}")
    }

    @Test
    fun `Joy reads the whole wheel and the mood, not goals of its own`() {
        val joy = WheelScorePredictor.predict(
            mapOf(
                WheelArea.PHYSICAL to AreaSignals(habits = 3, habitCompletionRate = 0.9, longestStreak = 30),
                WheelArea.MONEY to AreaSignals(habits = 2, habitCompletionRate = 0.9),
            ),
            GlobalSignals(averageMood = 4.5, journalEntriesThisMonth = 8),
        ).first { it.area == WheelArea.JOY }

        assertEquals(ScoreSource.PREDICTED, joy.source)
        assertTrue(joy.score > 5.0, "a good wheel and a good mood should not read as joyless")
    }

    @Test
    fun `Romance has no signal to draw on and says so`() {
        val romance = WheelScorePredictor.predict(
            mapOf(WheelArea.PHYSICAL to AreaSignals(habits = 3, habitCompletionRate = 0.9)),
            GlobalSignals(averageMood = 4.0, journalEntriesThisMonth = 10),
        ).first { it.area == WheelArea.ROMANCE }

        // Nothing in the app tracks this, so inventing a number would be dishonest.
        assertEquals(ScoreSource.ESTIMATED, romance.source)
        assertEquals(0.0, romance.confidence)
        assertTrue(romance.needsConfirmation)
    }

    @Test
    fun `every score lands on the half-point grid the wheel draws`() {
        val scores = WheelScorePredictor.predict(
            WheelArea.segments().associateWith {
                AreaSignals(habits = 3, habitCompletionRate = 0.37, completedGoals = 1, longestStreak = 21)
            },
            GlobalSignals(averageMood = 3.3, journalEntriesThisMonth = 7, averageDailySteps = 8_123),
        )

        scores.forEach {
            val doubled = it.score * 2
            assertEquals(doubled, kotlin.math.round(doubled), "${it.area} is off-grid at ${it.score}")
        }
    }

    @Test
    fun `every prediction explains itself`() {
        val scores = WheelScorePredictor.predict(
            WheelArea.segments().associateWith { AreaSignals(habits = 1, habitCompletionRate = 0.8) },
            GlobalSignals(averageMood = 4.0, journalEntriesThisMonth = 5),
        )

        scores.forEach {
            assertTrue(it.basis.isNotBlank(), "${it.area} gave a number with no reason")
        }
    }

    @Test
    fun `focus points at the weakest measured area and anything we guessed`() {
        val scores = WheelScorePredictor.predict(
            mapOf(
                WheelArea.PHYSICAL to AreaSignals(habits = 3, habitCompletionRate = 0.95),
                WheelArea.MONEY to AreaSignals(habits = 3, habitCompletionRate = 0.05),
            ),
            GlobalSignals(),
        )

        val focus = WheelScorePredictor.suggestedFocus(scores)

        assertEquals(WheelArea.MONEY, focus.first(), "the weakest measured area should lead")
        assertTrue(focus.contains(WheelArea.ROMANCE), "unconfirmed areas should be raised too")
    }

    @Test
    fun `every area states what a ten looks like`() {
        WheelArea.entries.forEach {
            assertTrue(it.rubric.isNotBlank(), "${it.name} has no rubric, so its score means nothing")
            assertTrue(it.displayName.isNotBlank())
        }
        assertEquals(10, WheelArea.entries.size)
        assertNotNull(WheelArea.entries.firstOrNull { it.displayName == "Mission" })
    }
}
