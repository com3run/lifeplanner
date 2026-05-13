@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.di

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import az.tribe.lifeplanner.database.LifePlannerDB
import java.io.File

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".lifeplanner")
        dbDir.mkdirs()
        val dbFile = File(dbDir, DB_NAME)

        // Open driver first to check existing version
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        val schema = LifePlannerDB.Schema.synchronous()

        val versionMapper: (app.cash.sqldelight.db.SqlCursor) -> app.cash.sqldelight.db.QueryResult.Value<Long> = {
            app.cash.sqldelight.db.QueryResult.Value(it.getLong(0) ?: 0L)
        }
        val currentVersion = driver.executeQuery(null, "PRAGMA user_version;", versionMapper, 0, null).value

        when {
            currentVersion == 0L -> {
                // New DB or old unversioned DB — recreate clean
                driver.close()
                dbFile.delete()
                val freshDriver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
                schema.create(freshDriver)
                freshDriver.execute(null, "PRAGMA user_version = ${schema.version};", 0, null)
                return freshDriver
            }
            currentVersion < schema.version -> {
                schema.migrate(driver, currentVersion, schema.version)
                driver.execute(null, "PRAGMA user_version = ${schema.version};", 0, null)
            }
        }
        return driver
    }
}
