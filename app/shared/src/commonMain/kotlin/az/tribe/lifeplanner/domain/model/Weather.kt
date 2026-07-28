package az.tribe.lifeplanner.domain.model

/** A geographic point, coarse is fine for weather. */
data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * A weather condition bucket, mapped from Open-Meteo WMO codes. [emoji] is used as a lightweight,
 * platform-free icon on the Today surface.
 */
enum class WeatherCondition(val label: String, val emoji: String) {
    CLEAR("Clear", "☀️"),
    PARTLY_CLOUDY("Partly cloudy", "⛅"),
    CLOUDY("Cloudy", "☁️"),
    FOG("Fog", "🌫️"),
    DRIZZLE("Drizzle", "🌦️"),
    RAIN("Rain", "🌧️"),
    HEAVY_RAIN("Heavy rain", "🌧️"),
    SNOW("Snow", "🌨️"),
    SHOWERS("Showers", "🌦️"),
    THUNDERSTORM("Thunderstorm", "⛈️"),
    UNKNOWN("Weather", "🌡️");

    companion object {
        /** Map a WMO weather-interpretation code (Open-Meteo) to a bucket. */
        fun fromWmo(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 66 -> RAIN
            65, 67 -> HEAVY_RAIN
            71, 73, 75, 77 -> SNOW
            80, 81 -> SHOWERS
            82 -> HEAVY_RAIN
            85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}

/** One hour of today's forecast, the input to [az.tribe.lifeplanner.domain.service.WeatherAlertBuilder]. */
data class HourForecast(
    val hour: Int,                 // 0..23, local time
    val wmoCode: Int,
    val temperatureC: Int,
    val precipitationProbability: Int, // 0..100, may be -1 when unknown
)

/**
 * Today's weather for the Today surface: current conditions for the user's place plus an optional
 * plain-language [alert] about a notable change coming later today ("Heavy rain around 3 PM").
 */
data class TodayWeather(
    val placeName: String?,
    val temperatureC: Int,
    val condition: WeatherCondition,
    val highC: Int,
    val lowC: Int,
    val alert: String?,
)
