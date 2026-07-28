package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.domain.model.Coordinates
import az.tribe.lifeplanner.domain.model.HourForecast
import az.tribe.lifeplanner.domain.model.TodayWeather
import az.tribe.lifeplanner.domain.model.WeatherCondition
import az.tribe.lifeplanner.domain.repository.WeatherRepository
import az.tribe.lifeplanner.domain.service.WeatherAlertBuilder
import az.tribe.lifeplanner.location.LocationProvider
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.time.Clock

/**
 * Fetches today's weather from **Open-Meteo** (free, keyless) for the device's coarse location, and
 * builds the plan-relevant alert from the hourly forecast. Returns null on any failure so the UI can
 * simply stay quiet rather than surface errors.
 */
class WeatherRepositoryImpl(
    private val client: HttpClient,
    private val locationProvider: LocationProvider,
) : WeatherRepository {

    override suspend fun today(): TodayWeather? {
        return try {
            val coords = locationProvider.currentCoordinates() ?: return null
            val dto = fetch(coords)
            val current = dto.current ?: return null

            val tz = TimeZone.currentSystemDefault()
            val nowHour = Clock.System.now().toLocalDateTime(tz).hour

            val hours = buildHours(dto)
            val alert = WeatherAlertBuilder.build(hours, nowHour)

            val place = runCatching { locationProvider.placeName(coords) }.getOrNull()
            val highLow = dailyHighLow(dto) ?: hours.let {
                (it.maxOfOrNull { h -> h.temperatureC } ?: current.temperature.roundToInt()) to
                    (it.minOfOrNull { h -> h.temperatureC } ?: current.temperature.roundToInt())
            }

            val hourly = hours.filter { it.hour >= nowHour }.take(HOURLY_CELLS).map {
                az.tribe.lifeplanner.domain.model.HourlyBrief(
                    hour = it.hour,
                    temperatureC = it.temperatureC,
                    condition = WeatherCondition.fromWmo(it.wmoCode),
                    precipitationProbability = it.precipitationProbability,
                )
            }

            TodayWeather(
                placeName = place,
                temperatureC = current.temperature.roundToInt(),
                condition = WeatherCondition.fromWmo(current.weatherCode),
                highC = highLow.first,
                lowC = highLow.second,
                alert = alert,
                hourly = hourly,
                details = buildDetails(dto),
                source = "Open-Meteo",
            )
        } catch (e: Exception) {
            Logger.w("WeatherRepository") { "today() failed: ${e.message}" }
            null
        }
    }

    private suspend fun fetch(coords: Coordinates): OpenMeteoResponse =
        client.get(BASE_URL) {
            parameter("latitude", coords.latitude)
            parameter("longitude", coords.longitude)
            parameter("current", "temperature_2m,weather_code,apparent_temperature,relative_humidity_2m,wind_speed_10m")
            parameter("hourly", "temperature_2m,precipitation_probability,weather_code")
            parameter("daily", "temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max")
            parameter("forecast_days", 1)
            parameter("timezone", "auto")
        }.body()

    private fun buildDetails(dto: OpenMeteoResponse): az.tribe.lifeplanner.domain.model.WeatherDetails {
        val cur = dto.current
        val daily = dto.daily
        return az.tribe.lifeplanner.domain.model.WeatherDetails(
            feelsLikeC = cur?.apparentTemperature?.roundToInt(),
            humidityPct = cur?.humidity,
            windSpeedKmh = cur?.windSpeed?.roundToInt(),
            uvIndexMax = daily?.uvIndexMax?.firstOrNull()?.roundToInt(),
            rainChanceMaxPct = daily?.precipProbabilityMax?.firstOrNull(),
            sunrise = daily?.sunrise?.firstOrNull()?.let { timeOf(it) },
            sunset = daily?.sunset?.firstOrNull()?.let { timeOf(it) },
        )
    }

    /** "2026-07-28T06:12" -> "6:12 AM". */
    private fun timeOf(iso: String): String? {
        val hm = iso.substringAfter('T', "").takeIf { it.isNotBlank() } ?: return null
        val h = hm.substringBefore(':').toIntOrNull() ?: return null
        val m = hm.substringAfter(':').take(2)
        val period = if (h < 12) "AM" else "PM"
        val h12 = when (h % 12) { 0 -> 12; else -> h % 12 }
        return "$h12:$m $period"
    }

    /** Zip the hourly arrays into today's [HourForecast] list (local hour parsed from the ISO time). */
    private fun buildHours(dto: OpenMeteoResponse): List<HourForecast> {
        val h = dto.hourly ?: return emptyList()
        val times = h.time ?: return emptyList()
        val temps = h.temperature.orEmpty()
        val codes = h.weatherCode.orEmpty()
        val probs = h.precipitationProbability.orEmpty()
        return times.indices.mapNotNull { i ->
            val hour = times[i].substringAfter('T').substringBefore(':').toIntOrNull() ?: return@mapNotNull null
            HourForecast(
                hour = hour,
                wmoCode = codes.getOrNull(i) ?: 0,
                temperatureC = (temps.getOrNull(i) ?: 0.0).roundToInt(),
                precipitationProbability = probs.getOrNull(i) ?: -1,
            )
        }
    }

    private fun dailyHighLow(dto: OpenMeteoResponse): Pair<Int, Int>? {
        val d = dto.daily ?: return null
        val hi = d.temperatureMax?.firstOrNull() ?: return null
        val lo = d.temperatureMin?.firstOrNull() ?: return null
        return hi.roundToInt() to lo.roundToInt()
    }

    private companion object {
        const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
        const val HOURLY_CELLS = 10 // how many upcoming hours to show in the by-time strip
    }
}

// ─── Open-Meteo response DTOs (only the fields we use; unknown keys ignored) ──────────────────────

@Serializable
private data class OpenMeteoResponse(
    val current: Current? = null,
    val hourly: Hourly? = null,
    val daily: Daily? = null,
)

@Serializable
private data class Current(
    @SerialName("temperature_2m") val temperature: Double = 0.0,
    @SerialName("weather_code") val weatherCode: Int = 0,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Int? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null,
)

@Serializable
private data class Hourly(
    val time: List<String>? = null,
    @SerialName("temperature_2m") val temperature: List<Double>? = null,
    @SerialName("precipitation_probability") val precipitationProbability: List<Int>? = null,
    @SerialName("weather_code") val weatherCode: List<Int>? = null,
)

@Serializable
private data class Daily(
    @SerialName("temperature_2m_max") val temperatureMax: List<Double>? = null,
    @SerialName("temperature_2m_min") val temperatureMin: List<Double>? = null,
    @SerialName("sunrise") val sunrise: List<String>? = null,
    @SerialName("sunset") val sunset: List<String>? = null,
    @SerialName("uv_index_max") val uvIndexMax: List<Double>? = null,
    @SerialName("precipitation_probability_max") val precipProbabilityMax: List<Int>? = null,
)
