package az.tribe.lifeplanner.infrastructure

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards logout against leaking one account's data into the next.
 *
 * `clearAllLocalData()` is a hand-written list of `deleteAll*` calls, so a new synced table is only
 * cleared if somebody remembers to add a line. Nobody did for `knowledge_reads`, which shipped in
 * v3 leaving the previous account's read lessons in place after a switch. This reads the source
 * instead of the database: the failure is an omission in that list, and the omission is what to
 * assert on.
 */
class ClearAllLocalDataTest {

    private val schema = readFile(
        "src/commonMain/sqldelight/az/tribe/lifeplanner/database/LifePlannerDB.sq"
    )
    private val source = readFile(
        "src/commonMain/kotlin/az/tribe/lifeplanner/infrastructure/SharedDatabase.kt"
    )

    /**
     * Server-authored content, identical for every user. Nothing to leak, and keeping it means the
     * Learn map is populated before the next fetch returns.
     */
    private val deliberatelyKept = setOf(
        "deleteAllKnowledgeLessons",
        "deleteAllKnowledgeCollections",
    )

    @Test
    fun `every bulk delete query is called on logout`() {
        val bulkDeletes = Regex("""^(deleteAll\w+):\s*\nDELETE FROM (\w+);""", RegexOption.MULTILINE)
            .findAll(schema)
            .map { it.groupValues[1] }
            .toSet() - deliberatelyKept

        assertTrue(bulkDeletes.isNotEmpty(), "found no bulk deletes; the schema path is probably wrong")

        val called = Regex("""q\.(deleteAll\w+)\(\)""").findAll(source).map { it.groupValues[1] }.toSet()
        val missing = (bulkDeletes - called).sorted()

        assertTrue(
            missing.isEmpty(),
            "clearAllLocalData() never calls: ${missing.joinToString()}. " +
                "A synced table left out of it survives logout into the next account. " +
                "Add the call, or add it to deliberatelyKept with a reason if it is not user data.",
        )
    }

    @Test
    fun `the tables this regressed on stay covered`() {
        listOf("deleteAllKnowledgeReads", "deleteAllWheelScores").forEach {
            assertTrue(source.contains("q.$it()"), "logout no longer clears $it")
        }
    }

    /** Host tests run with the module directory as the working directory. */
    private fun readFile(relativePath: String): String {
        val file = java.io.File(relativePath)
        assertTrue(file.exists(), "could not find $relativePath from ${file.absolutePath}")
        return file.readText()
    }
}
