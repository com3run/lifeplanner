package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.DecisionProfile
import az.tribe.lifeplanner.domain.model.DialSetting
import az.tribe.lifeplanner.domain.model.TuningDial
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdherenceForecastEngineTest {

    private val engine = AdherenceForecastEngine()

    private fun plan(
        horizon: Int = 90,
        moves: Int = 3,
        nearWins: Boolean = false,
        mitigations: Int = 0,
    ) = GoalPlan(
        horizonDays = horizon,
        majorMoveCount = moves,
        hasNearTermWins = nearWins,
        mitigationCount = mitigations,
    )

    @Test
    fun coldStart_sitsNearBaseRate_withLowConfidenceAndNoClaimedDrivers() {
        val f = engine.forecast(profile = null, plan = plan())
        assertTrue(f.isColdStart)
        assertEquals(ForecastConfidence.LOW, f.confidence)
        assertTrue(f.adherencePercent in 40..50, "cold start should sit near the base rate, was ${f.adherencePercent}")
        assertTrue(f.bandPercent >= 20, "cold start should show a wide band")
        assertTrue(f.drivers.isEmpty(), "must not claim drivers with no evidence")
    }

    @Test
    fun impatientUser_onLongHorizon_lowersAdherence_withADelayDriver() {
        val impatient = DecisionProfile(
            id = "u",
            delayDiscounting = DialSetting(value = 0.9f, confidence = 0.8f, sampleSize = 40),
        )
        val withProfile = engine.forecast(impatient, plan(horizon = 300, moves = 3))
        val neutral = engine.forecast(profile = null, plan = plan(horizon = 300, moves = 3))

        assertTrue(withProfile.adherencePercent < neutral.adherencePercent)
        assertTrue(
            withProfile.drivers.any {
                it.dial == TuningDial.DELAY_DISCOUNTING && it.direction == ForecastDriver.Direction.LOWERS
            },
            "an impatient user on a long-horizon goal should see a delay-discounting driver",
        )
    }

    @Test
    fun crystalBallMitigations_raiseAdherence() {
        val setbackSensitive = DecisionProfile(
            id = "u",
            punishmentSensitivity = DialSetting(value = 0.9f, confidence = 0.8f, sampleSize = 30),
        )
        val without = engine.forecast(setbackSensitive, plan(mitigations = 0))
        val withFixes = engine.forecast(setbackSensitive, plan(mitigations = 3))
        assertTrue(
            withFixes.adherencePercent > without.adherencePercent,
            "writing if-then fixes should raise the forecast (${withFixes.adherencePercent} vs ${without.adherencePercent})",
        )
    }

    @Test
    fun lowConfidenceDial_barelyMovesTheEstimate() {
        val unsure = DecisionProfile(
            id = "u",
            delayDiscounting = DialSetting(value = 0.9f, confidence = 0.05f, sampleSize = 1),
        )
        val a = engine.forecast(unsure, plan(horizon = 300))
        val b = engine.forecast(profile = null, plan = plan(horizon = 300))
        assertTrue(
            abs(a.adherencePercent - b.adherencePercent) <= 2,
            "a barely-sampled dial must not swing the forecast",
        )
    }

    @Test
    fun wellCalibratedFinisher_isNotPenalised() {
        val f = engine.forecast(profile = null, plan = plan(), calibrationRatio = 1.0, calibrationSamples = 6)
        assertTrue(f.adherencePercent >= 45)
        assertTrue(f.drivers.any { it.direction == ForecastDriver.Direction.RAISES })
    }

    @Test
    fun chronicSlipper_isTrimmed() {
        val onTime = engine.forecast(profile = null, plan = plan(), calibrationRatio = 1.0, calibrationSamples = 6)
        val slips = engine.forecast(profile = null, plan = plan(), calibrationRatio = 1.8, calibrationSamples = 6)
        assertTrue(slips.adherencePercent < onTime.adherencePercent)
    }

    @Test
    fun output_isAlwaysInSaneRange_evenAtExtremes() {
        val extreme = DecisionProfile(
            id = "u",
            delayDiscounting = DialSetting(0.99f, 1f, 99),
            punishmentSensitivity = DialSetting(0.99f, 1f, 99),
            riskAversion = DialSetting(0.01f, 1f, 99),
        )
        val f = engine.forecast(
            profile = extreme,
            plan = plan(horizon = 365, moves = 10),
            calibrationRatio = 2.0,
            calibrationSamples = 8,
        )
        assertTrue(f.adherencePercent in 5..95, "was ${f.adherencePercent}")
        assertTrue(f.bandPercent in 0..40)
        assertTrue(f.drivers.size <= 2, "surface at most two drivers")
    }
}
