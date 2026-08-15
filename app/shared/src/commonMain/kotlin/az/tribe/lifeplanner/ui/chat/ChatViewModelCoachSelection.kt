package az.tribe.lifeplanner.ui.chat

import az.tribe.lifeplanner.data.repository.ChatRepositoryImpl
import az.tribe.lifeplanner.domain.model.CoachGroup
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CustomCoach
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

/**
 * Select a specific coach to chat with (one session per coach)
 */
fun ChatViewModel.selectCoach(coach: CoachPersona) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val session = chatRepository.getOrCreateSessionForCoach(coach.id)
            val messages = chatRepository.getMessages(session.id)
            val isNewSession = messages.isEmpty()

            val executedIds = messages
                .mapNotNull { it.metadata?.executedSuggestionIds }
                .flatten()
                .toSet()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentSession = session,
                currentCoach = coach,
                isCouncilMode = false,
                isCustomCoachMode = false,
                isCustomGroupMode = false,
                messages = messages,
                showSessionList = false,
                executedSuggestionIds = executedIds
            )

            loadSessions()

            // No auto-greeting. Opening a coach used to fire an AI call purely to generate a
            // hello, and the instruction that produced it was sent as a real user message —
            // persisted, filtered from the view exactly once, and back in the transcript on the
            // next load. Users were reading "[NEW SESSION] Introduce yourself as Morgan..." in
            // their own chat.
            //
            // A coach you open and talk to needs neither. The screen is quiet until someone says
            // something.
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}

/**
 * Select The Council group chat where all coaches can participate
 */
fun ChatViewModel.selectCouncil() {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val session = chatRepository.getOrCreateSessionForCoach(CoachPersona.COUNCIL_ID)
            val messages = chatRepository.getMessages(session.id)
            val isNewSession = messages.isEmpty()

            val executedIds = messages
                .mapNotNull { it.metadata?.executedSuggestionIds }
                .flatten()
                .toSet()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentSession = session,
                currentCoach = null,
                isCouncilMode = true,
                messages = messages,
                showSessionList = false,
                executedSuggestionIds = executedIds
            )

            loadSessions()


        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}

/**
 * Select a user-created custom coach
 */
fun ChatViewModel.selectCustomCoach(customCoach: CustomCoach) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val coachId = ChatRepositoryImpl.makeCustomCoachId(customCoach.id)
            val session = chatRepository.getOrCreateSessionForCoach(coachId)
            val messages = chatRepository.getMessages(session.id)
            val isNewSession = messages.isEmpty()

            val executedIds = messages
                .mapNotNull { it.metadata?.executedSuggestionIds }
                .flatten()
                .toSet()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentSession = session,
                currentCoach = null,
                currentCustomCoach = customCoach,
                currentCoachGroup = null,
                isCouncilMode = false,
                isCustomCoachMode = true,
                isCustomGroupMode = false,
                messages = messages,
                showSessionList = false,
                executedSuggestionIds = executedIds
            )

            loadSessions()

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}

/**
 * Select a user-created coach group
 */
fun ChatViewModel.selectCoachGroup(coachGroup: CoachGroup) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val groupId = ChatRepositoryImpl.makeGroupId(coachGroup.id)
            val session = chatRepository.getOrCreateSessionForCoach(groupId)
            val messages = chatRepository.getMessages(session.id)

            val executedIds = messages
                .mapNotNull { it.metadata?.executedSuggestionIds }
                .flatten()
                .toSet()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentSession = session,
                currentCoach = null,
                currentCustomCoach = null,
                currentCoachGroup = coachGroup,
                isCouncilMode = false,
                isCustomCoachMode = false,
                isCustomGroupMode = true,
                messages = messages,
                showSessionList = false,
                executedSuggestionIds = executedIds
            )

            loadSessions()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }
}
