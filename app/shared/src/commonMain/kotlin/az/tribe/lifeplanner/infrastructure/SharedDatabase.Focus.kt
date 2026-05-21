package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.BadgeEntity
import az.tribe.lifeplanner.database.BeginnerObjectiveEntity
import az.tribe.lifeplanner.database.FocusSessionEntity
import az.tribe.lifeplanner.database.GetGoalChangesOnDate
import az.tribe.lifeplanner.database.GetHabitCheckInsWithHabitForDate
import az.tribe.lifeplanner.database.GoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// ===== Focus Session Operations =====

fun SharedDatabase.observeAllFocusSessions(): Flow<List<FocusSessionEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllFocusSessions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}

suspend fun SharedDatabase.insertFocusSession(
    id: String,
    goalId: String,
    milestoneId: String,
    plannedDurationMinutes: Long,
    actualDurationSeconds: Long,
    wasCompleted: Long,
    xpEarned: Long,
    startedAt: String,
    completedAt: String?,
    createdAt: String,
    mood: String? = null,
    ambientSound: String? = null,
    focusTheme: String? = null
) {
    this { db ->
        db.lifePlannerDBQueries.insertFocusSession(
            id, goalId, milestoneId, plannedDurationMinutes,
            actualDurationSeconds, wasCompleted, xpEarned,
            startedAt, completedAt, createdAt,
            mood, ambientSound, focusTheme,
            null, 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.updateFocusSession(
    id: String,
    actualDurationSeconds: Long,
    wasCompleted: Long,
    xpEarned: Long,
    completedAt: String?,
    mood: String? = null,
    ambientSound: String? = null,
    focusTheme: String? = null
) {
    this { db ->
        db.lifePlannerDBQueries.updateFocusSession(
            actualDurationSeconds, wasCompleted, xpEarned, completedAt,
            mood, ambientSound, focusTheme, id
        )
    }
}

suspend fun SharedDatabase.getFocusSessionById(id: String): FocusSessionEntity? {
    return this { db -> db.lifePlannerDBQueries.getFocusSessionById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getFocusSessionsByGoalId(goalId: String): List<FocusSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getFocusSessionsByGoalId(goalId).executeAsList() }
}

suspend fun SharedDatabase.getFocusSessionsByMilestoneId(milestoneId: String): List<FocusSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getFocusSessionsByMilestoneId(milestoneId).executeAsList() }
}

suspend fun SharedDatabase.getCompletedFocusSessions(): List<FocusSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getCompletedFocusSessions().executeAsList() }
}

suspend fun SharedDatabase.getTotalFocusSeconds(): Long {
    return this { db -> db.lifePlannerDBQueries.getTotalFocusSeconds().executeAsOne() }
}

suspend fun SharedDatabase.getTotalFocusSessionCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getTotalFocusSessionCount().executeAsOne() }
}

suspend fun SharedDatabase.getTodayFocusSessions(todayDate: String): List<FocusSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getTodayFocusSessions(todayDate).executeAsList() }
}

// ===== Retrospective Operations =====

suspend fun SharedDatabase.getHabitCheckInsWithHabitForDate(date: String): List<GetHabitCheckInsWithHabitForDate> {
    return this { db -> db.lifePlannerDBQueries.getHabitCheckInsWithHabitForDate(date).executeAsList() }
}

suspend fun SharedDatabase.getFocusSessionsByDate(datePrefix: String): List<FocusSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getFocusSessionsByDate(datePrefix).executeAsList() }
}

suspend fun SharedDatabase.getGoalChangesOnDate(datePrefix: String): List<GetGoalChangesOnDate> {
    return this { db -> db.lifePlannerDBQueries.getGoalChangesOnDate(datePrefix).executeAsList() }
}

suspend fun SharedDatabase.getBadgesEarnedOnDate(datePrefix: String): List<BadgeEntity> {
    return this { db -> db.lifePlannerDBQueries.getBadgesEarnedOnDate(datePrefix).executeAsList() }
}

suspend fun SharedDatabase.getGoalsExistingOnDate(dateStr: String): List<GoalEntity> {
    return this { db -> db.lifePlannerDBQueries.getGoalsExistingOnDate(dateStr).executeAsList() }
}

// ===== Beginner Objective Operations =====

fun SharedDatabase.observeAllBeginnerObjectives(): Flow<List<BeginnerObjectiveEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getAllBeginnerObjectives()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}

suspend fun SharedDatabase.getAllBeginnerObjectives(): List<BeginnerObjectiveEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllBeginnerObjectives().executeAsList() }
}

suspend fun SharedDatabase.getBeginnerObjectiveByType(objectiveType: String): BeginnerObjectiveEntity? {
    return this { db -> db.lifePlannerDBQueries.getBeginnerObjectiveByType(objectiveType).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getCompletedBeginnerObjectivesCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getCompletedBeginnerObjectivesCount().executeAsOne() }
}

suspend fun SharedDatabase.deduplicateBeginnerObjectives() {
    this { db ->
        db.lifePlannerDBQueries.deduplicateBeginnerObjectives()
    }
}

suspend fun SharedDatabase.insertBeginnerObjective(
    id: String,
    objectiveType: String,
    isCompleted: Long,
    completedAt: String?,
    xpAwarded: Long,
    createdAt: String
) {
    this { db ->
        db.lifePlannerDBQueries.insertBeginnerObjective(
            id, objectiveType, isCompleted, completedAt, xpAwarded, createdAt,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.completeBeginnerObjective(completedAt: String, xpAwarded: Long, objectiveType: String) {
    this { db ->
        db.lifePlannerDBQueries.completeBeginnerObjective(completedAt, xpAwarded, objectiveType)
    }
}

suspend fun SharedDatabase.uncompleteBeginnerObjective(objectiveType: String) {
    this { db ->
        db.lifePlannerDBQueries.uncompleteBeginnerObjective(objectiveType)
    }
}

suspend fun SharedDatabase.getDatesWithActivity(
    checkInStart: String, checkInEnd: String,
    journalStart: String, journalEnd: String,
    focusStart: String, focusEnd: String,
    historyStart: String, historyEnd: String,
    badgeStart: String, badgeEnd: String
): List<String> {
    return this { db ->
        db.lifePlannerDBQueries.getDatesWithActivity(
            checkInStart, checkInEnd,
            journalStart, journalEnd,
            focusStart, focusEnd,
            historyStart, historyEnd,
            badgeStart, badgeEnd
        ).executeAsList().mapNotNull { it }
    }
}
