package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.KnowledgeReadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- KnowledgeReadEntity accessors (Learn hub read-state) ---

suspend fun SharedDatabase.getAllKnowledgeReads(): List<KnowledgeReadEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllKnowledgeReads().executeAsList() }

suspend fun SharedDatabase.markKnowledgeReadLocal(id: String) {
    this { db -> db.lifePlannerDBQueries.markKnowledgeRead(id, nowTimestamp()) }
}

suspend fun SharedDatabase.softDeleteKnowledgeReadLocal(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteKnowledgeRead(nowTimestamp(), id) }
}

// --- Reactive Flow observer ---
fun SharedDatabase.observeKnowledgeReads(): Flow<List<KnowledgeReadEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllKnowledgeReads()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
