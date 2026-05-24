package az.tribe.lifeplanner.domain.service

import kotlin.math.round

/** Pillar 4, predicted-vs-actual completion calibration across the user's finished goals. */
data class Calibration(
    val ratio: Double,      // median actual/predicted; >1 = slower than estimated
    val sampleSize: Int,
    val statement: String
)

/**
 * Pillar 4, pure calibration stat. Given (predictedDays, actualDays) per completed goal,
 * reports the median actual/predicted ratio as a plain statement. Median (not mean) so one
 * runaway goal doesn't dominate. Deterministic → unit-testable.
 */
class CalibrationEngine(private val minSamples: Int = 3) {

    data class Sample(val predictedDays: Int, val actualDays: Int)

    fun calibrate(samples: List<Sample>): Calibration? {
        val ratios = samples
            .filter { it.predictedDays > 0 && it.actualDays > 0 }
            .map { it.actualDays.toDouble() / it.predictedDays }
            .sorted()
        if (ratios.size < minSamples) return null

        val median = medianOf(ratios)
        val statement = when {
            median >= 1.15 -> "You finish goals about ${oneDecimal(median)}× slower than you estimate."
            median <= 0.87 -> "You finish goals about ${oneDecimal(1.0 / median)}× faster than you estimate."
            else -> "Your goal time estimates are about right, within ~15%."
        }
        return Calibration(ratio = median, sampleSize = ratios.size, statement = statement)
    }

    private fun medianOf(sorted: List<Double>): Double {
        val n = sorted.size
        return if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
    }

    // KMP-safe one-decimal formatting (no java String.format in commonMain).
    private fun oneDecimal(v: Double): String {
        val r = round(v * 10) / 10.0
        val whole = r.toInt()
        val dec = ((r - whole) * 10 + 0.5).toInt() % 10
        return "$whole.$dec"
    }
}
