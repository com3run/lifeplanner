package az.tribe.lifeplanner.domain

import az.tribe.lifeplanner.domain.model.HourForecast
import az.tribe.lifeplanner.domain.service.WeatherAlertBuilder
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeatherAlertBuilderTest {

    private fun hour(h: Int, code: Int = 0, temp: Int = 20, prob: Int = 0) =
        HourForecast(hour = h, wmoCode = code, temperatureC = temp, precipitationProbability = prob)

    @Test
    fun highChanceShowsWindowAndPercentage() {
        // 14:00 and 15:00 are > 75%; 16:00 (70%) is not, so the window is 2 PM–4 PM.
        val hours = listOf(hour(14, prob = 85), hour(15, prob = 90), hour(16, prob = 70))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("90%") && alert.contains("2 PM") && alert.contains("4 PM"), "got: $alert")
    }

    @Test
    fun lowChanceShowerHoursDoNotWidenTheWindow() {
        // A wet peak at 4–5 PM (80/78%) must not start the window at a 1 PM low-chance shower (25%).
        val hours = listOf(hour(13, prob = 25), hour(14, prob = 41), hour(16, prob = 80), hour(17, prob = 78))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("4 PM") && !alert.contains("1 PM"), "got: $alert")
    }

    @Test
    fun moderateChanceLeadsWithPercentageNoWindow() {
        val hours = listOf(hour(15, prob = 60))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("60%") && alert.contains("3 PM") && !alert.contains("–"), "got: $alert")
    }

    @Test
    fun thunderstormTakesPriorityAndShowsChance() {
        val hours = listOf(hour(14, code = 61, prob = 55), hour(16, code = 95, prob = 40))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 13)
        assertTrue(alert != null && alert.contains("Thunderstorm") && alert.contains("40%"), "got: $alert")
    }

    @Test
    fun rainCodeWithNoProbabilityStillWarns() {
        val hours = listOf(hour(15, code = 61, prob = -1)) // rain code, unknown probability
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("Rain likely") && alert.contains("3 PM"), "got: $alert")
    }

    @Test
    fun ignoresHoursThatAlreadyPassed() {
        val hours = listOf(hour(8, prob = 90))
        assertNull(WeatherAlertBuilder.build(hours, nowHour = 14))
    }

    @Test
    fun flagsHeatWhenNoRain() {
        val hours = listOf(hour(13, temp = 30), hour(15, temp = 34))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("34"), "got: $alert")
    }

    @Test
    fun quietOnAnUneventfulDay() {
        val hours = listOf(hour(12, code = 1, temp = 21, prob = 10), hour(15, code = 0, temp = 23, prob = 5))
        assertNull(WeatherAlertBuilder.build(hours, nowHour = 12))
    }
}
