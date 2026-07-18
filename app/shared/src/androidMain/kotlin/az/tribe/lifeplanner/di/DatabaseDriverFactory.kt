@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package az.tribe.lifeplanner.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import az.tribe.lifeplanner.database.LifePlannerDB
import co.touchlab.kermit.Logger
import org.koin.mp.KoinPlatform

private const val PREFS_NAME = "lifeplanner_db_prefs"
private const val KEY_DB_SCHEMA_VERSION = "db_schema_version"
private const val CURRENT_SCHEMA_VERSION = 3 // Increment this when you want to force a fresh DB for all users

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        val context: Context = KoinPlatform.getKoin().get()

        // Check if we need to reset the database for users upgrading from v1
        resetDatabaseIfNeeded(context)

        return AndroidSqliteDriver(
            schema = LifePlannerDB.Schema.synchronous(),
            context = KoinPlatform.getKoin().get(),
            name = DB_NAME,
            callback = object : AndroidSqliteDriver.Callback(LifePlannerDB.Schema.synchronous()) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Run migrations on every open to ensure schema is up to date
                    migrateToVersion5(db)
                    migrateToVersion6(db)
                    migrateToVersion7(db)
                    migrateToVersion8(db)
                    migrateToVersion9(db)
                    migrateToVersion10(db)
                    migrateToVersion11(db)
                    migrateToVersion12(db)
                    migrateToSyncColumns(db)
                    migrateToVersion13(db)
                    migrateToVersion14(db)
                    migrateToVersion15(db)
                    migrateToVersion16(db)
                    migrateToVersion17(db)
                    migrateToVersion18(db)
                    migrateToVersion19(db)
                    migrateToVersion20(db)
                    migrateToVersion21(db)
                    migrateToVersion22(db)
                    migrateToVersion23(db)
                    migrateToVersion24(db)
                    migrateToVersion25(db)
                    migrateToVersion26(db)
                    migrateHealthMetricEntity(db)
                    migrateToVersion29(db)
                    migrateToVersion30(db)
                    migrateToVersion31(db)
                    migrateToVersion32(db)
                    migrateToVersion33(db)
                    migrateToVersion34(db)
                    migrateToVersion35(db)
                }

                override fun onDowngrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // A device DB newer than this build's schema (e.g. after testing a
                    // higher-schema branch) can't be downgraded by SQLite and would otherwise
                    // crash on open. Recreate the local DB cleanly instead; synced data
                    // restores from the cloud on next sync.
                    Logger.w("DatabaseDriverFactory") {
                        "Local DB v$oldVersion newer than app schema v$newVersion, recreating local database"
                    }
                    val tables = buildList {
                        db.query(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'android_metadata'"
                        ).use { c -> while (c.moveToNext()) add(c.getString(0)) }
                    }
                    tables.forEach { db.execSQL("DROP TABLE IF EXISTS `$it`") }
                    onCreate(db) // re-create the current schema from scratch
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    // Migration from version 3 to 4: Add UserEntity table
                    if (oldVersion < 4) {
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS UserEntity (
                                id TEXT PRIMARY KEY NOT NULL,
                                firebaseUid TEXT UNIQUE,
                                email TEXT,
                                displayName TEXT,
                                isGuest INTEGER NOT NULL DEFAULT 0,
                                selectedSymbol TEXT,
                                priorities TEXT,
                                ageRange TEXT,
                                profession TEXT,
                                relationshipStatus TEXT,
                                mindset TEXT,
                                hasCompletedOnboarding INTEGER NOT NULL DEFAULT 0,
                                createdAt TEXT NOT NULL,
                                lastSyncedAt TEXT
                            )
                            """.trimIndent()
                        )

                        db.execSQL(
                            "CREATE INDEX IF NOT EXISTS idx_user_firebase_uid ON UserEntity(firebaseUid)"
                        )
                    }

                    // Migration from version 4 to 5: Add Gamification tables and columns
                    if (oldVersion < 5) {
                        // Add new columns to UserProgressEntity
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN totalXp INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN currentLevel INTEGER NOT NULL DEFAULT 1")
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN goalsCompleted INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN habitsCompleted INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN journalEntriesCount INTEGER NOT NULL DEFAULT 0")
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN longestStreak INTEGER NOT NULL DEFAULT 0")

                        // Create BadgeEntity table
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS BadgeEntity (
                                id TEXT PRIMARY KEY NOT NULL,
                                badgeType TEXT NOT NULL,
                                earnedAt TEXT NOT NULL,
                                isNew INTEGER NOT NULL DEFAULT 1
                            )
                            """.trimIndent()
                        )

                        // Create ChallengeEntity table
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS ChallengeEntity (
                                id TEXT PRIMARY KEY NOT NULL,
                                challengeType TEXT NOT NULL,
                                startDate TEXT NOT NULL,
                                endDate TEXT NOT NULL,
                                currentProgress INTEGER NOT NULL DEFAULT 0,
                                targetProgress INTEGER NOT NULL,
                                isCompleted INTEGER NOT NULL DEFAULT 0,
                                completedAt TEXT,
                                xpEarned INTEGER NOT NULL DEFAULT 0
                            )
                            """.trimIndent()
                        )

                        // Create indexes for gamification
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_badge_type ON BadgeEntity(badgeType)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_badge_new ON BadgeEntity(isNew)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_type ON ChallengeEntity(challengeType)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_completed ON ChallengeEntity(isCompleted)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_challenge_end_date ON ChallengeEntity(endDate)")
                    }
                }
            }
        )
    }

    /**
     * Reset database for users upgrading from older versions (v1).
     * This ensures a clean slate without migration errors.
     */
    private fun resetDatabaseIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedVersion = prefs.getInt(KEY_DB_SCHEMA_VERSION, 0)

        if (storedVersion < CURRENT_SCHEMA_VERSION) {
            Logger.i("DatabaseDriverFactory") { "Upgrading from schema version $storedVersion to $CURRENT_SCHEMA_VERSION" }

            // Delete the old database file
            val dbFile = context.getDatabasePath(DB_NAME)
            if (dbFile.exists()) {
                Logger.i("DatabaseDriverFactory") { "Deleting old database for clean upgrade" }
                context.deleteDatabase(DB_NAME)
            }

            // Also delete any journal/wal files
            val dbDir = dbFile.parentFile
            dbDir?.listFiles()?.forEach { file ->
                if (file.name.startsWith(DB_NAME)) {
                    file.delete()
                }
            }

            // Update stored version
            prefs.edit().putInt(KEY_DB_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION).apply()
            Logger.i("DatabaseDriverFactory") { "Database reset complete, now at schema version $CURRENT_SCHEMA_VERSION" }
        }
    }
}
