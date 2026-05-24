package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.IdentityStatementEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- IdentityStatementEntity accessors ---

suspend fun SharedDatabase.getAllIdentityStatements(): List<IdentityStatementEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllIdentityStatements().executeAsList() }

suspend fun SharedDatabase.getIdentityStatementById(id: String): IdentityStatementEntity? =
    this { db -> db.lifePlannerDBQueries.selectIdentityStatementById(id).executeAsOneOrNull() }

suspend fun SharedDatabase.getIdentityStatementsByValue(valueId: String): List<IdentityStatementEntity> =
    this { db -> db.lifePlannerDBQueries.selectIdentityStatementsByValue(valueId).executeAsList() }

suspend fun SharedDatabase.insertIdentityStatement(s: IdentityStatementEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertIdentityStatement(
            id = s.id,
            statement = s.statement,
            valueId = s.valueId,
            isActive = s.isActive,
            sortOrder = s.sortOrder,
            createdAt = s.createdAt,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.updateIdentityStatement(s: IdentityStatementEntity) {
    this { db ->
        db.lifePlannerDBQueries.updateIdentityStatement(
            statement = s.statement,
            valueId = s.valueId,
            isActive = s.isActive,
            sortOrder = s.sortOrder,
            createdAt = s.createdAt,
            id = s.id
        )
    }
}

suspend fun SharedDatabase.deleteIdentityStatementById(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteIdentityStatement(nowTimestamp(), id) }
}

fun SharedDatabase.observeAllIdentityStatements(): Flow<List<IdentityStatementEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllIdentityStatements()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
