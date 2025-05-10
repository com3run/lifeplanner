@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import az.tribe.lifeplanner.database.LifePlannerDB
import org.koin.mp.KoinPlatform


actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            LifePlannerDB.Schema.synchronous(),
            KoinPlatform.getKoin().get(),
            DB_NAME
        )
    }
}