package az.tribe.lifeplanner.domain.model

import kotlinx.datetime.LocalDate

/** Pillar 4, how much to trust an insight, driven purely by sample size. */
enum class InsightConfidence { LOW, MODERATE, HIGH }

enum class InsightKind {
    /** A statistical relationship between two signals (Pearson correlation). */
    CORRELATION,
    /** A self-reinforcing trajectory caught early (the "innate" amplification spiral). */
    AMPLIFICATION_SPIRAL
}

/**
 * Pillar 4 (Causal Model), one plain-language finding derived on-device from the user's
 * own history (no AI). Always carries its sample size + confidence so it stays honest about
 * small data.
 */
data class CausalInsight(
    val kind: InsightKind,
    val statement: String,
    val strength: Double,   // |r| for CORRELATION (0..1); decline magnitude for a spiral
    val sampleSize: Int,
    val confidence: InsightConfidence
)

/**
 * One day's worth of the signals the engine correlates. Any field may be null (no data that
 * day); the engine only pairs days where both signals in a relationship are present.
 *
 * - [habitsCompleted] number of habits checked in
 * - [mood] journal mood score 1..5
 * - [focusMinutes] focus-session minutes
 * - [sleepHours] from HealthMetric / UserSituation
 * - [energy] energy rating 1..5
 * - [goalProgressDelta] progress gained that day
 */
data class DayMetrics(
    val date: LocalDate,
    val habitsCompleted: Int? = null,
    val mood: Int? = null,
    val focusMinutes: Int? = null,
    val sleepHours: Double? = null,
    val energy: Int? = null,
    val goalProgressDelta: Double? = null
)
