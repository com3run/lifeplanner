package az.tribe.lifeplanner.ui.habit

/**
 * Parses numeric targets from habit title/description strings.
 * Returns (count, unit) or null if no numeric pattern is found.
 *
 * Examples:
 *   "Drink 2L of water"    → (2, "L")
 *   "Do pushups 8 times"   → (8, "times")
 *   "Meditate 10min"       → (10, "min")
 *   "Run for 30 minutes"   → (30, "min")
 *   "Drink 8 glasses"      → (8, "glasses")
 *   "Read 20 pages"        → (20, "pages")
 *   "Walk 5000 steps"      → (5000, "steps")
 */
object HabitNumericParser {

    private val patterns = listOf(
        // "2L" or "2.5L"
        Regex("""(\d+(?:\.\d+)?)\s*[Ll](?:\s|$)""") to { m: MatchResult -> parse(m, "L") },
        // "10min" or "10 min" or "30 minutes"
        Regex("""(\d+)\s*min(?:utes?)?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "min") },
        // "2 hours" or "1h"
        Regex("""(\d+)\s*h(?:ours?)?(?:\s|$)""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "hrs") },
        // "8 times" or "10 times"
        Regex("""(\d+)\s*times?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "times") },
        // "8 glasses"
        Regex("""(\d+)\s*glasses?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "glasses") },
        // "20 pages"
        Regex("""(\d+)\s*pages?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "pages") },
        // "5000 steps"
        Regex("""(\d+)\s*steps?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "steps") },
        // "3 sets"
        Regex("""(\d+)\s*sets?""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "sets") },
        // "5 km" or "5km"
        Regex("""(\d+(?:\.\d+)?)\s*kms?(?:\s|$)""", RegexOption.IGNORE_CASE) to { m: MatchResult -> parse(m, "km") },
    )

    private fun parse(match: MatchResult, unit: String): Pair<Int, String> {
        val count = match.groupValues[1].toDoubleOrNull()?.toInt() ?: 1
        return count to unit
    }

    fun parse(text: String): Pair<Int, String>? {
        for ((pattern, extractor) in patterns) {
            val match = pattern.find(text) ?: continue
            return extractor(match)
        }
        return null
    }
}
