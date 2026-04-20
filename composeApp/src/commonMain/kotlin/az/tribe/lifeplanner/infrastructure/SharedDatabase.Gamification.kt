package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.BadgeEntity
import az.tribe.lifeplanner.database.ChallengeEntity
import az.tribe.lifeplanner.database.UserProgressEntity

// --- Badge operations ---

suspend fun SharedDatabase.getAllBadges(): List<BadgeEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllBadges().executeAsList() }
}

suspend fun SharedDatabase.getBadgeByType(badgeType: String): BadgeEntity? {
    return this { db -> db.lifePlannerDBQueries.getBadgeByType(badgeType).executeAsOneOrNull() }
}

suspend fun SharedDatabase.hasBadge(badgeType: String): Boolean {
    return this { db -> db.lifePlannerDBQueries.hasBadge(badgeType).executeAsOne() > 0 }
}

suspend fun SharedDatabase.insertBadge(badge: BadgeEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertBadge(
            id = badge.id,
            badgeType = badge.badgeType,
            earnedAt = badge.earnedAt,
            isNew = badge.isNew,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.markBadgeAsSeen(id: String) {
    this { db -> db.lifePlannerDBQueries.markBadgeAsSeen(id) }
}

suspend fun SharedDatabase.markAllBadgesAsSeen() {
    this { db -> db.lifePlannerDBQueries.markAllBadgesAsSeen() }
}

suspend fun SharedDatabase.getNewBadges(): List<BadgeEntity> {
    return this { db -> db.lifePlannerDBQueries.getNewBadges().executeAsList() }
}

suspend fun SharedDatabase.getBadgeCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getBadgeCount().executeAsOne() }
}

// --- Challenge operations ---

suspend fun SharedDatabase.getAllChallenges(): List<ChallengeEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllChallenges().executeAsList() }
}

suspend fun SharedDatabase.getActiveChallenges(today: String): List<ChallengeEntity> {
    return this { db -> db.lifePlannerDBQueries.getActiveChallenges(today).executeAsList() }
}

suspend fun SharedDatabase.getCompletedChallenges(): List<ChallengeEntity> {
    return this { db -> db.lifePlannerDBQueries.getCompletedChallenges().executeAsList() }
}

suspend fun SharedDatabase.getChallengeById(id: String): ChallengeEntity? {
    return this { db -> db.lifePlannerDBQueries.getChallengeById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getChallengeByType(challengeType: String): ChallengeEntity? {
    return this { db ->
        db.lifePlannerDBQueries.getChallengeByType(challengeType).executeAsOneOrNull()
    }
}

suspend fun SharedDatabase.insertChallenge(challenge: ChallengeEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertChallenge(
            id = challenge.id,
            challengeType = challenge.challengeType,
            startDate = challenge.startDate,
            endDate = challenge.endDate,
            currentProgress = challenge.currentProgress,
            targetProgress = challenge.targetProgress,
            isCompleted = challenge.isCompleted,
            completedAt = challenge.completedAt,
            xpEarned = challenge.xpEarned,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateChallengeProgress(id: String, progress: Long) {
    this { db -> db.lifePlannerDBQueries.updateChallengeProgress(progress, id) }
}

suspend fun SharedDatabase.completeChallenge(id: String, completedAt: String, xpEarned: Long) {
    this { db -> db.lifePlannerDBQueries.completeChallenge(completedAt, xpEarned, id) }
}

suspend fun SharedDatabase.deleteChallenge(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteChallenge(nowTimestamp(), id) }
}

suspend fun SharedDatabase.deleteExpiredChallenges(today: String) {
    // Expired challenges are soft-deleted; they can still sync
    this { db -> db.lifePlannerDBQueries.deleteExpiredChallenges(today) }
}

// --- Extended User Progress operations ---

suspend fun SharedDatabase.getUserProgressEntity(): UserProgressEntity? {
    return this { db -> db.lifePlannerDBQueries.getUserProgress().executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertUserProgressFull(
    currentStreak: Long,
    lastCheckInDate: String?,
    totalXp: Long,
    currentLevel: Long,
    goalsCompleted: Long,
    habitsCompleted: Long,
    journalEntriesCount: Long,
    longestStreak: Long
) {
    this { db ->
        db.lifePlannerDBQueries.insertUserProgress(
            currentStreak = currentStreak,
            lastCheckInDate = lastCheckInDate,
            totalXp = totalXp,
            currentLevel = currentLevel,
            goalsCompleted = goalsCompleted,
            habitsCompleted = habitsCompleted,
            journalEntriesCount = journalEntriesCount,
            longestStreak = longestStreak,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateUserStreakFull(currentStreak: Long, lastCheckInDate: String, longestStreak: Long) {
    this { db ->
        db.lifePlannerDBQueries.updateUserStreak(currentStreak, lastCheckInDate, longestStreak)
    }
}

suspend fun SharedDatabase.updateUserXp(totalXp: Long, currentLevel: Long) {
    this { db -> db.lifePlannerDBQueries.updateUserXp(totalXp, currentLevel) }
}

suspend fun SharedDatabase.addXp(xpAmount: Long) {
    this { db -> db.lifePlannerDBQueries.addXp(xpAmount) }
}

suspend fun SharedDatabase.incrementGoalsCompleted() {
    this { db -> db.lifePlannerDBQueries.incrementGoalsCompleted() }
}

suspend fun SharedDatabase.incrementHabitsCompleted() {
    this { db -> db.lifePlannerDBQueries.incrementHabitsCompleted() }
}

suspend fun SharedDatabase.decrementHabitsCompleted() {
    this { db -> db.lifePlannerDBQueries.decrementHabitsCompleted() }
}

suspend fun SharedDatabase.incrementJournalEntries() {
    this { db -> db.lifePlannerDBQueries.incrementJournalEntries() }
}
