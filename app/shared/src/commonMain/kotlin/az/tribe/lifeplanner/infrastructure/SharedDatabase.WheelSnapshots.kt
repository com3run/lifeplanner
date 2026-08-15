package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.WheelSnapshotEntity

// --- WheelSnapshotEntity accessors (Wheel of Life history, one row per day) ---

suspend fun SharedDatabase.getAllWheelSnapshots(): List<WheelSnapshotEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllWheelSnapshots().executeAsList() }

suspend fun SharedDatabase.getWheelSnapshot(date: String): WheelSnapshotEntity? =
    this { db -> db.lifePlannerDBQueries.selectWheelSnapshotById(date).executeAsOneOrNull() }

/** The nearest snapshot at or before [date], so a missed day still yields a comparison. */
suspend fun SharedDatabase.getWheelSnapshotOnOrBefore(date: String): WheelSnapshotEntity? =
    this { db -> db.lifePlannerDBQueries.selectWheelSnapshotOnOrBefore(date).executeAsOneOrNull() }

/** `id` is the ISO date. Re-writing the same day replaces it rather than adding a row. */
suspend fun SharedDatabase.putWheelSnapshotLocal(date: String, scoresJson: String) {
    this { db -> db.lifePlannerDBQueries.putWheelSnapshot(date, scoresJson, nowTimestamp()) }
}
