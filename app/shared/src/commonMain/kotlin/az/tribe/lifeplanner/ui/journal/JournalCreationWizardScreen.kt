package az.tribe.lifeplanner.ui.journal

import androidx.compose.animation.*
import co.touchlab.kermit.Logger
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.ui.components.rememberHapticManager
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.util.NetworkConnectivityObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class JournalWizardStep {
    MOOD, PROMPT, CONTEXT_GENERATE, REVIEW_SAVE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalCreationWizardScreen(
    onNavigateBack: () -> Unit,
    preSelectedGoalId: String? = null,
    initialMood: Mood? = null,
    /** Day this entry belongs to; null means today. */
    initialDate: kotlinx.datetime.LocalDate? = null,
    viewModel: JournalViewModel = koinViewModel(),
    goalViewModel: GoalViewModel = koinInject(),
    habitViewModel: az.tribe.lifeplanner.ui.habit.HabitViewModel = koinViewModel(),
    aiProxy: AiProxyService = koinInject()
) {
    val goals by goalViewModel.goals.collectAsState()
    val habitsWithStatus by habitViewModel.habits.collectAsState()
    val habits = habitsWithStatus.map { it.habit }
    val haptic = rememberHapticManager()
    val coroutineScope = rememberCoroutineScope()
    val connectivityObserver: NetworkConnectivityObserver = koinInject()
    val isConnected by connectivityObserver.isConnected.collectAsState()
    val isOffline = !isConnected

    // Track wizard start
    LaunchedEffect(Unit) { Analytics.journalWizardStarted() }

    // Wizard state. When a mood is handed in (from the Today mood prompt), step 1 is already
    // answered, so start on the PROMPT slide with that mood selected.
    var currentStep by remember { mutableStateOf(if (initialMood != null) JournalWizardStep.PROMPT else JournalWizardStep.MOOD) }
    var selectedMood by remember { mutableStateOf(initialMood) }
    var selectedPrompt by remember { mutableStateOf<String?>(null) }
    var userNote by remember { mutableStateOf("") }
    var selectedGoalId by remember { mutableStateOf<String?>(preSelectedGoalId) }
    var selectedHabitId by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedTitle by remember { mutableStateOf("") }
    var generatedContent by remember { mutableStateOf("") }
    var generatedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var detectedDecision by remember { mutableStateOf<DetectedDecision?>(null) }

    // Back handler
    val canGoBack = !isGenerating
    val onBack: () -> Unit = {
        when (currentStep) {
            JournalWizardStep.MOOD -> {
                Analytics.journalWizardAbandoned("mood")
                onNavigateBack()
            }
            JournalWizardStep.PROMPT -> currentStep = JournalWizardStep.MOOD
            JournalWizardStep.CONTEXT_GENERATE -> currentStep = JournalWizardStep.PROMPT
            JournalWizardStep.REVIEW_SAVE -> {
                if (!isGenerating) currentStep = JournalWizardStep.CONTEXT_GENERATE
            }
        }
    }

    Scaffold(
        topBar = {
            JournalWizardTopBar(
                currentStep = currentStep,
                onBackClick = if (canGoBack) onBack else ({})
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { step ->
            when (step) {
                JournalWizardStep.MOOD -> MoodSelectionStep(
                    selectedMood = selectedMood,
                    onMoodSelected = { mood ->
                        selectedMood = mood
                        haptic.click()
                        coroutineScope.launch {
                            delay(400)
                            currentStep = JournalWizardStep.PROMPT
                        }
                    }
                )

                JournalWizardStep.PROMPT -> PromptSelectionStep(
                    mood = selectedMood ?: Mood.NEUTRAL,
                    selectedPrompt = selectedPrompt,
                    onPromptSelected = { prompt ->
                        selectedPrompt = prompt
                        haptic.click()
                        coroutineScope.launch {
                            delay(300)
                            currentStep = JournalWizardStep.CONTEXT_GENERATE
                        }
                    }
                )

                JournalWizardStep.CONTEXT_GENERATE -> ContextAndGenerateStep(
                    goals = goals,
                    habits = habits,
                    selectedGoalId = selectedGoalId,
                    selectedHabitId = selectedHabitId,
                    userNote = userNote,
                    isGenerating = isGenerating,
                    isOffline = isOffline,
                    onGoalSelected = { selectedGoalId = it },
                    onHabitSelected = { selectedHabitId = it },
                    onNoteChanged = { userNote = it },
                    onGenerateClick = {
                        isGenerating = true
                        currentStep = JournalWizardStep.REVIEW_SAVE
                        coroutineScope.launch {
                            try {
                                val linkedGoal = selectedGoalId?.let { id -> goals.find { it.id == id } }
                                val linkedHabit = selectedHabitId?.let { id -> habits.find { it.id == id } }
                                val result = generateAiJournalEntry(
                                    aiProxy = aiProxy,
                                    mood = selectedMood ?: Mood.NEUTRAL,
                                    prompt = selectedPrompt ?: "",
                                    userNote = userNote,
                                    linkedGoal = linkedGoal,
                                    linkedHabit = linkedHabit
                                )
                                result?.let {
                                    generatedTitle = it.title
                                    generatedContent = it.content
                                    generatedTags = it.tags
                                    detectedDecision = it.detectedDecision
                                }
                            } catch (e: Exception) {
                                Logger.e("JournalCreationWizard", e) { "AI journal generation failed" }
                                // Stay on current step so user can retry
                                currentStep = JournalWizardStep.CONTEXT_GENERATE
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    onSkipAiClick = {
                        generatedTitle = ""
                        generatedContent = ""
                        generatedTags = emptyList()
                        detectedDecision = null
                        currentStep = JournalWizardStep.REVIEW_SAVE
                    }
                )

                JournalWizardStep.REVIEW_SAVE -> {
                    if (isGenerating) {
                        GeneratingOverlay()
                    } else {
                        ReviewAndSaveStep(
                            mood = selectedMood ?: Mood.NEUTRAL,
                            prompt = selectedPrompt,
                            title = generatedTitle,
                            content = generatedContent,
                            tags = generatedTags,
                            onTitleChanged = { generatedTitle = it },
                            onContentChanged = { generatedContent = it },
                            onTagsChanged = { generatedTags = it },
                            onSave = {
                                haptic.success()
                                val mood = selectedMood ?: Mood.NEUTRAL
                                val hasAiContent = generatedContent.isNotBlank()
                                Analytics.journalEntryCreated(mood.name, hasAiContent, "wizard")
                                Analytics.journalWizardCompleted(mood.name)
                                viewModel.createEntry(
                                    title = generatedTitle,
                                    content = generatedContent,
                                    mood = mood,
                                    linkedGoalId = selectedGoalId,
                                    linkedHabitId = selectedHabitId,
                                    tags = generatedTags,
                                    promptUsed = selectedPrompt,
                                    detectedDecision = detectedDecision,
                                    date = initialDate,
                                )
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}
