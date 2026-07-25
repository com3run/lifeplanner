package az.tribe.lifeplanner.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Tracks which Learn lessons the user has read. Backed by a local table and synced across devices,
 * so progress in the Learn library follows the user from phone to tablet to the other platform.
 */
interface KnowledgeRepository {

    /** Ids of every lesson the user has marked read, emitted again whenever the set changes. */
    fun readIds(): Flow<Set<String>>

    /** Mark a lesson read. Idempotent, safe to call every time a lesson opens. */
    suspend fun markRead(id: String)

    /** Forget a lesson's read state, for a "mark unread" affordance. */
    suspend fun markUnread(id: String)
}
