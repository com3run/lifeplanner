package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.data.repository.KnowledgeContentStore
import az.tribe.lifeplanner.database.KnowledgeCollectionEntity
import az.tribe.lifeplanner.database.KnowledgeLessonEntity
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.KnowledgeCollection
import az.tribe.lifeplanner.domain.service.KnowledgeTopic
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import az.tribe.lifeplanner.infrastructure.getAllKnowledgeCollections
import az.tribe.lifeplanner.infrastructure.getAllKnowledgeLessons
import az.tribe.lifeplanner.infrastructure.replaceKnowledgeContent
import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
private data class KnowledgeLessonDto(
    val id: String,
    val title: String,
    val body: String,
    val emoji: String = "💡",
    @SerialName("min_level") val minLevel: Int = 1,
    @SerialName("read_min") val readMin: Int = 2,
    val detail: List<String> = emptyList(),
    val takeaway: String = "",
    val source: String? = null,
    val topics: List<String> = emptyList(),
)

@Serializable
private data class KnowledgeCollectionDto(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val emoji: String = "📚",
    @SerialName("lesson_ids") val lessonIds: List<String> = emptyList(),
)

/**
 * Pulls the Learn library from Supabase, caches it locally, and publishes it to
 * [KnowledgeContentStore]. Content is server-authored, so this is a one-way pull with no syncer:
 * nothing here is ever pushed back.
 *
 * Call [loadCache] first for an instant, offline-capable library, then [fetch] to refresh it.
 */
class KnowledgeFetcher(
    private val supabase: SupabaseClient,
    private val db: SharedDatabase,
) {
    private val log = Logger.withTag("KnowledgeFetcher")
    private val json = Json { ignoreUnknownKeys = true }

    /** Publishes the last-cached library. No network, so the Learn map is populated immediately. */
    suspend fun loadCache() {
        try {
            val lessons = db.getAllKnowledgeLessons().map { it.toBit() }
            val collections = db.getAllKnowledgeCollections().map { it.toCollection() }
            if (lessons.isNotEmpty()) {
                KnowledgeContentStore.update(lessons, collections)
                log.d { "Loaded ${lessons.size} cached lessons" }
            }
        } catch (e: Exception) {
            log.w { "Failed to read cached lessons (using bundled): ${e.message}" }
        }
    }

    suspend fun fetch() {
        try {
            val lessonDtos = supabase.postgrest["knowledge_lessons"]
                .select {
                    filter { eq("is_active", true) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<KnowledgeLessonDto>()

            // An empty publish would wipe a working library, so treat it as a failed fetch.
            if (lessonDtos.isEmpty()) {
                log.w { "knowledge_lessons returned nothing, keeping current library" }
                return
            }

            val collectionDtos = supabase.postgrest["knowledge_collections"]
                .select {
                    filter { eq("is_active", true) }
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<KnowledgeCollectionDto>()

            val lessons = lessonDtos.mapNotNull { it.toBit() }
            if (lessons.isEmpty()) {
                log.w { "no lesson decoded cleanly, keeping current library" }
                return
            }
            val collections = collectionDtos.map { it.toCollection() }

            KnowledgeContentStore.update(lessons, collections)
            persist(lessonDtos, collectionDtos)
            log.i { "Loaded ${lessons.size} lessons and ${collections.size} paths from Supabase" }
        } catch (e: Exception) {
            log.w { "Failed to fetch lessons (using cached or bundled): ${e.message}" }
        }
    }

    private suspend fun persist(
        lessons: List<KnowledgeLessonDto>,
        collections: List<KnowledgeCollectionDto>,
    ) {
        try {
            val at = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            db.replaceKnowledgeContent(
                lessons = lessons.mapIndexed { i, d ->
                    KnowledgeLessonEntity(
                        id = d.id,
                        title = d.title,
                        body = d.body,
                        emoji = d.emoji,
                        minLevel = d.minLevel.toLong(),
                        readMin = d.readMin.toLong(),
                        detail = json.encodeToString(d.detail),
                        takeaway = d.takeaway,
                        source = d.source,
                        topics = json.encodeToString(d.topics),
                        sortOrder = i.toLong(),
                        fetchedAt = at,
                    )
                },
                collections = collections.mapIndexed { i, d ->
                    KnowledgeCollectionEntity(
                        id = d.id,
                        title = d.title,
                        subtitle = d.subtitle,
                        emoji = d.emoji,
                        lessonIds = json.encodeToString(d.lessonIds),
                        sortOrder = i.toLong(),
                        fetchedAt = at,
                    )
                },
            )
        } catch (e: Exception) {
            log.w { "Failed to cache lessons locally: ${e.message}" }
        }
    }

    /** An unrecognised topic is dropped rather than failing the lesson it appears on. */
    private fun parseTopics(raw: List<String>): List<KnowledgeTopic> =
        raw.mapNotNull { name -> runCatching { KnowledgeTopic.valueOf(name.uppercase()) }.getOrNull() }

    private fun KnowledgeLessonDto.toBit(): KnowledgeBit? {
        if (id.isBlank() || title.isBlank()) return null
        return KnowledgeBit(
            id = id,
            title = title,
            body = body,
            emoji = emoji,
            minLevel = minLevel,
            readMin = readMin,
            detail = detail,
            takeaway = takeaway,
            source = source,
            topics = parseTopics(topics),
        )
    }

    private fun KnowledgeCollectionDto.toCollection() =
        KnowledgeCollection(id, title, subtitle, emoji, lessonIds)

    private fun KnowledgeLessonEntity.toBit() = KnowledgeBit(
        id = id,
        title = title,
        body = body,
        emoji = emoji,
        minLevel = minLevel.toInt(),
        readMin = readMin.toInt(),
        detail = decodeList(detail),
        takeaway = takeaway,
        source = source,
        topics = parseTopics(decodeList(topics)),
    )

    private fun KnowledgeCollectionEntity.toCollection() =
        KnowledgeCollection(id, title, subtitle, emoji, decodeList(lessonIds))

    private fun decodeList(raw: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
}
