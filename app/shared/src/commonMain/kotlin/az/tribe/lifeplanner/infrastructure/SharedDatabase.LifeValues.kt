package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.LifeValueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- LifeValueEntity accessors ---

suspend fun SharedDatabase.getAllLifeValues(): List<LifeValueEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllLifeValues().executeAsList() }

suspend fun SharedDatabase.getActiveLifeValues(): List<LifeValueEntity> =
    this { db -> db.lifePlannerDBQueries.selectActiveLifeValues().executeAsList() }

suspend fun SharedDatabase.getLifeValueById(id: String): LifeValueEntity? =
    this { db -> db.lifePlannerDBQueries.selectLifeValueById(id).executeAsOneOrNull() }

suspend fun SharedDatabase.insertLifeValue(value: LifeValueEntity) {
    this { db ->
        db.lifePlannerDBQueries.insertLifeValue(
            id = value.id,
            title = value.title,
            description = value.description,
            isActive = value.isActive,
            sortOrder = value.sortOrder,
            sync_updated_at = nowTimestamp(),
            is_deleted = 0L,
            sync_version = 0L,
            last_synced_at = null
        )
    }
}

suspend fun SharedDatabase.insertLifeValues(values: List<LifeValueEntity>) {
    this { db ->
        values.forEach { value ->
            db.lifePlannerDBQueries.insertLifeValue(
                id = value.id,
                title = value.title,
                description = value.description,
                isActive = value.isActive,
                sortOrder = value.sortOrder,
                sync_updated_at = nowTimestamp(),
                is_deleted = 0L,
                sync_version = 0L,
                last_synced_at = null
            )
        }
    }
}

suspend fun SharedDatabase.updateLifeValue(value: LifeValueEntity) {
    this { db ->
        db.lifePlannerDBQueries.updateLifeValue(
            title = value.title,
            description = value.description,
            isActive = value.isActive,
            sortOrder = value.sortOrder,
            id = value.id
        )
    }
}

suspend fun SharedDatabase.deleteLifeValueById(id: String) {
    this { db -> db.lifePlannerDBQueries.softDeleteLifeValue(nowTimestamp(), id) }
}

// --- Reactive Flow observer ---

fun SharedDatabase.observeAllLifeValues(): Flow<List<LifeValueEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllLifeValues()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
