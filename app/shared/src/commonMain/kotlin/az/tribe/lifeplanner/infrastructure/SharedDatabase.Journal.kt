package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.JournalEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- Journal Entry operations ---

suspend fun SharedDatabase.getAllJournalEntries(): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllJournalEntries().executeAsList() }
}

suspend fun SharedDatabase.getJournalEntryById(id: String): JournalEntryEntity? {
    return this { db -> db.lifePlannerDBQueries.getJournalEntryById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getJournalEntriesByDate(date: String): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getJournalEntriesByDate(date).executeAsList() }
}

suspend fun SharedDatabase.getJournalEntriesByGoalId(goalId: String): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getJournalEntriesByGoalId(goalId).executeAsList() }
}

suspend fun SharedDatabase.getJournalEntriesByHabitId(habitId: String): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getJournalEntriesByHabitId(habitId).executeAsList() }
}

suspend fun SharedDatabase.getJournalEntriesByMood(mood: String): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getJournalEntriesByMood(mood).executeAsList() }
}

suspend fun SharedDatabase.getJournalEntriesInRange(startDate: String, endDate: String): List<JournalEntryEntity> {
    return this { db ->
        db.lifePlannerDBQueries.getJournalEntriesInRange(startDate, endDate).executeAsList()
    }
}

suspend fun SharedDatabase.getRecentJournalEntries(limit: Long): List<JournalEntryEntity> {
    return this { db -> db.lifePlannerDBQueries.getRecentJournalEntries(limit).executeAsList() }
}

suspend fun SharedDatabase.insertJournalEntry(entry: JournalEntryEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertJournalEntry(
            id = entry.id,
            title = entry.title,
            content = entry.content,
            mood = entry.mood,
            linkedGoalId = entry.linkedGoalId,
            linkedHabitId = entry.linkedHabitId,
            promptUsed = entry.promptUsed,
            tags = entry.tags,
            date = entry.date,
            createdAt = entry.createdAt,
            updatedAt = entry.updatedAt,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateJournalEntry(entry: JournalEntryEntity) {
    this { db ->
        db.lifePlannerDBQueries.updateJournalEntry(
            title = entry.title,
            content = entry.content,
            mood = entry.mood,
            linkedGoalId = entry.linkedGoalId,
            linkedHabitId = entry.linkedHabitId,
            promptUsed = entry.promptUsed,
            tags = entry.tags,
            updatedAt = entry.updatedAt,
            id = entry.id
        )
    }
}

suspend fun SharedDatabase.deleteJournalEntry(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteJournalEntry(nowTimestamp(), id) }
}

suspend fun SharedDatabase.searchJournalEntries(query: String): List<JournalEntryEntity> {
    return this { db ->
        db.lifePlannerDBQueries.searchJournalEntries(query, query).executeAsList()
    }
}

suspend fun SharedDatabase.getMoodCountInRange(startDate: String, endDate: String): Map<String, Long> {
    return this { db ->
        db.lifePlannerDBQueries.getMoodCountInRange(startDate, endDate).executeAsList()
            .associate { result -> result.mood to result.COUNT }
    }
}

// --- Reactive Flow observer ---

fun SharedDatabase.observeAllJournalEntries(): Flow<List<JournalEntryEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getAllJournalEntries()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
