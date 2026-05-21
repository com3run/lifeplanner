package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.ScreenTimeEventEntity

// --- ScreenTimeEventEntity operations ---

suspend fun SharedDatabase.insertScreenTimeEvent(event: ScreenTimeEventEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertScreenTimeEvent(
            id = event.id,
            screen = event.screen,
            eventType = event.eventType,
            timestamp = event.timestamp,
            durationMs = event.durationMs,
            date = event.date
        )
    }
}

suspend fun SharedDatabase.getScreenTimeEventsForDateRange(
    startDate: String,
    endDate: String
): List<ScreenTimeEventEntity> {
    return this { db ->
        db.lifePlannerDBQueries.getScreenTimeEventsForDateRange(startDate, endDate).executeAsList()
    }
}

suspend fun SharedDatabase.getScreenTimeEventCount(): Long {
    return this { db ->
        db.lifePlannerDBQueries.getScreenTimeEventCount().executeAsOne()
    }
}

suspend fun SharedDatabase.deleteOldScreenTimeEvents(beforeDate: String) {
    this { db ->
        db.lifePlannerDBQueries.deleteOldScreenTimeEvents(beforeDate)
    }
}

// Upserts the full pattern including the new behavioral columns.
// The existing insertUserActivityPattern in SharedDatabase.Reminders.kt handles the
// smart-reminder fields; this one writes the complete row for the insight feature.
suspend fun SharedDatabase.upsertAppUsagePattern(
    mostActiveHours: String,
    mostActiveDays: String,
    featureEngagementJson: String,
    sessionAvgMinutes: Double,
    sessionsPerWeek: Double,
    lastUpdated: String
) {
    this { db ->
        db.lifePlannerDBQueries.insertUserActivityPattern(
            id = "default",
            mostActiveHours = mostActiveHours,
            mostActiveDays = mostActiveDays,
            averageResponseTime = 0L,
            bestCheckInTimes = "",
            featureEngagementJson = featureEngagementJson,
            sessionAvgMinutes = sessionAvgMinutes,
            sessionsPerWeek = sessionsPerWeek,
            lastUpdated = lastUpdated
        )
    }
}

suspend fun SharedDatabase.getAppUsagePattern() =
    this { db -> db.lifePlannerDBQueries.getUserActivityPattern().executeAsOneOrNull() }
