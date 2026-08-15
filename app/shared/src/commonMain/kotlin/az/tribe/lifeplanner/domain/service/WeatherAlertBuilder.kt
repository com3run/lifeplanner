package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.HourForecast
import az.tribe.lifeplanner.domain.model.WeatherCondition

/**
 * Turns today's remaining hourly forecast into one short, plan-relevant nudge, "how might today's
 * weather change what I do?". Pure and platform-free so it is unit-testable.
 *
 * Only the hours from [nowHour] onward matter. Rain is communicated by its chance: a moderate chance
 * shows the percentage, and a genuinely wet stretch (> [HEAVY_PROB]) also shows its window so the
 * user knows roughly how long to wait it out. Priority: thunderstorm, rain, then temperature
 * extremes. Returns null when nothing stands out, so the UI can stay quiet.
 */
object WeatherAlertBuilder {

    fun build(hours: List<HourForecast>, nowHour: Int): String? {
        val ahead = hours.filter { it.hour >= nowHour }
        if (ahead.isEmpty()) return null

        val storm = ahead.firstOrNull { WeatherCondition.fromWmo(it.wmoCode) == WeatherCondition.THUNDERSTORM }
        if (storm != null) {
            val p = storm.precipitationProbability
            val chance = if (p in 1..100) " (~$p%)" else ""
            return "Thunderstorms around ${clock(storm.hour)}$chance, keep today flexible."
        }

        // A genuinely wet window: only hours whose chance is actually high (> HEAVY_PROB). This is
        // the "wait it out" period, so we don't flag a whole afternoon off a low-chance shower code.
        val heavyPeak = ahead.filter { it.precipitationProbability > HEAVY_PROB }.maxByOrNull { it.precipitationProbability }
        if (heavyPeak != null) {
            val peakIdx = ahead.indexOf(heavyPeak)
            var s = peakIdx
            while (s - 1 >= 0 && ahead[s - 1].hour == ahead[s].hour - 1 && ahead[s - 1].precipitationProbability > HEAVY_PROB) s--
            var e = peakIdx
            while (e + 1 < ahead.size && ahead[e + 1].hour == ahead[e].hour + 1 && ahead[e + 1].precipitationProbability > HEAVY_PROB) e++
            val peak = (s..e).maxOf { ahead[it].precipitationProbability }
            return "Rain likely ${clock(ahead[s].hour)}–${clock(ahead[e].hour + 1)}, up to $peak%. A good window to stay in."
        }

        // Moderate chance: lead with the percentage, no window.
        val notable = ahead.filter { it.precipitationProbability >= NOTABLE_PROB }
        if (notable.isNotEmpty()) {
            val onset = notable.first()
            val peak = notable.maxOf { it.precipitationProbability }
            return if (onset.hour <= nowHour + 1) "Rain moving in, ~$peak% chance, grab a jacket."
            else "Rain possible around ${clock(onset.hour)}, ~$peak% chance."
        }

        // Rain in the forecast by condition, but low/unknown probability.
        val rainCode = ahead.firstOrNull {
            val c = WeatherCondition.fromWmo(it.wmoCode)
            c == WeatherCondition.RAIN || c == WeatherCondition.HEAVY_RAIN ||
                c == WeatherCondition.SHOWERS || c == WeatherCondition.DRIZZLE
        }
        if (rainCode != null) {
            return if (rainCode.hour <= nowHour + 1) "Rain moving in, grab a jacket if you're heading out."
            else "Rain likely around ${clock(rainCode.hour)}."
        }

        val hottest = ahead.maxByOrNull { it.temperatureC }
        if (hottest != null && hottest.temperatureC >= HOT_C) {
            return "Hot later, up to ${hottest.temperatureC}° around ${clock(hottest.hour)}, hydrate."
        }

        val coldest = ahead.minByOrNull { it.temperatureC }
        if (coldest != null && coldest.temperatureC <= FREEZING_C) {
            return "Freezing today, low of ${coldest.temperatureC}°, bundle up before you head out."
        }

        return null
    }

    /** 24h hour → friendly 12h clock, e.g. 15 -> "3 PM", 0 -> "12 AM". */
    private fun clock(hour24: Int): String {
        val h = ((hour24 % 24) + 24) % 24
        val period = if (h < 12) "AM" else "PM"
        val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
        return "$h12 $period"
    }

    private const val NOTABLE_PROB = 50 // mention rain at or above this chance
    private const val HEAVY_PROB = 75   // only show a "wait it out" window above this chance
    private const val HOT_C = 32
    private const val FREEZING_C = 0
}
