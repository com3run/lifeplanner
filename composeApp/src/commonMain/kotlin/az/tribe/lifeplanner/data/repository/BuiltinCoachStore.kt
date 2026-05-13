package az.tribe.lifeplanner.data.repository

import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachPersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BuiltinCoachStore {
    private val _coaches = MutableStateFlow<List<CoachPersona>>(emptyList())
    val coaches: StateFlow<List<CoachPersona>> = _coaches.asStateFlow()

    fun update(newCoaches: List<CoachPersona>) { _coaches.value = newCoaches }

    /** Merges TribeBot data (imageUrl, clipUrl, media) into existing coaches matched by name (case-insensitive). */
    fun mergeMedia(tribeBotCoaches: List<CoachPersona>) {
        val byName = tribeBotCoaches.associateBy { it.name.lowercase() }
        _coaches.value = _coaches.value.map { coach ->
            val tb = byName[coach.name.lowercase()] ?: return@map coach
            coach.copy(
                imageUrl = tb.imageUrl ?: coach.imageUrl,
                clipUrl = tb.clipUrl ?: coach.clipUrl,
                posterUrl = tb.posterUrl ?: coach.posterUrl,
                media = if (tb.media.isNotEmpty()) tb.media else coach.media,
            )
        }
    }

    fun getAll(): List<CoachPersona> = _coaches.value.ifEmpty { CoachPersona.ALL_COACHES }

    fun getById(id: String): CoachPersona =
        getAll().find { it.id == id } ?: CoachPersona.ALL_COACHES.first()

    fun getByCategory(category: GoalCategory): CoachPersona =
        getAll().find { it.category == category } ?: CoachPersona.ALL_COACHES.first()
}
