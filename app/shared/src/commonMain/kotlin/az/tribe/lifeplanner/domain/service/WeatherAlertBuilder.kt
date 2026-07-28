package az.tribe.lifeplanner.domain.service

import az.tribe.lifeplanner.domain.model.HourForecast
import az.tribe.lifeplanner.domain.model.WeatherCondition

/**
 * Turns today's remaining hourly forecast into one short, plan-relevant nudge, "how might today's
 * weather change what I do?". Pure and platform-free so it is unit-testable.
 *
 * Only the hours from [nowHour] onward matter (we nudge about what's ahead, not the morning that
 * already happened). Picks a single most-salient signal in priority order: thunderstorm, heavy rain,
 * rain, then temperature extremes. Returns null when nothing stands out, so the UI can stay quiet.
 */
object WeatherAlertBuilder {

    fun build(hours: List<HourForecast>, nowHour: Int): String? {
        val ahead = hours.filter { it.hour >= nowHour }
        if (ahead.isEmpty()) return null

        val storm = ahead.firstOrNull { WeatherCondition.fromWmo(it.wmoCode) == WeatherCondition.THUNDERSTORM }
        if (storm != null) return "Thunderstorms likely around ${clock(storm.hour)}, keep today flexible."

        val heavy = ahead.firstOrNull { WeatherCondition.fromWmo(it.wmoCode) == WeatherCondition.HEAVY_RAIN }
        if (heavy != null) return "Heavy rain around ${clock(heavy.hour)}, plan indoor steps."

        // Rain / showers / drizzle, or a high chance of precipitation.
        val rain = ahead.firstOrNull {
            val c = WeatherCondition.fromWmo(it.wmoCode)
            c == WeatherCondition.RAIN || c == WeatherCondition.SHOWERS || c == WeatherCondition.DRIZZLE ||
                it.precipitationProbability >= RAIN_PROB
        }
        if (rain != null) {
            val soon = rain.hour <= nowHour + 1
            return if (soon) "Rain moving in, grab a jacket if you're heading out."
            else "Rain likely around ${clock(rain.hour)}, an earlier walk beats a wet one."
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

    private const val RAIN_PROB = 60
    private const val HOT_C = 32
    private const val FREEZING_C = 0
}
