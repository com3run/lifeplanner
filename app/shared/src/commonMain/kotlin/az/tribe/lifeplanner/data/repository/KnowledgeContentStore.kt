package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.KnowledgeCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Learn library as the app currently knows it. Content is authored in Supabase and arrives via
 * `KnowledgeFetcher` (from the local cache first, then the network); until it does, reads fall back
 * to the lessons bundled with the build, so a first launch or an offline device is never empty.
 *
 * Mirrors [BuiltinCoachStore], which solves the same problem for coach personas.
 */
object KnowledgeContentStore {

    private val _lessons = MutableStateFlow<List<KnowledgeBit>>(emptyList())
    private val _collections = MutableStateFlow<List<KnowledgeCollection>>(emptyList())

    /** Emits whenever the library changes, so an open Learn map picks up new content. */
    val lessons: StateFlow<List<KnowledgeBit>> = _lessons.asStateFlow()
    val collections: StateFlow<List<KnowledgeCollection>> = _collections.asStateFlow()

    /**
     * Both halves land together: a collection referencing a lesson that has not arrived yet would
     * render a path with holes in it.
     */
    fun update(lessons: List<KnowledgeBit>, collections: List<KnowledgeCollection>) {
        if (lessons.isEmpty()) return
        _lessons.value = lessons
        _collections.value = collections
    }

    fun currentLessons(fallback: List<KnowledgeBit>): List<KnowledgeBit> =
        _lessons.value.ifEmpty { fallback }

    fun currentCollections(fallback: List<KnowledgeCollection>): List<KnowledgeCollection> =
        _collections.value.ifEmpty { fallback }
}
