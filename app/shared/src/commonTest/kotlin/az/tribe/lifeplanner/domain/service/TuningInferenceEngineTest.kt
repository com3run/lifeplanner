package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.BehaviorWindow
import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.TuningDial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TuningInferenceEngineTest {

    private val engine = TuningInferenceEngine() // saturationSamples = 20

    @Test
    fun `no evidence leaves every dial neutral, unconfident, and unreliable`() {
        val p = engine.infer(prior = null, window = BehaviorWindow(), newProfileId = "p1")
        TuningDial.entries.forEach { dial ->
            val s = p.dial(dial)
            assertEquals(0.5f, s.value, 0.0001f, "$dial should stay neutral")
            assertEquals(0f, s.confidence, 0.0001f, "$dial should have zero confidence")
            assertEquals(0, s.sampleSize)
            assertFalse(s.isReliable, "$dial should not be reliable without evidence")
        }
    }

    @Test
    fun `acting quickly on suggestions infers a LOW confidence threshold`() {
        // 8 of 10 surfaced options acted on quickly ⇒ ratio 0.8 ⇒ threshold value 0.2.
        val p = engine.infer(null, BehaviorWindow(suggestionsSurfaced = 10, suggestionsActedOnQuickly = 8), "p1")
        val s = p.dial(TuningDial.CONFIDENCE_THRESHOLD)
        assertEquals(0.2f, s.value, 0.0001f)
        assertEquals(10, s.sampleSize)
        assertEquals(0.5f, s.confidence, 0.0001f) // 10 / 20
        assertTrue(s.isReliable) // confidence >= 0.5 and n >= 10
    }

    @Test
    fun `high category variety infers high novelty salience`() {
        val p = engine.infer(null, BehaviorWindow(goalsStarted = 4, distinctCategoriesStarted = 4), "p1")
        val s = p.dial(TuningDial.NOVELTY_SALIENCE)
        assertEquals(1f, s.value, 0.0001f)
        assertEquals(4, s.sampleSize)
        assertEquals(0.2f, s.confidence, 0.0001f) // 4 / 20 → still low confidence
        assertFalse(s.isReliable)
    }

    @Test
    fun `delay discounting combines short-horizon preference and cut-short focus sessions`() {
        // short/long: 3 of 4 short ⇒ 0.75 (n=4). focus: actual 0.5× planned ⇒ 0.5 (n=6).
        // evidence-weighted: (0.75*4 + 0.5*6) / 10 = 0.6
        val p = engine.infer(
            null,
            BehaviorWindow(
                shortHorizonGoals = 3, longHorizonGoals = 1,
                focusSessions = 6, avgFocusActualOverPlanned = 0.5f,
            ),
            "p1",
        )
        val s = p.dial(TuningDial.DELAY_DISCOUNTING)
        assertEquals(0.6f, s.value, 0.0001f)
        assertEquals(10, s.sampleSize)
    }

    @Test
    fun `focus session signal is ignored when there were no sessions`() {
        val p = engine.infer(
            null,
            BehaviorWindow(shortHorizonGoals = 3, longHorizonGoals = 1, focusSessions = 0, avgFocusActualOverPlanned = 0.1f),
            "p1",
        )
        val s = p.dial(TuningDial.DELAY_DISCOUNTING)
        assertEquals(0.75f, s.value, 0.0001f) // only the short/long ratio counts
        assertEquals(4, s.sampleSize)
    }

    @Test
    fun `evidence accumulates across successive windows and confidence saturates`() {
        // Window 1: always acted quickly ⇒ threshold value 0.0, n=10.
        val p1 = engine.infer(null, BehaviorWindow(suggestionsSurfaced = 10, suggestionsActedOnQuickly = 10), "p1")
        // Window 2: never acted quickly ⇒ threshold value 1.0, n=10.
        val p2 = engine.infer(p1, BehaviorWindow(suggestionsSurfaced = 10, suggestionsActedOnQuickly = 0), "ignored")

        val s = p2.dial(TuningDial.CONFIDENCE_THRESHOLD)
        assertEquals(0.5f, s.value, 0.0001f) // (0*10 + 1*10) / 20
        assertEquals(20, s.sampleSize)
        assertEquals(1f, s.confidence, 0.0001f) // 20 / 20, capped
        assertEquals("p1", p2.id, "infer should reuse the prior profile id")
    }

    @Test
    fun `a window with no signal for a dial preserves that dial's prior`() {
        val p1 = engine.infer(null, BehaviorWindow(safeGoalsChosen = 8, ambitiousGoalsChosen = 2), "p1")
        val before = p1.dial(TuningDial.RISK_AVERSION)
        // A later window touches only novelty — risk aversion has no new evidence.
        val p2 = engine.infer(p1, BehaviorWindow(goalsStarted = 3, distinctCategoriesStarted = 1), "ignored")
        val after = p2.dial(TuningDial.RISK_AVERSION)
        assertEquals(before.value, after.value, 0.0001f)
        assertEquals(before.sampleSize, after.sampleSize)
    }

    @Test
    fun `confidence is capped at one even with large samples`() {
        val p = engine.infer(null, BehaviorWindow(habitMisses = 80, abandonmentsAfterMiss = 80), "p1")
        val s = p.dial(TuningDial.PUNISHMENT_SENSITIVITY)
        assertEquals(1f, s.value, 0.0001f)
        assertEquals(1f, s.confidence, 0.0001f)
        assertEquals(80, s.sampleSize)
    }

    @Test
    fun `a fresh profile uses the supplied id`() {
        val p = engine.infer(null, BehaviorWindow(rewardEvents = 4, reengagementsAfterReward = 1), "new-profile-id")
        assertEquals("new-profile-id", p.id)
        assertEquals(0.25f, p.dial(TuningDial.REWARD_SENSITIVITY).value, 0.0001f)
    }
}
