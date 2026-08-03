package az.tribe.lifeplanner.di

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers the Android migration chain, which is the only thing that brings an existing install up
 * to the current schema (the `.sqm` files drive iOS and compile-time verification, not the running
 * Android app). Nothing else catches a broken step: the build stays green and the app crashes with
 * `no such column` on the user's device instead.
 *
 * [addColumnSafe] swallows every exception, so a step can fail completely in silence. These tests
 * assert on the resulting schema rather than on the absence of a thrown exception.
 *
 * v2.3 shipped at schema v21, so an upgrader arrives at v39 having run the whole chain.
 */
@RunWith(RobolectricTestRunner::class)
// The library targets SDK 37, past what Robolectric 4.16 emulates, so pin it as
// PreviewScreenshots does. SQLite behaviour is not what varies across these levels.
@Config(sdk = [35])
class DatabaseMigrationsTest {

    @Test
    fun `a v21-era HabitEntity gains every column the current app reads`() {
        val db = legacyDatabase()

        runAndroidMigrations(db)

        // type/unit arrive at v22, health linkage at v35, completionSource at v38. A habit list
        // query selects all of them, so a silent miss here is a crash on the Habits tab.
        listOf("type", "unit", "healthMetricType", "healthTarget", "completionSource").forEach {
            assertTrue(columnExists(db, "HabitEntity", it), "HabitEntity is missing $it")
        }
    }

    @Test
    fun `DecisionEntity is created before the migration that alters it`() {
        val db = legacyDatabase()

        runAndroidMigrations(db)

        // The table itself only arrives at v31 and gains source/status at v37. Run the steps out
        // of order and addColumnSafe would no-op against a table that does not exist yet, quietly
        // leaving the columns off.
        assertTrue(tableExists(db, "DecisionEntity"), "DecisionEntity was never created")
        assertTrue(columnExists(db, "DecisionEntity", "source"), "DecisionEntity is missing source")
        assertTrue(columnExists(db, "DecisionEntity", "status"), "DecisionEntity is missing status")
    }

    @Test
    fun `the Learn hub tables arrive for upgraders`() {
        val db = legacyDatabase()

        runAndroidMigrations(db)

        listOf("KnowledgeReadEntity", "KnowledgeLessonEntity", "KnowledgeCollectionEntity")
            .forEach { assertTrue(tableExists(db, it), "$it was never created") }
    }

    @Test
    fun `running the chain twice changes nothing`() {
        val db = legacyDatabase()

        runAndroidMigrations(db)
        val afterFirst = schemaSnapshot(db)
        runAndroidMigrations(db)

        // The chain runs on every database open, not once per upgrade, so a step that is not
        // idempotent would corrupt or throw on the second launch rather than the first.
        assertEquals(afterFirst, schemaSnapshot(db))
    }

    @Test
    fun `migrating preserves rows that were already there`() {
        val db = legacyDatabase()
        db.execSQL(
            "INSERT INTO HabitEntity (id, title, category, createdAt) VALUES " +
                "('h1', 'Morning walk', 'BODY', '2026-01-01T08:00')"
        )

        runAndroidMigrations(db)

        db.query("SELECT title, completionSource FROM HabitEntity WHERE id = 'h1'").use {
            assertTrue(it.moveToFirst(), "the pre-existing habit did not survive the migration")
            assertEquals("Morning walk", it.getString(0))
            assertTrue(it.isNull(1), "completionSource should default to null for old rows")
        }
    }

    // -- fixture ---------------------------------------------------------------------------

    /**
     * A database shaped like a v21 install: the tables 2.3 shipped, without any column or table
     * added since. Deliberately hand-written rather than generated from the current schema, so it
     * cannot silently acquire the very columns the migrations are supposed to add.
     */
    private fun legacyDatabase(): SupportSQLiteDatabase {
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                // valueId (v30) and predictedDueDate (v32) are deliberately absent.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS GoalEntity (
                        id TEXT NOT NULL PRIMARY KEY,
                        category TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        status TEXT NOT NULL,
                        timeline TEXT NOT NULL,
                        dueDate TEXT NOT NULL,
                        progress INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt TEXT NOT NULL DEFAULT '2025-01-01T00:00:00',
                        completionRate REAL NOT NULL DEFAULT 0.0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        aiReasoning TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS HabitEntity (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        category TEXT NOT NULL,
                        frequency TEXT NOT NULL DEFAULT 'DAILY',
                        targetCount INTEGER NOT NULL DEFAULT 1,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        longestStreak INTEGER NOT NULL DEFAULT 0,
                        totalCompletions INTEGER NOT NULL DEFAULT 0,
                        lastCompletedDate TEXT,
                        linkedGoalId TEXT,
                        correlationScore REAL NOT NULL DEFAULT 0.0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt TEXT NOT NULL,
                        reminderTime TEXT
                    )
                    """.trimIndent()
                )
                // `count` arrives at v24; the rest is the shape the migrations index against.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS HabitCheckInEntity (
                        id TEXT PRIMARY KEY NOT NULL,
                        habitId TEXT NOT NULL,
                        date TEXT NOT NULL,
                        completed INTEGER NOT NULL DEFAULT 1,
                        notes TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS JournalEntryEntity (
                        id TEXT PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        mood TEXT NOT NULL DEFAULT 'NEUTRAL',
                        linkedGoalId TEXT,
                        linkedHabitId TEXT,
                        promptUsed TEXT,
                        tags TEXT NOT NULL DEFAULT '',
                        date TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS UserProgressEntity (
                        id INTEGER PRIMARY KEY DEFAULT 1,
                        currentStreak INTEGER NOT NULL DEFAULT 0,
                        lastCheckInDate TEXT,
                        totalXp INTEGER NOT NULL DEFAULT 0,
                        currentLevel INTEGER NOT NULL DEFAULT 1,
                        goalsCompleted INTEGER NOT NULL DEFAULT 0,
                        habitsCompleted INTEGER NOT NULL DEFAULT 0,
                        journalEntriesCount INTEGER NOT NULL DEFAULT 0,
                        longestStreak INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }

        val configuration = SupportSQLiteOpenHelper.Configuration
            .builder(RuntimeEnvironment.getApplication())
            .name(null) // in-memory, so each test starts clean
            .callback(callback)
            .build()

        return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
    }

    private fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean =
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$table'")
            .use { it.moveToFirst() }

    /** Every table and column, so an idempotency break shows up as a diff rather than a guess. */
    private fun schemaSnapshot(db: SupportSQLiteDatabase): List<String> {
        val tables = buildList {
            db.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' " +
                    "AND name != 'android_metadata' ORDER BY name"
            ).use { while (it.moveToNext()) add(it.getString(0)) }
        }
        return tables.flatMap { table ->
            buildList {
                db.query("PRAGMA table_info($table)").use { cursor ->
                    val nameIndex = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) add("$table.${cursor.getString(nameIndex)}")
                }
            }.sorted()
        }
    }
}
