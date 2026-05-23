package az.tribe.lifeplanner.ui.becoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.model.IdentityStatement
import az.tribe.lifeplanner.domain.model.LifeValue
import az.tribe.lifeplanner.domain.model.ValueAlignment
import az.tribe.lifeplanner.domain.repository.IdentityStatementRepository
import az.tribe.lifeplanner.domain.repository.LifeValueRepository
import az.tribe.lifeplanner.usecases.ComputeValueAlignmentUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Pillar 5 — the "Becoming" layer: the user's identity statements + how their actual choices
 * align to each value. Additive — XP/levels live on elsewhere; this answers "who am I becoming?"
 */
class BecomingViewModel(
    private val identityRepo: IdentityStatementRepository,
    private val computeAlignment: ComputeValueAlignmentUseCase,
    private val lifeValueRepo: LifeValueRepository,
) : ViewModel() {

    data class State(
        val isLoading: Boolean = true,
        val statements: List<IdentityStatement> = emptyList(),
        val alignments: List<ValueAlignment> = emptyList(),
        val values: List<LifeValue> = emptyList(), // for the add-statement value picker
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            try {
                _state.value = State(
                    isLoading = false,
                    statements = identityRepo.getAll(),
                    alignments = computeAlignment(),
                    values = lifeValueRepo.getActiveLifeValues(),
                )
            } catch (e: Exception) {
                Logger.w("BecomingVM") { "load failed: ${e.message}" }
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun addStatement(text: String, valueId: String?) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            identityRepo.insert(
                IdentityStatement(
                    id = Uuid.random().toString(),
                    statement = text.trim(),
                    valueId = valueId,
                    createdAt = now,
                )
            )
            refresh()
        }
    }

    fun deleteStatement(id: String) {
        viewModelScope.launch {
            identityRepo.deleteById(id)
            refresh()
        }
    }
}
