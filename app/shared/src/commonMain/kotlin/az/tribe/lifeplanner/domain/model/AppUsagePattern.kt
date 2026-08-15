package az.tribe.lifeplanner.domain.model

data class AppUsagePattern(
    val mostActiveHours: List<Int>,           // hour-of-day (0-23), sorted by activity
    val mostActiveDays: List<String>,          // "MON"-"SUN", sorted by activity
    val featureEngagement: Map<String, Long>,  // screen route → avg duration ms
    val sessionAvgMinutes: Double,
    val sessionsPerWeek: Double,
    val bestCheckInTimes: Map<String, String>, // feature key → "HH:MM"
    val lastUpdated: String,
    val totalEventCount: Long = 0L
) {
    val hasEnoughData: Boolean get() = totalEventCount >= 10

    fun topFeatures(limit: Int = 3): List<Pair<String, Long>> =
        featureEngagement.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.toPair() }

    /**
     * The stretch of day the user shows up most, or null when nothing is recorded yet. The exact
     * hour used to ride along ("morning (11:00)"), which on the 10-event sample that unlocks
     * this claim is more precision than the data holds, and tied hours resolved to whichever
     * came first. A period is a claim the sample can actually carry; "morning" as a default was
     * an invented fact.
     */
    fun peakHourLabel(): String? {
        val h = mostActiveHours.firstOrNull() ?: return null
        return when (h) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }
    }
}
