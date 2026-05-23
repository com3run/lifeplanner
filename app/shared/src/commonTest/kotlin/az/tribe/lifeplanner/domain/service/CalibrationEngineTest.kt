package az.tribe.lifeplanner.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalibrationEngineTest {

    private val engine = CalibrationEngine()
    private fun s(p: Int, a: Int) = CalibrationEngine.Sample(p, a)

    @Test
    fun `consistently slower goals report a slower-than-estimate ratio`() {
        // predicted 10 days, actually 18 → 1.8x
        val result = engine.calibrate(listOf(s(10, 18), s(10, 18), s(10, 18)))
        assertTrue(result != null)
        assertEquals(1.8, result!!.ratio, 0.001)
        assertEquals(3, result.sampleSize)
        assertTrue(result.statement.contains("1.8×") && result.statement.contains("slower", true))
    }

    @Test
    fun `consistently faster goals report a faster-than-estimate ratio`() {
        // predicted 20, actually 10 → 0.5x → 2.0x faster
        val result = engine.calibrate(listOf(s(20, 10), s(20, 10), s(20, 10)))
        assertTrue(result != null && result.statement.contains("faster", true))
        assertEquals(0.5, result!!.ratio, 0.001)
    }

    @Test
    fun `accurate estimates report within-range`() {
        val result = engine.calibrate(listOf(s(10, 10), s(10, 11), s(10, 9)))
        assertTrue(result != null && result.statement.contains("about right", true))
    }

    @Test
    fun `median ignores a single runaway goal`() {
        // ratios: 1.0, 1.0, 1.0, 10.0 → median 1.0 (mean would be ~3.25)
        val result = engine.calibrate(listOf(s(10, 10), s(10, 10), s(10, 10), s(10, 100)))
        assertEquals(1.0, result!!.ratio, 0.001)
    }

    @Test
    fun `too few samples yields null`() {
        assertNull(engine.calibrate(listOf(s(10, 18), s(10, 18))))
    }

    @Test
    fun `non-positive durations are ignored`() {
        assertNull(engine.calibrate(listOf(s(0, 18), s(10, 0), s(-5, 18))))
    }
}
