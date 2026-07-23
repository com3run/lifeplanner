// GoalDetailScreen.kt
package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.DotsThreeVertical
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Trash
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Ability
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.ui.components.AddDependencyBottomSheet
import az.tribe.lifeplanner.ui.components.CelebrationOverlay
import az.tribe.lifeplanner.ui.components.CelebrationType
import az.tribe.lifeplanner.ui.components.DependenciesCard
import az.tribe.lifeplanner.ui.components.GoalDetailDialogs
import az.tribe.lifeplanner.ui.components.GoalDetailHeroHeader
import az.tribe.lifeplanner.ui.components.StatusToggleButtons
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import az.tribe.lifeplanner.ui.goal.AiReasoningCard
import az.tribe.lifeplanner.ui.goal.CoachInsightCard
import az.tribe.lifeplanner.ui.goal.CompletedGoalBanner
import az.tribe.lifeplanner.ui.goal.EmptyMilestonesCard
import az.tribe.lifeplanner.ui.goal.GoalNotFoundState
import az.tribe.lifeplanner.ui.goal.ModernMilestonesCard
import az.tribe.lifeplanner.ui.goal.GoalDescriptionCard
import az.tribe.lifeplanner.ui.goal.PoweredByAbilitiesCard
import az.tribe.lifeplanner.ui.goal.ReflectionsCard
import az.tribe.lifeplanner.ui.theme.gradientColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalViewModel,
    dependencyViewModel: GoalDependencyViewModel = koinInject(),
    journalRepository: JournalRepository = koinInject(),
    abilityRepository: AbilityRepository = koinInject(),
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewDependencyGraph: (String) -> Unit = {},
    onNavigateToGoal: (String) -> Unit = {},
    onNavigateToJournal: (String) -> Unit = {},
    onReflectOnGoal: (String) -> Unit = {},
    onCoachClick: (String) -> Unit = {},
    onAbilityClick: (String) -> Unit = {}
) {
    val goals by viewModel.goals.collectAsState()
    val goal = goals.find { it.id == goalId }
    val dependencyUiState by dependencyViewModel.uiState.collectAsState()
    val lifeValues by viewModel.lifeValues.collectAsState()

    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var poweredByAbilities by remember { mutableStateOf<List<Ability>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    var showCompleteConfirmDialog by remember { mutableStateOf(false) }
    var showAllMilestonesCompletedDialog by remember { mutableStateOf(false) }
    var showGoalCelebration by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var showAddDependencySheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showValueSheet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val promptCompleteGoalId by viewModel.promptCompleteGoal.collectAsState()
    LaunchedEffect(promptCompleteGoalId) {
        if (promptCompleteGoalId == goalId) {
            showAllMilestonesCompletedDialog = true
            viewModel.clearCompleteGoalPrompt()
        }
    }

    LaunchedEffect(Unit) {
        dependencyViewModel.loadData()
    }

    LaunchedEffect(goalId) {
        goal?.let {
            dependencyViewModel.selectGoal(it)
            Analytics.goalViewed(goalId, it.category.name)
        }
        coroutineScope.launch {
            journalEntries = journalRepository.getEntriesByGoalId(goalId)
        }
        coroutineScope.launch {
            val links = abilityRepository.getAbilityLinksForGoal(goalId)
            poweredByAbilities = links.mapNotNull { abilityRepository.getAbilityById(it.abilityId) }
        }
    }

    if (goal == null) {
        GoalNotFoundState(onBackClick = onBackClick)
        return
    }

    val isCompleted = goal.status == GoalStatus.COMPLETED
    val coach = CoachPersona.getByCategory(goal.category)
    val primaryColor = goal.category.backgroundColor()
    val gradientColors = goal.category.gradientColors()

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Box {}
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                PhosphorIcons.Regular.DotsThreeVertical,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            if (!isCompleted) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    leadingIcon = { Icon(PhosphorIcons.Regular.PencilSimple, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onEditClick()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("History") },
                                leadingIcon = { Icon(PhosphorIcons.Regular.ClockCounterClockwise, null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showHistoryModal = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        PhosphorIcons.Regular.Trash,
                                        null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = gradientColors.first(),
                    scrolledContainerColor = gradientColors.first(),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                GoalDetailHeroHeader(
                    modifier = Modifier,
                    goal = goal
                )
            }

            if (isCompleted) {
                item {
                    CompletedGoalBanner(
                        milestonesCompleted = goal.milestones.count { it.isCompleted },
                        totalMilestones = goal.milestones.size,
                        reflectionsCount = journalEntries.size,
                        primaryColor = primaryColor,
                        onReopenGoal = {
                            viewModel.updateGoalStatus(goalId, GoalStatus.IN_PROGRESS)
                        }
                    )
                }
            } else {
                item {
                    StatusToggleButtons(
                        currentStatus = goal.status,
                        onStatusChange = { newStatus ->
                            if (newStatus == GoalStatus.COMPLETED && goal.status != GoalStatus.COMPLETED) {
                                showCompleteConfirmDialog = true
                            } else {
                                viewModel.updateGoalStatus(goalId, newStatus)
                            }
                        }
                    )
                }
            }

            item {
                GoalDescriptionCard(
                    description = goal.description,
                    notes = goal.notes,
                    isReadOnly = isCompleted,
                    onNotesClick = { showNotesDialog = true }
                )
            }

            // Goal as a journal: local narrative of where the user is on this journey.
            item {
                GoalJourneyCard(goal = goal)
            }

            // Pillar 1: Why-Chain (value → goal → milestones) + orphan nudge
            item {
                val linkedValue = lifeValues.find { it.id == goal.valueId }
                WhyChainComponent(
                    valueTitle = linkedValue?.title,
                    goalTitle = goal.title,
                    milestoneCount = goal.milestones.size,
                    onValueClick = { showValueSheet = true }
                )
            }

            if (!goal.aiReasoning.isNullOrBlank()) {
                item {
                    AiReasoningCard(reasoning = goal.aiReasoning!!)
                }
            }


            if (goal.milestones.isNotEmpty()) {
                item {
                    ModernMilestonesCard(
                        milestones = goal.milestones,
                        isReadOnly = isCompleted,
                        onMilestoneToggle = { milestoneId ->
                            viewModel.toggleMilestoneCompletion(goalId, milestoneId)
                        },
                        onAddMilestone = { showAddMilestoneDialog = true }
                    )
                }
            } else if (!isCompleted) {
                item {
                    EmptyMilestonesCard(
                        onAddMilestone = { showAddMilestoneDialog = true }
                    )
                }
            }

            item {
                CoachInsightCard(
                    coach = coach,
                    onMeetCoach = { onCoachClick(coach.id) },
                    valueTitle = lifeValues.find { it.id == goal.valueId }?.title
                )
            }

            item {
                DependenciesCard(
                    dependencies = dependencyUiState.selectedGoalDependencies,
                    goals = dependencyUiState.allGoals,
                    currentGoalId = goalId,
                    suggestedDependencies = dependencyUiState.suggestedDependencies,
                    onAddDependency = { showAddDependencySheet = true },
                    onRemoveDependency = { dependencyId ->
                        dependencyViewModel.removeDependency(dependencyId)
                    },
                    onViewDependencyGraph = { onViewDependencyGraph(goalId) },
                    onGoalClick = { linkedGoalId ->
                        onNavigateToGoal(linkedGoalId)
                    }
                )
            }

            if (poweredByAbilities.isNotEmpty()) {
                item {
                    PoweredByAbilitiesCard(
                        abilities = poweredByAbilities,
                        onAbilityClick = onAbilityClick
                    )
                }
            }

            if (journalEntries.isNotEmpty()) {
                item {
                    ReflectionsCard(
                        entries = journalEntries,
                        isReadOnly = isCompleted,
                        onAddReflection = { onReflectOnGoal(goalId) },
                        onEntryClick = { entryId -> onNavigateToJournal(entryId) }
                    )
                }
            }
        }
    }

    AddDependencyBottomSheet(
        isVisible = showAddDependencySheet,
        currentGoal = goal,
        availableGoals = dependencyUiState.allGoals.filter { it.id != goalId },
        onDismiss = { showAddDependencySheet = false },
        onAddDependency = { targetGoalId, dependencyType ->
            dependencyViewModel.addDependency(goalId, targetGoalId, dependencyType)
        }
    )

    GoalHistoryModal(
        isVisible = showHistoryModal,
        goalId = goalId,
        viewModel = viewModel,
        onDismiss = { showHistoryModal = false }
    )

    GoalDetailDialogs(
        goal = goal,
        goalId = goalId,
        viewModel = viewModel,
        showDeleteDialog = showDeleteDialog,
        showNotesDialog = showNotesDialog,
        showAddMilestoneDialog = showAddMilestoneDialog,
        showCompleteConfirmDialog = showCompleteConfirmDialog,
        showAllMilestonesCompletedDialog = showAllMilestonesCompletedDialog,
        onDismissDelete = { showDeleteDialog = false },
        onDismissNotes = { showNotesDialog = false },
        onDismissAddMilestone = { showAddMilestoneDialog = false },
        onCompleteConfirmed = {
            showCompleteConfirmDialog = false
            showGoalCelebration = true
        },
        onDismissComplete = { showCompleteConfirmDialog = false },
        onAllMilestonesConfirmed = {
            showAllMilestonesCompletedDialog = false
            showGoalCelebration = true
        },
        onDismissAllMilestonesCompleted = { showAllMilestonesCompletedDialog = false },
        onBackClick = onBackClick
    )

    CelebrationOverlay(
        type = CelebrationType.GOAL_COMPLETED,
        isVisible = showGoalCelebration,
        message = "Goal Complete!",
        onDismiss = { showGoalCelebration = false }
    )

    if (showValueSheet) {
        WhyThisGoalBottomSheet(
            values = lifeValues,
            selectedValueId = goal.valueId,
            onSelect = { viewModel.updateGoal(goal.copy(valueId = it)) },
            onDismiss = { showValueSheet = false }
        )
    }
}
