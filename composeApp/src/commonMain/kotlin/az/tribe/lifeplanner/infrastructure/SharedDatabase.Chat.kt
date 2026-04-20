package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.ChatMessageEntity
import az.tribe.lifeplanner.database.ChatSessionEntity
import az.tribe.lifeplanner.database.ReviewReportEntity

// --- Chat Session operations ---

suspend fun SharedDatabase.getAllChatSessions(): List<ChatSessionEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllChatSessions().executeAsList() }
}

suspend fun SharedDatabase.getChatSessionById(id: String): ChatSessionEntity? {
    return this { db -> db.lifePlannerDBQueries.getChatSessionById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertChatSession(
    id: String,
    title: String,
    createdAt: String,
    lastMessageAt: String,
    summary: String?,
    coachId: String = "luna_general"
) {
    this { db ->
        db.lifePlannerDBQueries.insertChatSession(
            id, title, createdAt, lastMessageAt, summary, coachId,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.getChatSessionByCoachId(coachId: String): ChatSessionEntity? {
    return this { db -> db.lifePlannerDBQueries.getChatSessionByCoachId(coachId).executeAsOneOrNull() }
}

suspend fun SharedDatabase.updateChatSessionLastMessage(id: String, lastMessageAt: String, title: String) {
    this { db ->
        db.lifePlannerDBQueries.updateChatSessionLastMessage(lastMessageAt, title, id)
    }
}

suspend fun SharedDatabase.updateChatSessionSummary(id: String, summary: String) {
    this { db ->
        db.lifePlannerDBQueries.updateChatSessionSummary(summary, id)
    }
}

suspend fun SharedDatabase.deleteChatSession(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteChatSession(nowTimestamp(), id) }
}

suspend fun SharedDatabase.deleteOldChatSessions(beforeDate: String) {
    // Soft-delete old sessions and their messages
    this { db ->
        val oldSessions = db.lifePlannerDBQueries.getAllChatSessions().executeAsList()
            .filter { it.lastMessageAt < beforeDate }
        val now = nowTimestamp()
        oldSessions.forEach { session ->
            val messages = db.lifePlannerDBQueries.getMessagesBySessionId(session.id).executeAsList()
            messages.forEach { msg -> db.lifePlannerDBQueries.softDeleteChatMessage(now, msg.id) }
            db.lifePlannerDBQueries.softDeleteChatSession(now, session.id)
        }
    }
}

suspend fun SharedDatabase.getChatSessionCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getChatSessionCount().executeAsOne() }
}

// --- Chat Message operations ---

suspend fun SharedDatabase.getMessagesBySessionId(sessionId: String): List<ChatMessageEntity> {
    return this { db -> db.lifePlannerDBQueries.getMessagesBySessionId(sessionId).executeAsList() }
}

suspend fun SharedDatabase.getRecentMessages(sessionId: String, limit: Long): List<ChatMessageEntity> {
    return this { db -> db.lifePlannerDBQueries.getRecentMessages(sessionId, limit).executeAsList() }
}

suspend fun SharedDatabase.getMessageById(id: String): ChatMessageEntity? {
    return this { db -> db.lifePlannerDBQueries.getMessageById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertChatMessage(
    id: String,
    sessionId: String,
    content: String,
    role: String,
    timestamp: String,
    relatedGoalId: String?,
    metadata: String?
) {
    this { db ->
        db.lifePlannerDBQueries.insertChatMessage(
            id, sessionId, content, role, timestamp, relatedGoalId, metadata,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.deleteMessage(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteChatMessage(nowTimestamp(), id) }
}

suspend fun SharedDatabase.deleteMessagesBySession(sessionId: String) {
    this { db ->
        val messages = db.lifePlannerDBQueries.getMessagesBySessionId(sessionId).executeAsList()
        val now = nowTimestamp()
        messages.forEach { msg -> db.lifePlannerDBQueries.softDeleteChatMessage(now, msg.id) }
    }
}

suspend fun SharedDatabase.getMessageCountBySession(sessionId: String): Long {
    return this { db -> db.lifePlannerDBQueries.getMessageCountBySession(sessionId).executeAsOne() }
}

suspend fun SharedDatabase.getLastMessageBySession(sessionId: String): ChatMessageEntity? {
    return this { db -> db.lifePlannerDBQueries.getLastMessageBySession(sessionId).executeAsOneOrNull() }
}

suspend fun SharedDatabase.updateChatMessageMetadata(id: String, metadata: String?) {
    this { db -> db.lifePlannerDBQueries.updateChatMessageMetadata(metadata, id) }
}

// --- Review operations ---

suspend fun SharedDatabase.getAllReviews(): List<ReviewReportEntity> {
    return this { db -> db.lifePlannerDBQueries.getAllReviews().executeAsList() }
}

suspend fun SharedDatabase.getReviewById(id: String): ReviewReportEntity? {
    return this { db -> db.lifePlannerDBQueries.getReviewById(id).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getReviewsByType(type: String): List<ReviewReportEntity> {
    return this { db -> db.lifePlannerDBQueries.getReviewsByType(type).executeAsList() }
}

suspend fun SharedDatabase.getLatestReviewByType(type: String): ReviewReportEntity? {
    return this { db -> db.lifePlannerDBQueries.getLatestReviewByType(type).executeAsOneOrNull() }
}

suspend fun SharedDatabase.getUnreadReviews(): List<ReviewReportEntity> {
    return this { db -> db.lifePlannerDBQueries.getUnreadReviews().executeAsList() }
}

suspend fun SharedDatabase.insertReview(
    id: String,
    type: String,
    periodStart: String,
    periodEnd: String,
    generatedAt: String,
    summary: String,
    highlightsJson: String,
    insightsJson: String,
    recommendationsJson: String,
    statsJson: String,
    feedbackRating: String?,
    feedbackComment: String?,
    feedbackAt: String?,
    isRead: Long
) {
    this { db ->
        db.lifePlannerDBQueries.insertReview(
            id, type, periodStart, periodEnd, generatedAt, summary,
            highlightsJson, insightsJson, recommendationsJson, statsJson,
            feedbackRating, feedbackComment, feedbackAt, isRead,
            nowTimestamp(), 0L, 0L, null
        )
    }
}

suspend fun SharedDatabase.markReviewAsRead(id: String) {
    this { db -> db.lifePlannerDBQueries.markReviewAsRead(id) }
}

suspend fun SharedDatabase.updateReviewFeedback(id: String, rating: String, comment: String?, feedbackAt: String) {
    this { db -> db.lifePlannerDBQueries.updateReviewFeedback(rating, comment, feedbackAt, id) }
}

suspend fun SharedDatabase.deleteReview(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteReviewReport(nowTimestamp(), id) }
}

suspend fun SharedDatabase.getUnreadReviewCount(): Long {
    return this { db -> db.lifePlannerDBQueries.getUnreadReviewCount().executeAsOne() }
}
