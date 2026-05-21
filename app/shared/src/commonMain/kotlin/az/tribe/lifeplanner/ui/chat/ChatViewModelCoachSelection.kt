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

            if (isNewSession) {
                val userName = _uiState.value.userContext?.userName?.takeIf { it.isNotBlank() }
                val nameHint = if (userName != null) " The user's name is $userName." else ""
                triggerWelcomeMessage(
                    "[NEW SESSION]$nameHint Introduce yourself as ${coach.name} (${coach.title}). " +
                    "Your personality is ${coach.personality}. Your specialties: ${coach.specialties.take(2).joinToString(", ")}. " +
                    "Warmly welcome the user${if (userName != null) " by name" else ""}. Ask ONE focused question about their current goals or challenges in your area."
                )
            }
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

            if (isNewSession) {
                val userName = _uiState.value.userContext?.userName?.takeIf { it.isNotBlank() }
                val nameHint = if (userName != null) " The user's name is $userName." else ""
                triggerWelcomeMessage(
                    "[NEW SESSION]$nameHint You are The Council — all 7 coaches speaking together. Each coach introduces themselves briefly in one sentence. End with a unified invitation for the user to share their biggest goal or challenge."
                )
            }
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

            if (isNewSession) {
                val userName = _uiState.value.userContext?.userName?.takeIf { it.isNotBlank() }
                val nameHint = if (userName != null) " The user's name is $userName." else ""
                triggerWelcomeMessage(
                    "[NEW SESSION]$nameHint Introduce yourself as ${customCoach.name} and warmly welcome the user${if (userName != null) " by name" else ""}. Ask one opening question to understand what they need help with today."
                )
            }
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
