package az.tribe.lifeplanner.domain.model

data class AppUsagePattern(
    val mostActiveHours: List<Int>,           // hour-of-day (0–23), sorted by activity
    val mostActiveDays: List<String>,          // "MON"–"SUN", sorted by activity
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

    fun peakHourLabel(): String {
        val h = mostActiveHours.firstOrNull() ?: return "morning"
        return when (h) {
            in 5..11 -> "morning ($h:00)"
            in 12..17 -> "afternoon ($h:00)"
            in 18..21 -> "evening ($h:00)"
            else -> "night ($h:00)"
        }
    }
}
