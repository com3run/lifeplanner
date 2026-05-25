package az.tribe.lifeplanner.ui.health

import az.tribe.lifeplanner.domain.model.Story
import kotlin.math.roundToInt

internal fun generateHealthStories(
    todaySteps: Long?,
    latestHeartRate: Double?,
    latestSleep: Double?,
    latestWeight: Double?,
): List<Story> {
    val stories = mutableListOf<Story>()

    if (todaySteps != null) {
        val goal = 10_000L
        val percent = ((todaySteps.toDouble() / goal) * 100).roundToInt().coerceAtMost(100)
        val stepsLabel = formatCompact(todaySteps.toDouble())
        val (emoji, title, subtitle) = when {
            percent >= 100 -> Triple(
                "🏆", "Goal Crushed!",
                "You hit $stepsLabel steps today, that's your 10k goal!\n\nConsistency like this builds lasting fitness habits."
            )
            percent >= 70 -> Triple(
                "🔥", "$stepsLabel Steps",
                "You're at $percent% of your daily goal.\n\nJust a short walk away from hitting 10k, keep it going!"
            )
            percent >= 40 -> Triple(
                "👟", "$stepsLabel Steps",
                "You're at $percent% of your 10k goal.\n\nEvery step you take is a vote for a healthier you."
            )
            else -> Triple(
                "💪", "Get Moving",
                "Only $stepsLabel steps so far today.\n\nA 15-minute walk can shift your energy and mood significantly."
            )
        }
        stories.add(Story(
            id = "health_steps_today",
            title = title,
            subtitle = subtitle,
            emoji = emoji,
            category = "health",
            gradientStart = "#1565C0",
            gradientEnd = "#42A5F5",
            ctaText = "Open Health",
            ctaAction = "health"
        ))
    }

    if (latestHeartRate != null) {
        val bpm = latestHeartRate.roundToInt()
        val (emoji, title, subtitle) = when {
            bpm < 60 -> Triple(
                "💙", "Athlete's Heart",
                "Your resting heart rate is $bpm bpm, that's excellent.\n\nLow resting HR is a strong indicator of cardiovascular fitness."
            )
            bpm < 80 -> Triple(
                "❤️", "$bpm BPM",
                "Your heart rate is in a healthy range.\n\nKeep up the regular activity and rest, your heart is happy."
            )
            bpm < 100 -> Triple(
                "🫀", "$bpm BPM",
                "Your heart rate is slightly elevated.\n\nCheck in on stress and sleep, they're the biggest drivers of resting HR."
            )
            else -> Triple(
                "⚠️", "$bpm BPM",
                "Your heart rate is higher than usual.\n\nConsider lighter activity today and make sure you're staying hydrated."
            )
        }
        stories.add(Story(
            id = "health_heart_rate",
            title = title,
            subtitle = subtitle,
            emoji = emoji,
            category = "health",
            gradientStart = "#B71C1C",
            gradientEnd = "#EF5350",
            ctaText = "Open Health",
            ctaAction = "health"
        ))
    }

    if (latestSleep != null) {
        val label = formatSleepDuration(latestSleep)
        val (emoji, title, subtitle) = when {
            latestSleep >= 8.0 -> Triple(
                "😴", "$label Sleep",
                "You got a full night's rest, well done.\n\nQuality sleep powers your mood, memory, and recovery more than any supplement."
            )
            latestSleep >= 6.5 -> Triple(
                "🌙", "$label Sleep",
                "Decent rest last night.\n\nAiming for 7-9 hours will keep your energy and focus sharp throughout the day."
            )
            latestSleep >= 5.0 -> Triple(
                "😪", "$label Sleep",
                "You may be running a sleep debt.\n\nTry to protect your wind-down time tonight, your body does its best work when rested."
            )
            else -> Triple(
                "🥱", "$label Sleep",
                "That's not enough recovery time.\n\nPrioritize sleep tonight, it's one of the highest-leverage health decisions you can make."
            )
        }
        stories.add(Story(
            id = "health_sleep",
            title = title,
            subtitle = subtitle,
            emoji = emoji,
            category = "health",
            gradientStart = "#311B92",
            gradientEnd = "#7986CB",
            ctaText = "Open Health",
            ctaAction = "health"
        ))
    }

    if (latestWeight != null) {
        stories.add(Story(
            id = "health_weight",
            title = "Weight: ${"%.1f".format(latestWeight)} kg",
            subtitle = "You're actively tracking your weight, that alone puts you ahead.\n\nConsistent data points are what make trends visible over time.",
            emoji = "⚖️",
            category = "health",
            gradientStart = "#1B5E20",
            gradientEnd = "#66BB6A",
            ctaText = "Log Weight",
            ctaAction = "health"
        ))
    }

    return stories
}
