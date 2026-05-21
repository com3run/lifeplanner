package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.AbilityEntity
import az.tribe.lifeplanner.database.AbilityGoalLinkEntity
import az.tribe.lifeplanner.database.AbilityHabitLinkEntity
import az.tribe.lifeplanner.database.HealthMetricEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- Health Metric operations ---

fun SharedDatabase.observeHealthMetricsByType(metricType: String): Flow<List<HealthMetricEntity>> = flow {
    emitAll(
        this@observeHealthMetricsByType { db ->
            db.lifePlannerDBQueries.observeHealthMetricsByType(metricType)
        }.asFlow().mapToList(Dispatchers.IO)
    )
}

suspend fun SharedDatabase.getHealthMetricsByTypeAndDateRange(
    metricType: String,
    startDate: String,
    endDate: String
): List<HealthMetricEntity> {
    return this { db ->
        db.lifePlannerDBQueries.getHealthMetricsByTypeAndDateRange(metricType, startDate, endDate)
            .executeAsList()
    }
}

suspend fun SharedDatabase.getLatestHealthMetric(metricType: String): HealthMetricEntity? {
    return this { db ->
        db.lifePlannerDBQueries.getLatestHealthMetric(metricType).executeAsOneOrNull()
    }
}

suspend fun SharedDatabase.insertHealthMetric(
    id: String,
    metricType: String,
    value: Double,
    unit: String,
    date: String,
    source: String,
    recordedAt: String,
    createdAt: String
) {
    this { db ->
        db.lifePlannerDBQueries.insertHealthMetric(
            id = id,
            metricType = metricType,
            value_ = value,
            unit = unit,
            date = date,
            source = source,
            recordedAt = recordedAt,
            createdAt = createdAt
        )
    }
}

// --- Ability operations ---

fun SharedDatabase.observeAllAbilities(): Flow<List<AbilityEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.getAllAbilities()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}

suspend fun SharedDatabase.getAbilityById(id: String): AbilityEntity? {
    return this { db -> db.lifePlannerDBQueries.getAbilityById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertAbility(ability: AbilityEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertAbility(
            id = ability.id,
            title = ability.title,
            description = ability.description,
            iconEmoji = ability.iconEmoji,
            totalXp = ability.totalXp,
            currentLevel = ability.currentLevel,
            isActive = ability.isActive,
            createdAt = ability.createdAt,
            lastActivityDate = ability.lastActivityDate,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateAbility(id: String, title: String, description: String, iconEmoji: String) {
    this { db ->
        db.lifePlannerDBQueries.updateAbility(
            title = title,
            description = description,
            iconEmoji = iconEmoji,
            id = id
        )
    }
}

suspend fun SharedDatabase.updateAbilityXpAndLevel(id: String, totalXp: Long, currentLevel: Long, lastActivityDate: String) {
    this { db ->
        db.lifePlannerDBQueries.updateAbilityXpAndLevel(
            totalXp = totalXp,
            currentLevel = currentLevel,
            lastActivityDate = lastActivityDate,
            id = id
        )
    }
}

suspend fun SharedDatabase.deleteAbility(id: String) {
    this { db -> db.lifePlannerDBQueries.deleteAbility(id) }
}

suspend fun SharedDatabase.insertAbilityHabitLink(link: AbilityHabitLinkEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertAbilityHabitLink(
            id = link.id,
            abilityId = link.abilityId,
            habitId = link.habitId,
            xpWeight = link.xpWeight,
            createdAt = link.createdAt
        )
    }
}

suspend fun SharedDatabase.getLinksForAbility(abilityId: String): List<AbilityHabitLinkEntity> {
    return this { db -> db.lifePlannerDBQueries.getLinksForAbility(abilityId).executeAsList() }
}

suspend fun SharedDatabase.getLinksForHabit(habitId: String): List<AbilityHabitLinkEntity> {
    return this { db -> db.lifePlannerDBQueries.getLinksForHabit(habitId).executeAsList() }
}

suspend fun SharedDatabase.deleteAbilityHabitLink(abilityId: String, habitId: String) {
    this { db -> db.lifePlannerDBQueries.deleteAbilityHabitLink(abilityId, habitId) }
}

// --- Ability-Goal link operations ---

suspend fun SharedDatabase.insertAbilityGoalLink(link: AbilityGoalLinkEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertAbilityGoalLink(
            id = link.id,
            abilityId = link.abilityId,
            goalId = link.goalId,
            createdAt = link.createdAt
        )
    }
}

suspend fun SharedDatabase.getGoalLinksForAbility(abilityId: String): List<AbilityGoalLinkEntity> {
    return this { db -> db.lifePlannerDBQueries.getGoalLinksForAbility(abilityId).executeAsList() }
}

suspend fun SharedDatabase.getAbilityLinksForGoal(goalId: String): List<AbilityGoalLinkEntity> {
    return this { db -> db.lifePlannerDBQueries.getAbilityLinksForGoal(goalId).executeAsList() }
}

suspend fun SharedDatabase.deleteAbilityGoalLink(abilityId: String, goalId: String) {
    this { db -> db.lifePlannerDBQueries.deleteAbilityGoalLink(abilityId, goalId) }
}
