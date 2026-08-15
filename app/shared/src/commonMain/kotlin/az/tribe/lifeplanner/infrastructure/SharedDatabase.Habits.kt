package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.HabitCheckInEntity
import az.tribe.lifeplanner.database.HabitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- Habit operations ---

suspend fun SharedDatabase.getAllHabits(): List<HabitEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllHabits().executeAsList() }
}

suspend fun SharedDatabase.getHabitById(id: String): HabitEntity? {
    return this { db -> db.lifePlannerDBQueries.getHabitById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getHabitsByCategory(category: String): List<HabitEntity> {
    return this { db -> db.lifePlannerDBQueries.getHabitsByCategory(category).executeAsList() }
}

suspend fun SharedDatabase.getHabitsByGoalId(goalId: String): List<HabitEntity> {
    return this { db -> db.lifePlannerDBQueries.getHabitsByGoalId(goalId).executeAsList() }
}

suspend fun SharedDatabase.insertHabit(habit: HabitEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertHabit(
            id = habit.id,
            title = habit.title,
            description = habit.description,
            category = habit.category,
            frequency = habit.frequency,
            targetCount = habit.targetCount,
            currentStreak = habit.currentStreak,
            longestStreak = habit.longestStreak,
            totalCompletions = habit.totalCompletions,
            lastCompletedDate = habit.lastCompletedDate,
            linkedGoalId = habit.linkedGoalId,
            correlationScore = habit.correlationScore,
            isActive = habit.isActive,
            createdAt = habit.createdAt,
            reminderTime = habit.reminderTime,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null,
            type = habit.type,
            unit = habit.unit,
            healthMetricType = habit.healthMetricType,
            healthTarget = habit.healthTarget,
            completionSource = habit.completionSource
        )
    }
}

suspend fun SharedDatabase.updateHabit(
    id: String,
    title: String,
    description: String,
    category: String,
    frequency: String,
    targetCount: Long,
    linkedGoalId: String?,
    reminderTime: String?,
    type: String,
    unit: String? = null,
    healthMetricType: String? = null,
    healthTarget: Double? = null,
    completionSource: String? = null
) {
    this { db ->
        db.lifePlannerDBQueries.updateHabit(
            title = title,
            description = description,
            category = category,
            frequency = frequency,
            targetCount = targetCount,
            linkedGoalId = linkedGoalId,
            reminderTime = reminderTime,
            type = type,
            unit = unit,
            healthMetricType = healthMetricType,
            healthTarget = healthTarget,
            completionSource = completionSource,
            id = id
        )
    }
}

suspend fun SharedDatabase.updateHabitStreak(
    id: String,
    currentStreak: Long,
    longestStreak: Long,
    totalCompletions: Long,
    lastCompletedDate: String?
) {
    this { db ->
        db.lifePlannerDBQueries.updateHabitStreak(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = totalCompletions,
            lastCompletedDate = lastCompletedDate,
            id = id
        )
    }
}

suspend fun SharedDatabase.updateHabitCorrelation(id: String, correlationScore: Double) {
    this { db ->
        db.lifePlannerDBQueries.updateHabitCorrelation(
            correlationScore = correlationScore,
            id = id
        )
    }
}

suspend fun SharedDatabase.deactivateHabit(id: String) {
    this { db -> db.lifePlannerDBQueries.deactivateHabit(id) }
}

suspend fun SharedDatabase.deleteHabit(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteHabit(nowTimestamp(), id) }
}

// --- Habit Check-in operations ---

suspend fun SharedDatabase.insertHabitCheckIn(checkIn: HabitCheckInEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertHabitCheckIn(
            id = checkIn.id,
            habitId = checkIn.habitId,
            date = checkIn.date,
            completed = checkIn.completed,
            notes = checkIn.notes,
            count = checkIn.count,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.insertHabitCheckInOrIgnore(checkIn: HabitCheckInEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertHabitCheckInOrIgnore(
            id = checkIn.id,
            habitId = checkIn.habitId,
            date = checkIn.date,
            completed = checkIn.completed,
            notes = checkIn.notes,
            count = checkIn.count,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateHabitCheckInCount(habitId: String, date: String, count: Long, completed: Long) {
    this { db ->
        db.lifePlannerDBQueries.updateHabitCheckInCount(
            count = count,
            completed = completed,
            habitId = habitId,
            date = date
        )
    }
}

suspend fun SharedDatabase.getCheckInsByHabitId(habitId: String): List<HabitCheckInEntity> {
    return this { db -> db.lifePlannerDBQueries.getCheckInsByHabitId(habitId).executeAsList() }
}

suspend fun SharedDatabase.getCheckInsByDate(date: String): List<HabitCheckInEntity> {
    return this { db -> db.lifePlannerDBQueries.getCheckInsByDate(date).executeAsList() }
}

suspend fun SharedDatabase.getAllCheckInsInRange(startDate: String, endDate: String): List<HabitCheckInEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllCheckInsInRange(startDate, endDate).executeAsList() }
}

suspend fun SharedDatabase.getCheckInByHabitAndDate(habitId: String, date: String): HabitCheckInEntity? {
    return this { db ->
        db.lifePlannerDBQueries.getCheckInByHabitAndDate(habitId, date).executeAsOneOrNull()
    }
}

suspend fun SharedDatabase.getSoftDeletedCheckIn(habitId: String, date: String): HabitCheckInEntity? {
    return this { db ->
        db.lifePlannerDBQueries.getSoftDeletedCheckIn(habitId, date).executeAsOneOrNull()
    }
}

suspend fun SharedDatabase.restoreHabitCheckIn(id: String) {
    this { db -> db.lifePlannerDBQueries.restoreHabitCheckIn(nowTimestamp(), id) }
}

// Single query to get all completed check-in dates for streak calculation (eliminates N+1)
suspend fun SharedDatabase.getCompletedCheckInDatesDesc(habitId: String): List<String> {
    return this { db ->
        db.lifePlannerDBQueries.getCompletedCheckInDatesDesc(habitId).executeAsList()
    }
}

suspend fun SharedDatabase.deleteDuplicateCheckIns() {
    this { db -> db.lifePlannerDBQueries.deleteDuplicateCheckIns() }
}

/**
 * Force SQLDelight to invalidate cached queries on habit-related tables.
 * Needed when external writers (e.g. Glance widget) modify the DB
 * outside the SQLDelight driver.
 */
suspend fun SharedDatabase.invalidateHabitCache() {
    this { db -> db.lifePlannerDBQueries.deleteDuplicateCheckIns() }
}

suspend fun SharedDatabase.getCheckInsInRange(habitId: String, startDate: String, endDate: String): List<HabitCheckInEntity> {
    return this { db ->
        db.lifePlannerDBQueries.getCheckInsInRange(habitId, startDate, endDate).executeAsList()
    }
}

suspend fun SharedDatabase.deleteCheckIn(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteHabitCheckIn(nowTimestamp(), id) }
}

// --- Reactive Flow observers ---

fun SharedDatabase.observeAllHabits(): Flow<List<HabitEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getAllHabits()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}

fun SharedDatabase.observeCheckInsByDate(date: String): Flow<List<HabitCheckInEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getCheckInsByDate(date)
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}

fun SharedDatabase.observeCheckInsInRange(startDate: String, endDate: String): Flow<List<HabitCheckInEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getAllCheckInsInRange(startDate, endDate)
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
