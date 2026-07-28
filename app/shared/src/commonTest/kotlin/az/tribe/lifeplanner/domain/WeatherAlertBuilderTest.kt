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
    fun flagsHeavyRainWithTime() {
        val hours = listOf(hour(12), hour(13), hour(15, code = 65)) // 65 = heavy rain
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("Heavy rain") && alert.contains("3 PM"), "got: $alert")
    }

    @Test
    fun thunderstormTakesPriorityOverRain() {
        val hours = listOf(hour(14, code = 61), hour(16, code = 95)) // rain then thunderstorm
        val alert = WeatherAlertBuilder.build(hours, nowHour = 13)
        assertTrue(alert != null && alert.contains("Thunderstorm"), "got: $alert")
    }

    @Test
    fun usesPrecipitationProbabilityWhenCodeIsMild() {
        val hours = listOf(hour(15, code = 2, prob = 80))
        val alert = WeatherAlertBuilder.build(hours, nowHour = 12)
        assertTrue(alert != null && alert.contains("Rain"), "got: $alert")
    }

    @Test
    fun ignoresHoursThatAlreadyPassed() {
        val hours = listOf(hour(8, code = 65)) // heavy rain, but this morning
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
        val hours = listOf(hour(12, code = 1, temp = 21), hour(15, code = 0, temp = 23))
        assertNull(WeatherAlertBuilder.build(hours, nowHour = 12))
    }
}
