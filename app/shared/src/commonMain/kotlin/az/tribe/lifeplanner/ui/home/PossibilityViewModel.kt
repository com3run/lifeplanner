package az.tribe.lifeplanner.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.ActionOption
import az.tribe.lifeplanner.domain.service.PossibilityContextProvider
import az.tribe.lifeplanner.domain.service.PossibilityEngine
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Pillar 2, drives the "Right now you could…" Home card. Pulls the current
 * [az.tribe.lifeplanner.domain.model.PossibilityContext] and ranks it on demand.
 */
class PossibilityViewModel(
    private val contextProvider: PossibilityContextProvider,
    private val engine: PossibilityEngine,
) : ViewModel() {

    private val _options = MutableStateFlow<List<ActionOption>>(emptyList())
    val options: StateFlow<List<ActionOption>> = _options.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _options.value = try {
                engine.rank(contextProvider.currentContext())
            } catch (e: Exception) {
                Logger.w("PossibilityViewModel") { "Failed to build possibilities: ${e.message}" }
                emptyList()
            }
        }
    }
}
