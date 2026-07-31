package az.tribe.lifeplanner.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.XpRewards
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Completing a Learn lesson: marks it read, pays the XP once, and awards the zone's badge if that
 * lesson was the last one standing in its path.
 *
 * XP is deliberately paid on *completion*, not on open. Reading used to be recorded the moment the
 * screen appeared, which would have made the reward farmable by opening a lesson and backing out.
 */
class KnowledgeDetailViewModel(
    private val knowledgeRepository: KnowledgeRepository,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    /** Set once the lesson has been credited, so a recomposition cannot pay twice. */
    private var handled = false

    private val _earnedXp = MutableStateFlow(0)

    /** The XP just earned for this lesson, 0 if it had already been read. Drives the reward pill. */
    val earnedXp: StateFlow<Int> = _earnedXp.asStateFlow()

    private val _earnedBadgeName = MutableStateFlow<String?>(null)

    /** The zone badge just cleared, if this lesson finished a path. */
    val earnedBadgeName: StateFlow<String?> = _earnedBadgeName.asStateFlow()

    fun onLessonCompleted(id: String) {
        if (handled) return
        handled = true
        viewModelScope.launch {
            runCatching {
                val alreadyRead = knowledgeRepository.readIds().first().contains(id)
                knowledgeRepository.markRead(id)

                // Re-reading is welcome, but it only pays the first time.
                if (!alreadyRead) {
                    gamificationRepository.awardXp(XpRewards.LESSON_READ.toLong())
                    _earnedXp.value = XpRewards.LESSON_READ
                }

                awardZoneBadgeIfCleared(id)
            }.onFailure {
                handled = false
                Logger.w("KnowledgeDetailViewModel") { "completing $id failed: ${it.message}" }
            }
        }
    }

    /**
     * A zone counts as cleared only when every lesson filed under it has been read, including any
     * still gated behind a level, so the badge means the whole path rather than the unlocked part.
     */
    private suspend fun awardZoneBadgeIfCleared(justReadId: String) {
        val collection = KnowledgeLibrary.collectionOf(justReadId) ?: return
        val badge = KnowledgeLibrary.badgeFor(collection.id) ?: return
        val alreadyHeld = gamificationRepository.hasBadge(badge)
        if (alreadyHeld) return

        val read = knowledgeRepository.readIds().first()
        if (collection.lessonIds.all { it in read }) {
            gamificationRepository.awardBadge(badge)
            _earnedBadgeName.value = badge.displayName
        }
    }
}
