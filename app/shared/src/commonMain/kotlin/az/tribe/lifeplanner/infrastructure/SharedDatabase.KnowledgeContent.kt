package az.tribe.lifeplanner.infrastructure

import az.tribe.lifeplanner.database.KnowledgeCollectionEntity
import az.tribe.lifeplanner.database.KnowledgeLessonEntity

// --- Learn content cache accessors (server-owned lessons pulled from Supabase) ---

suspend fun SharedDatabase.getAllKnowledgeLessons(): List<KnowledgeLessonEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllKnowledgeLessons().executeAsList() }

suspend fun SharedDatabase.getAllKnowledgeCollections(): List<KnowledgeCollectionEntity> =
    this { db -> db.lifePlannerDBQueries.selectAllKnowledgeCollections().executeAsList() }

/**
 * Replaces the cached library wholesale, in one transaction. A whole-table swap rather than a merge
 * because the remote table is the sole author: a lesson pulled from Supabase should also disappear
 * locally when it is unpublished there.
 */
suspend fun SharedDatabase.replaceKnowledgeContent(
    lessons: List<KnowledgeLessonEntity>,
    collections: List<KnowledgeCollectionEntity>,
) {
    this { db ->
        db.lifePlannerDBQueries.transaction {
            db.lifePlannerDBQueries.deleteAllKnowledgeLessons()
            db.lifePlannerDBQueries.deleteAllKnowledgeCollections()
            lessons.forEach { l ->
                db.lifePlannerDBQueries.upsertKnowledgeLesson(
                    l.id, l.title, l.body, l.emoji, l.minLevel, l.readMin,
                    l.detail, l.takeaway, l.source, l.topics, l.sortOrder, l.fetchedAt,
                )
            }
            collections.forEach { c ->
                db.lifePlannerDBQueries.upsertKnowledgeCollection(
                    c.id, c.title, c.subtitle, c.emoji, c.lessonIds, c.sortOrder, c.fetchedAt,
                )
            }
        }
    }
}
