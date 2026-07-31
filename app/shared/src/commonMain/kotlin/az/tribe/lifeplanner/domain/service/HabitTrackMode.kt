package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.Habit

/**
 * How a habit is measured, and therefore how its card behaves.
 *
 * [SINGLE] is one tap, done. [COUNT] accumulates discrete reps the user taps out (8 glasses,
 * 20 pages). [DURATION] is time: "Meditate 10 min" is a single act that lasts ten minutes, not ten
 * separate things to tick. Asking for ten taps was the old behaviour and it made no sense, so a
 * duration habit completes in one tap or gets filled automatically by a timed session.
 */
enum class HabitTrackMode(val label: String) {
    SINGLE("Just check"),
    COUNT("Count"),
    DURATION("Minutes"),
}

/**
 * Units [HabitNumericParser] can produce that mean time. Hours are normalised to minutes by
 * [az.tribe.lifeplanner.usecases.habit.BackfillHabitTargetsUseCase], but older rows may still
 * carry "hrs", so both are recognised.
 */
private val TIME_UNITS = setOf("min", "mins", "minute", "minutes", "hr", "hrs", "hour", "hours")

/** Whether [unit] names a span of time rather than a countable thing. */
fun isTimeUnit(unit: String?): Boolean = unit?.trim()?.lowercase() in TIME_UNITS

val Habit.trackMode: HabitTrackMode
    get() = when {
        isTimeUnit(unit) -> HabitTrackMode.DURATION
        targetCount > 1 -> HabitTrackMode.COUNT
        else -> HabitTrackMode.SINGLE
    }

/** A duration habit's target expressed in minutes, for timers and progress. */
val Habit.targetMinutes: Int?
    get() = if (trackMode == HabitTrackMode.DURATION) targetCount.coerceAtLeast(1) else null
