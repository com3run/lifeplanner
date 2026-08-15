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
private val TIME_UNITS = setOf(
    "sec", "secs", "second", "seconds",
    "min", "mins", "minute", "minutes",
    "hr", "hrs", "hour", "hours",
)

/** Whether [unit] names a span of time rather than a countable thing. */
fun isTimeUnit(unit: String?): Boolean = unit?.trim()?.lowercase() in TIME_UNITS

/** Seconds in one of [TIME_UNITS], so a target can be normalised whatever unit it was written in. */
private fun secondsPerUnit(unit: String?): Int = when (unit?.trim()?.lowercase()) {
    "sec", "secs", "second", "seconds" -> 1
    "hr", "hrs", "hour", "hours" -> 3600
    else -> 60 // minutes, the default for a time unit
}

val Habit.trackMode: HabitTrackMode
    get() = when {
        isTimeUnit(unit) -> HabitTrackMode.DURATION
        targetCount > 1 -> HabitTrackMode.COUNT
        else -> HabitTrackMode.SINGLE
    }

/**
 * A duration habit's target in seconds, for timers and progress. Seconds rather than minutes so a
 * 30-second plank is expressible: rounding it up to a minute would make the timer lie about the
 * exercise. Stored data is untouched — targetCount keeps whatever number the user wrote and unit
 * says which unit it was, so this is a read-side conversion with no migration behind it.
 */
val Habit.targetSeconds: Int?
    get() = if (trackMode == HabitTrackMode.DURATION) {
        (targetCount.coerceAtLeast(1)) * secondsPerUnit(unit)
    } else null

/** A duration habit's target in whole minutes, rounded up. Null for sub-minute habits' callers. */
val Habit.targetMinutes: Int?
    get() = targetSeconds?.let { (it + 59) / 60 }

/**
 * A duration habit's target stated in the unit it was written in: "30 sec", "3 min", "2 hrs".
 * Shared so every surface says the same thing — a hardcoded "min" here renders a 30-second plank
 * as half an hour.
 */
val Habit.durationLabel: String
    get() {
        val seconds = targetSeconds ?: return ""
        return when {
            seconds < 60 -> "$seconds sec"
            seconds % 3600 == 0 -> "${seconds / 3600} hr${if (seconds > 3600) "s" else ""}"
            else -> "${seconds / 60} min"
        }
    }
