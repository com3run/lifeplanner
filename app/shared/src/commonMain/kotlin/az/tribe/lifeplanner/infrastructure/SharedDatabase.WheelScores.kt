package az.tribe.lifeplanner.infrastructure

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import az.tribe.lifeplanner.database.WheelScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// --- WheelScoreEntity accessors (Wheel of Life, user-set scores only) ---

suspend fun SharedDatabase.getAllWheelScores(): List<WheelScoreEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllWheelScores().executeAsList() }

/** `id` is the [az.tribe.lifeplanner.domain.model.WheelArea] enum name. */
suspend fun SharedDatabase.setWheelScoreLocal(id: String, score: Double, note: String?) {
    this { db -> db.lifePlannerDBQueries.setWheelScore(id, score, nowTimestamp(), note) }
}

suspend fun SharedDatabase.clearWheelScoreLocal(id: String) {
    this { db -> db.lifePlannerDBQueries.clearWheelScore(nowTimestamp(), id) }
}

// --- Reactive Flow observer ---
fun SharedDatabase.observeWheelScores(): Flow<List<WheelScoreEntity>> = flow {
    initDatabase()
    emitAll(
        database!!.lifePlannerDBQueries.selectAllWheelScores()
            .asFlow()
            .mapToList(Dispatchers.IO)
    )
}
