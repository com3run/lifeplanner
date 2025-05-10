package az.tribe.lifeplanner.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import az.tribe.lifeplanner.database.LifePlannerDB

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        return NativeSqliteDriver(LifePlannerDB.Schema.synchronous(), DB_NAME)
    }
}