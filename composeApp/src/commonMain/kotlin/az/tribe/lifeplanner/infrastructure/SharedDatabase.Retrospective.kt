package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.DayRecapEntity

suspend fun SharedDatabase.getDayRecap(date: String): DayRecapEntity? {
    return this { db -> db.lifePlannerDBQueries.getDayRecap(date).executeAsOneOrNull() }
}

suspend fun SharedDatabase.insertOrReplaceDayRecap(date: String, recap: String, generatedAt: String) {
    this { db -> db.lifePlannerDBQueries.insertOrReplaceDayRecap(date, recap, generatedAt) }
}
