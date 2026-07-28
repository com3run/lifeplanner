package az.tribe.lifeplanner.domain.repository

import az.tribe.lifeplanner.domain.model.TodayWeather

/** Today's weather for the user's current place, or null when unavailable (offline/denied). */
interface WeatherRepository {
    suspend fun today(): TodayWeather?
}
