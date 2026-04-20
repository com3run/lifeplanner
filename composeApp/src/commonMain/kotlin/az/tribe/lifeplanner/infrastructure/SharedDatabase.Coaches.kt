package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.CoachGroupEntity
import az.tribe.lifeplanner.database.CoachGroupMemberEntity
import az.tribe.lifeplanner.database.CoachPersonaOverrideEntity
import az.tribe.lifeplanner.database.CustomCoachEntity

// ===== Custom Coach Operations =====

suspend fun SharedDatabase.insertCustomCoach(
    id: String,
    name: String,
    icon: String,
    iconBackgroundColor: String,
    iconAccentColor: String,
    systemPrompt: String,
    characteristics: String,
    isFromTemplate: Long,
    templateId: String?,
    createdAt: String,
    updatedAt: String?
) {
    this { db ->
        db.lifePlannerDBQueries.insertCustomCoach(
            id, name, icon, iconBackgroundColor, iconAccentColor,
            systemPrompt, characteristics, isFromTemplate, templateId,
            createdAt, updatedAt,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.getAllCustomCoaches(): List<CustomCoachEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllCustomCoaches().executeAsList() }
}

suspend fun SharedDatabase.getCustomCoachById(id: String): CustomCoachEntity? {
    return this { db -> db.lifePlannerDBQueries.getCustomCoachById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.updateCustomCoach(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    iconAccentColor: String,
    systemPrompt: String,
    characteristics: String,
    updatedAt: String?,
    id: String
) {
    this { db ->
        db.lifePlannerDBQueries.updateCustomCoach(
            name, icon, iconBackgroundColor, iconAccentColor,
            systemPrompt, characteristics, updatedAt, id
        )
    }
}

suspend fun SharedDatabase.deleteCustomCoach(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteCustomCoach(nowTimestamp(), id) }
}

suspend fun SharedDatabase.getCustomCoachCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getCustomCoachCount().executeAsOne() }
}

// ===== Coach Group Operations =====

suspend fun SharedDatabase.insertCoachGroup(
    id: String,
    name: String,
    icon: String,
    description: String,
    createdAt: String,
    updatedAt: String?
) {
    this { db ->
        db.lifePlannerDBQueries.insertCoachGroup(id, name, icon, description, createdAt, updatedAt, null, 0L, 0L, null)
    }
}

suspend fun SharedDatabase.getAllCoachGroups(): List<CoachGroupEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllCoachGroups().executeAsList() }
}

suspend fun SharedDatabase.getCoachGroupById(id: String): CoachGroupEntity? {
    return this { db -> db.lifePlannerDBQueries.getCoachGroupById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.updateCoachGroup(
    name: String,
    icon: String,
    description: String,
    updatedAt: String?,
    id: String
) {
    this { db ->
        db.lifePlannerDBQueries.updateCoachGroup(name, icon, description, updatedAt, id)
    }
}

suspend fun SharedDatabase.deleteCoachGroup(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteCoachGroup(nowTimestamp(), id) }
}

suspend fun SharedDatabase.getCoachGroupCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getCoachGroupCount().executeAsOne() }
}

// ===== Coach Group Member Operations =====

suspend fun SharedDatabase.insertCoachGroupMember(
    id: String,
    groupId: String,
    coachType: String,
    coachId: String,
    displayOrder: Long
) {
    this { db ->
        db.lifePlannerDBQueries.insertCoachGroupMember(id, groupId, coachType, coachId, displayOrder, null, 0L, 0L, null)
    }
}

suspend fun SharedDatabase.getCoachGroupMembers(groupId: String): List<CoachGroupMemberEntity> {
    return this { db -> db.lifePlannerDBQueries.getCoachGroupMembers(groupId).executeAsList() }
}

// Batch fetch all coach group members in ONE query (eliminates N+1 when loading groups)
suspend fun SharedDatabase.getAllActiveCoachGroupMembers(): Map<String, List<CoachGroupMemberEntity>> {
    return this { db ->
        db.lifePlannerDBQueries.getAllActiveCoachGroupMembers().executeAsList()
            .groupBy { it.groupId }
    }
}

suspend fun SharedDatabase.deleteCoachGroupMember(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteCoachGroupMember(nowTimestamp(), id) }
}

suspend fun SharedDatabase.deleteCoachGroupMembersByGroup(groupId: String) {
    this { db ->
        val members = db.lifePlannerDBQueries.getCoachGroupMembers(groupId).executeAsList()
        val now = nowTimestamp()
        members.forEach { m -> db.lifePlannerDBQueries.softDeleteCoachGroupMember(now, m.id) }
    }
}

suspend fun SharedDatabase.updateCoachGroupMemberOrder(displayOrder: Long, id: String) {
    this { db -> db.lifePlannerDBQueries.updateCoachGroupMemberOrder(displayOrder, id) }
}

// ===== Coach Persona Override Operations =====

suspend fun SharedDatabase.getCoachPersonaOverride(coachId: String): CoachPersonaOverrideEntity? {
    return this { db -> db.lifePlannerDBQueries.getCoachPersonaOverride(coachId).executeAsOneOrNull() }
}

suspend fun SharedDatabase.upsertCoachPersonaOverride(coachId: String, userPersona: String, updatedAt: String) {
    this { db -> db.lifePlannerDBQueries.upsertCoachPersonaOverride(coachId, userPersona, updatedAt, nowTimestamp()) }
}

suspend fun SharedDatabase.deleteCoachPersonaOverride(coachId: String) {
    this { db -> db.lifePlannerDBQueries.deleteCoachPersonaOverride(coachId) }
}
