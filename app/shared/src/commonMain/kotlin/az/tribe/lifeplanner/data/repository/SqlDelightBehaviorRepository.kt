package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.database.ScreenTimeEventEntity
import az.tribe.lifeplanner.domain.model.AppUsagePattern
import az.tribe.lifeplanner.domain.repository.BehaviorRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.deleteOldScreenTimeEvents
import az.tribe.lifeplanner.infrastructure.getAppUsagePattern
import az.tribe.lifeplanner.infrastructure.getScreenTimeEventCount
import az.tribe.lifeplanner.infrastructure.getScreenTimeEventsForDateRange
import az.tribe.lifeplanner.infrastructure.insertScreenTimeEvent
import az.tribe.lifeplanner.infrastructure.upsertAppUsagePattern
import co.touchlab.kermit.Logger
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SqlDelightBehaviorRepository(
    private val db: SharedDatabase
) : BehaviorRepository {

    private val tz = TimeZone.currentSystemDefault()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun recordScreenEnter(screen: String) {
        val now = Clock.System.now()
        val date = now.toLocalDateTime(tz).date.toString()
        db.insertScreenTimeEvent(
            ScreenTimeEventEntity(
                id = Uuid.random().toString(),
                screen = screen,
                eventType = "ENTER",
                timestamp = now.toString(),
                durationMs = 0L,
                date = date
            )
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun recordScreenExit(screen: String, durationMs: Long) {
        val now = Clock.System.now()
        val date = now.toLocalDateTime(tz).date.toString()
        db.insertScreenTimeEvent(
            ScreenTimeEventEntity(
                id = Uuid.random().toString(),
                screen = screen,
                eventType = "EXIT",
                timestamp = now.toString(),
                durationMs = durationMs,
                date = date
            )
        )
    }

    override suspend fun getPattern(): AppUsagePattern? {
        val entity = db.getAppUsagePattern() ?: return null
        val count = db.getScreenTimeEventCount()
        return entity.toDomain(count)
    }

    override suspend fun refreshPattern() {
        val today = Clock.System.now().toLocalDateTime(tz).date
        val thirtyDaysAgo = today.minus(30, DateTimeUnit.DAY)
        val events = db.getScreenTimeEventsForDateRange(thirtyDaysAgo.toString(), today.toString())
        val count = db.getScreenTimeEventCount()

        if (events.isEmpty()) return

        val hourCounts = IntArray(24)
        val dayCounts = mutableMapOf<String, Int>()
        val featureDurations = mutableMapOf<String, MutableList<Long>>()
        var sessionCount = 0
        var totalSessionMs = 0L
        val seenDates = mutableSetOf<String>()

        events.forEach { event ->
            try {
                val instant = kotlinx.datetime.Instant.parse(event.timestamp)
                val dt = instant.toLocalDateTime(tz)
                hourCounts[dt.hour]++
                val dayKey = dt.dayOfWeek.name.take(3)
                dayCounts[dayKey] = (dayCounts[dayKey] ?: 0) + 1

                if (event.eventType == "ENTER") {
                    if (!seenDates.contains(event.date)) { sessionCount++; seenDates.add(event.date) }
                } else if (event.eventType == "EXIT" && event.durationMs > 0) {
                    featureDurations.getOrPut(event.screen) { mutableListOf() }.add(event.durationMs)
                    totalSessionMs += event.durationMs
                }
            } catch (_: Exception) { }
        }

        val sortedHours = hourCounts.mapIndexed { hour, c -> hour to c }
            .sortedByDescending { it.second }
            .filter { it.second > 0 }
            .map { it.first }

        val sortedDays = dayCounts.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val featureEngagement = featureDurations.mapValues { (_, durations) ->
            if (durations.isEmpty()) 0L else durations.sum() / durations.size
        }

        val featureEngagementJson = buildJsonObject {
            featureEngagement.forEach { (screen, avgMs) -> put(screen, avgMs) }
        }.toString()

        val avgSessionMinutes = if (sessionCount > 0)
            totalSessionMs / sessionCount / 1000.0 / 60.0 else 0.0
        val sessionsPerWeek = sessionCount / (30.0 / 7.0)

        db.upsertAppUsagePattern(
            mostActiveHours = sortedHours.take(3).joinToString(","),
            mostActiveDays = sortedDays.take(3).joinToString(","),
            featureEngagementJson = featureEngagementJson,
            sessionAvgMinutes = avgSessionMinutes,
            sessionsPerWeek = sessionsPerWeek,
            lastUpdated = Clock.System.now().toString()
        )
        Logger.d("BehaviorRepository") { "Pattern refreshed: ${sortedHours.take(3)} hours, ${sortedDays.take(3)} days" }
    }

    override suspend fun pruneOldEvents(keepDays: Int) {
        val today = Clock.System.now().toLocalDateTime(tz).date
        val cutoff = today.minus(keepDays, DateTimeUnit.DAY).toString()
        db.deleteOldScreenTimeEvents(cutoff)
    }

    private fun az.tribe.lifeplanner.database.UserActivityPatternEntity.toDomain(count: Long): AppUsagePattern {
        val hours = mostActiveHours.split(",").mapNotNull { it.trim().toIntOrNull() }
        val days = mostActiveDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val engagement: Map<String, Long> = try {
            val obj = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .parseToJsonElement(featureEngagementJson) as? JsonObject ?: JsonObject(emptyMap())
            obj.entries.associate { (k, v) -> k to v.jsonPrimitive.long }
        } catch (_: Exception) { emptyMap() }
        return AppUsagePattern(
            mostActiveHours = hours,
            mostActiveDays = days,
            featureEngagement = engagement,
            sessionAvgMinutes = sessionAvgMinutes,
            sessionsPerWeek = sessionsPerWeek,
            bestCheckInTimes = emptyMap(),
            lastUpdated = lastUpdated,
            totalEventCount = count
        )
    }
}
