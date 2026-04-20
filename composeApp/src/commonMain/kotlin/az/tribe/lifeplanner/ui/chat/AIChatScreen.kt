package az.tribe.lifeplanner.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.ObjectiveType
import az.tribe.lifeplanner.ui.balance.InsightMessageHolder
import az.tribe.lifeplanner.ui.chat.ChatContent
import az.tribe.lifeplanner.ui.chat.ChatViewModel
import az.tribe.lifeplanner.ui.chat.CoachLockedScreen
import az.tribe.lifeplanner.ui.chat.CoachSelectorStrip
import az.tribe.lifeplanner.ui.components.CoachListContentExtended
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import az.tribe.lifeplanner.util.NetworkConnectivityObserver
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    viewModel: ChatViewModel = koinInject(),
    coachId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCoach: (String) -> Unit = {},
    onNavigateToCreateCoach: () -> Unit = {},
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToCoachProfile: (String) -> Unit = {}
) {
    val gamificationViewModel: GamificationViewModel = koinViewModel()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val requiredLevel = 3
    val xpForLevel3 = 450
    val isUnlocked = (userProgress?.currentLevel ?: 1) >= requiredLevel

    if (!isUnlocked) {
        CoachLockedScreen(
            currentLevel = userProgress?.currentLevel ?: 1,
            currentXp = userProgress?.totalXp ?: 0,
            xpNeeded = xpForLevel3 - (userProgress?.totalXp ?: 0),
            requiredLevel = requiredLevel,
            totalXpRequired = xpForLevel3,
            onBack = onNavigateBack
        )
        return
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val connectivityObserver: NetworkConnectivityObserver = koinInject()
    val isConnected by connectivityObserver.isConnected.collectAsState()
    val isOffline = !isConnected
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val objectiveViewModel: BeginnerObjectiveViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
        viewModel.refreshUserContext()
    }

    LaunchedEffect(coachId) {
        if (coachId != null) viewModel.selectCoachById(coachId)
    }

    LaunchedEffect(uiState.showSessionList, uiState.isLoading) {
        if (uiState.showSessionList && !uiState.isLoading && coachId == null) {
            val mostRecentId = uiState.sessionsByCoach.entries
                .filter { it.value != null }
                .maxByOrNull { it.value!!.lastMessageAt }
                ?.key ?: "luna_general"
            viewModel.selectCoachById(mostRecentId)
        }
    }

    LaunchedEffect(uiState.showSessionList, uiState.isLoading, isOffline) {
        if (!isOffline && !uiState.showSessionList && !uiState.isLoading) {
            val pending = InsightMessageHolder.pendingMessage
            if (pending != null) {
                InsightMessageHolder.pendingMessage = null
                viewModel.sendMessage(pending)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (!uiState.showSessionList) {
                    CoachSelectorStrip(
                        coaches = CoachPersona.ALL_COACHES,
                        customCoaches = uiState.customCoaches,
                        selectedCoachId = uiState.currentCoach?.id,
                        isCouncilMode = uiState.isCouncilMode,
                        onSelectCoach = { viewModel.selectCoachById(it) },
                        onSelectCouncil = { viewModel.selectCoachById(CoachPersona.COUNCIL_ID) }
                    )
                }
            }

            if (uiState.isLoading && uiState.showSessionList) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.showSessionList) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    ChatContent(
                        messages = uiState.messages,
                        isSending = uiState.isSending,
                        isStreaming = uiState.isStreaming,
                        streamingText = uiState.streamingText,
                        isExecutingAction = uiState.executingAction,
                        onSendMessage = {
                            viewModel.sendMessage(it)
                            objectiveViewModel.markObjectiveCompleted(ObjectiveType.CHAT_WITH_COACH)
                        },
                        onExecuteSuggestion = { viewModel.executeCoachSuggestion(it) },
                        executedSuggestionIds = uiState.executedSuggestionIds,
                        isCouncilMode = uiState.isCouncilMode,
                        isOffline = isOffline,
                        coach = uiState.currentCoach,
                        goalQuestionnaire = uiState.goalQuestionnaire,
                        onAnswerGoalQuestion = { idx, answer -> viewModel.answerGoalQuestion(idx, answer) },
                        onSubmitQuestionnaire = { viewModel.submitGoalQuestionnaire() },
                        onCopyMessage = { text ->
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                            scope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
                        }
                    )
                    if (uiState.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            } else {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    CoachListContentExtended(
                        modifier = Modifier.align(Alignment.TopCenter),
                        builtinCoaches = CoachPersona.ALL_COACHES,
                        customCoaches = uiState.customCoaches,
                        coachGroups = uiState.coachGroups,
                        sessions = uiState.sessionsByCoach,
                        onBuiltinCoachClick = { coach -> viewModel.selectCoachById(coach.id) },
                        onCustomCoachClick = { customCoach -> viewModel.selectCustomCoach(customCoach) },
                        onGroupClick = { group -> viewModel.selectCoachGroup(group) },
                        onCouncilClick = { viewModel.selectCoachById(CoachPersona.COUNCIL_ID) },
                        onCreateCoach = onNavigateToCreateCoach,
                        onCreateGroup = onNavigateToCreateGroup
                    )
                }
            }
        }
    }
}
