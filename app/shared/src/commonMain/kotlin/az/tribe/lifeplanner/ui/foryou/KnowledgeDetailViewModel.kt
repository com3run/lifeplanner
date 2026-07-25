package az.tribe.lifeplanner.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch

/** Marks a Learn lesson read when its detail screen opens, so hub progress reflects it. */
class KnowledgeDetailViewModel(
    private val knowledgeRepository: KnowledgeRepository,
) : ViewModel() {

    fun markRead(id: String) {
        viewModelScope.launch {
            runCatching { knowledgeRepository.markRead(id) }
                .onFailure { Logger.w("KnowledgeDetailViewModel") { "markRead failed: ${it.message}" } }
        }
    }
}
