package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.data.sync.SyncManager
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.markKnowledgeReadLocal
import az.tribe.lifeplanner.infrastructure.observeKnowledgeReads
import az.tribe.lifeplanner.infrastructure.softDeleteKnowledgeReadLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightKnowledgeRepository(
    private val db: SharedDatabase,
    private val syncManager: SyncManager,
) : KnowledgeRepository {

    override fun readIds(): Flow<Set<String>> =
        db.observeKnowledgeReads().map { rows -> rows.map { it.id }.toSet() }

    override suspend fun markRead(id: String) {
        db.markKnowledgeReadLocal(id)
        syncManager.requestSync()
    }

    override suspend fun markUnread(id: String) {
        db.softDeleteKnowledgeReadLocal(id)
        syncManager.requestSync()
    }
}
