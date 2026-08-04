package az.tribe.lifeplanner.domain.service

/**
 * The length of a step that is really a timer.
 *
 * A lot of steps are already written as a duration — "Hold crow pose 30 seconds", "Meditate for 5
 * minutes", "Plank 45s". For those, a tick box is the wrong control: the user has to do the thing,
 * watch a clock somewhere else, then come back and tick. Finding the number in the title lets the
 * row run the timer itself, and complete when it finishes.
 *
 * Deliberately conservative. A false positive puts a play button on something you cannot start, so
 * this only fires on an explicit number next to an explicit unit, and only inside a range where a
 * countdown is a sensible thing to sit and watch.
 */
object StepDuration {

    /** Below this, a "timer" is over before the animation is. */
    private const val MIN_SECONDS = 5

    /**
     * Above this, nobody is watching a countdown in a feed row. An hour of deep work is a real
     * step, but it belongs in the focus timer, not here.
     */
    private const val MAX_SECONDS = 30 * 60

    private val PATTERNS = listOf(
        // "30 seconds", "30 second", "30 secs", "30 sec", "30s"
        Regex("""(\d{1,4})\s*(?:seconds|second|secs|sec|s)\b""", RegexOption.IGNORE_CASE) to 1,
        // "5 minutes", "5 minute", "5 mins", "5 min", "5m"
        Regex("""(\d{1,4})\s*(?:minutes|minute|mins|min|m)\b""", RegexOption.IGNORE_CASE) to 60,
    )

    /**
     * @return the step's length in seconds, or null when the title does not name one we can run.
     */
    fun secondsIn(title: String): Int? {
        // Minutes first: "5 min" would otherwise match the seconds pattern on its trailing letters
        // for titles like "5 mins" -> no, but "90 m" vs "90 s" ordering keeps the intent explicit.
        val candidates = PATTERNS.mapNotNull { (regex, multiplier) ->
            regex.find(title)?.let { match ->
                val value = match.groupValues[1].toIntOrNull() ?: return@let null
                value * multiplier
            }
        }

        // Longest wins: "Hold for 2 minutes 30 seconds" should offer 150s, not 30s. Summing would
        // be wrong just as often ("run 5k in 30 minutes"), so the safe read is the larger unit.
        val seconds = candidates.maxOrNull() ?: return null
        return seconds.takeIf { it in MIN_SECONDS..MAX_SECONDS }
    }

    /** "0:30", "1:05", "12:00" — what a countdown should read as. */
    fun format(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "$m:${if (s < 10) "0$s" else "$s"}"
    }
}
