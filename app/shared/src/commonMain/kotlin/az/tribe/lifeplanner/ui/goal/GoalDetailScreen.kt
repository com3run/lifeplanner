// GoalDetailScreen.kt
package az.tribe.lifeplanner.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.analytics.Analytics
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Ability
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.GoalPractice
import az.tribe.lifeplanner.domain.model.PracticeWindow
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.domain.repository.AbilityRepository
import az.tribe.lifeplanner.domain.repository.JournalRepository
import az.tribe.lifeplanner.ui.components.AddDependencyBottomSheet
import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.components.CelebrationOverlay
import az.tribe.lifeplanner.ui.components.CelebrationType
import az.tribe.lifeplanner.ui.components.DependenciesCard
import az.tribe.lifeplanner.ui.components.GoalDetailDialogs
import az.tribe.lifeplanner.ui.components.GoalPaperHeader
import az.tribe.lifeplanner.ui.components.StatusToggleButtons
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import az.tribe.lifeplanner.ui.goal.CompletedGoalBanner
import az.tribe.lifeplanner.ui.goal.GoalNotFoundState
import az.tribe.lifeplanner.ui.goal.ModernMilestonesCard
import az.tribe.lifeplanner.ui.goal.GoalDescriptionCard
import az.tribe.lifeplanner.ui.goal.PoweredByAbilitiesCard
import az.tribe.lifeplanner.ui.goal.ReflectionsCard
import az.tribe.lifeplanner.ui.theme.gradientColors
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_more_options

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalViewModel,
    dependencyViewModel: GoalDependencyViewModel = koinViewModel(),
    journalRepository: JournalRepository = koinInject(),
    abilityRepository: AbilityRepository = koinInject(),
    habitRepository: az.tribe.lifeplanner.domain.repository.HabitRepository = koinInject(),
    wheelRepository: az.tribe.lifeplanner.domain.repository.WheelRepository = koinInject(),
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewDependencyGraph: (String) -> Unit = {},
    onNavigateToGoal: (String) -> Unit = {},
    onNavigateToJournal: (String) -> Unit = {},
    onReflectOnGoal: (String) -> Unit = {},
    onCoachClick: (String) -> Unit = {},
    onAbilityClick: (String) -> Unit = {},
    onExplorePossibilities: (String) -> Unit = {},
    onHabitClick: (String) -> Unit = {},
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val goal = goals.find { it.id == goalId }
    val dependencyUiState by dependencyViewModel.uiState.collectAsStateWithLifecycle()
    val lifeValues by viewModel.lifeValues.collectAsStateWithLifecycle()

    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var poweredByAbilities by remember { mutableStateOf<List<Ability>>(emptyList()) }
    // Habits linked to this goal make it a practice rather than a checklist. Null until loaded and
    // whenever nothing is linked, which is the ordinary case.
    var practice by remember(goalId) { mutableStateOf<GoalPractice?>(null) }
    // The wheel, so the goal's why can carry the user's own score for that area rather than just
    // naming it. Null until the wheel has been filled in at least once.
    //
    // remember is load-bearing: observeWheel() builds a fresh Flow, and calling it inside
    // composition made every recomposition resubscribe, every subscription emit a report stamped
    // with a new generatedAt, and every new report schedule the next recomposition. The screen
    // sat in that loop flickering until the flow instance was pinned.
    val wheelFlow = remember { wheelRepository.observeWheel() }
    val wheelReport by wheelFlow
        .collectAsState(initial = null as az.tribe.lifeplanner.domain.model.WheelReport?)
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
    val promptCompleteGoalId by viewModel.promptCompleteGoal.collectAsStateWithLifecycle()
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
        coroutineScope.launch {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            practice = PracticeWindow.of(habitRepository.getHabitsByGoalId(goalId), today)
        }
    }

    if (goal == null) {
        GoalNotFoundState(onBackClick = onBackClick)
        return
    }

    val isCompleted = goal.status == GoalStatus.COMPLETED
    val coach = CoachPersona.getByCategory(goal.category)
    val primaryColor = goal.category.backgroundColor()

    val listState = rememberLazyListState()
    // The paper header sits on the page background, so the bar has nothing to blend with any
    // more; the scroll-fraction colour crossing left with the gradient hero it was built for.
    val barIconColor = MaterialTheme.colorScheme.onSurface
    val barContainerColor = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {}
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = stringResource(Res.string.cd_back),
                            tint = barIconColor,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                PhosphorIcons.Regular.DotsThreeVertical,
                                contentDescription = stringResource(Res.string.cd_more_options),
                                tint = barIconColor,
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
                // The bar starts as the hero's own colour and ends as the page's. Both were the
                // gradient before, so scrolling left a slab of category red pinned above a dark
                // screen with nothing below it to belong to. scrolledContainerColor is not used
                // here because it only applies with a scroll behaviour attached, and this bar has
                // none — barContainerColor is already the blended value.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barContainerColor,
                    navigationIconContentColor = barIconColor,
                    titleContentColor = barIconColor,
                    actionIconContentColor = barIconColor,
                )
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                GoalPaperHeader(
                    goal = goal,
                    practice = practice,
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
                GoalJourneyCard(goal = goal, isPractice = practice != null)
            }

            // Pillar 1: the Why-Chain, which now tells the goal's whole story in one card:
            // area (the why), goal, milestones with their progress, and the coach's read on
            // where it stands. The read and the progress used to be a separate coach card and a
            // hero banner stat; three surfaces repeating each other is why none of them read as
            // meaningful.
            item {
                // Goals saved before wheel areas existed have none stored. Inferring at display
                // time means every goal shows a correct area straight away, without a migration
                // rewriting rows the user never asked us to touch.
                val area = goal.wheelArea
                    ?: az.tribe.lifeplanner.domain.service.GoalWheelAreaInferrer.infer(
                        goal.category, goal.title, goal.description,
                    )
                val areaScore = wheelReport?.scores?.firstOrNull { it.area == area }?.score
                val segments = wheelReport?.segments.orEmpty()
                val lowestScore = segments.minOfOrNull { it.score }
                // Areas tie for last more often than not on a rounded 0..10 scale, and singling one
                // out because it sorts first claims precision the scores do not have.
                val lowestNote = when {
                    areaScore == null || lowestScore == null || areaScore != lowestScore -> null
                    // A wheel where every area ties has no low point at all. "Among your lowest"
                    // on a row of tens is the app inventing a problem (same tie rule as the
                    // wheel's own headline).
                    segments.all { it.score == lowestScore } -> null
                    segments.count { it.score == lowestScore } > 1 -> "among your lowest"
                    else -> "your lowest"
                }

                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val snapshot = az.tribe.lifeplanner.domain.service.GoalSnapshot(
                    status = goal.status,
                    milestonesTotal = goal.milestones.size,
                    milestonesDone = goal.milestones.count { it.isCompleted },
                    nextStep = goal.milestones.firstOrNull { !it.isCompleted }?.title,
                    daysUntilDue = (goal.dueDate.toEpochDays() - today.toEpochDays()).toInt(),
                    ageDays = (today.toEpochDays() - goal.createdAt.date.toEpochDays()).toInt(),
                    reflections = journalEntries.size,
                    practiceDay = practice?.dayNumber,
                    practiceStreak = practice?.currentStreak,
                    areaName = area.displayName,
                    // A full tie means no area is meaningfully lowest, so the coach does not get
                    // to claim this one is (same tie rule as the wheel's headline).
                    areaIsLowest = lowestScore != null && areaScore == lowestScore &&
                        segments.any { it.score != lowestScore },
                )

                WhyChainComponent(
                    valueTitle = "${area.emoji} ${area.displayName}",
                    goalTitle = goal.title,
                    milestoneCount = goal.milestones.size,
                    milestonesDone = goal.milestones.count { it.isCompleted },
                    nextStep = goal.milestones.firstOrNull { !it.isCompleted }?.title,
                    areaScore = areaScore,
                    lowestNote = lowestNote,
                    coachRead = az.tribe.lifeplanner.domain.service.CoachGoalRead
                        .readBeyondProgress(coach.name, snapshot),
                    coachName = coach.name,
                    coachTitle = coach.title,
                    onChat = { onCoachClick(coach.id) },
                    reasoning = goal.aiReasoning?.takeIf { it.isNotBlank() },
                    onValueClick = { showValueSheet = true }
                )
            }


            // A goal with habits linked to it is kept, not completed, so the practice leads and the
            // milestone list (if there even is one) becomes a detail below it.
            practice?.let { p ->
                item(key = "practice") {
                    GoalPracticeCard(
                        practice = p,
                        onHabitClick = onHabitClick,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // Milestones and the coach's draft of what to add next are one section, not two.
            // Editing a plan beats inventing one, so the draft is always available on a live goal —
            // but it belongs under the list it feeds, otherwise taking a step adds a milestone the
            // user cannot see and the tap looks like it did nothing.
            if (goal.milestones.isNotEmpty()) {
                item(key = "milestones") {
                    ModernMilestonesCard(
                        milestones = goal.milestones,
                        isReadOnly = isCompleted,
                        onMilestoneToggle = { milestoneId ->
                            viewModel.toggleMilestoneCompletion(goalId, milestoneId)
                        },
                        onAddMilestone = { showAddMilestoneDialog = true },
                        coachDraft = if (isCompleted) null else {
                            {
                                CoachMilestonesContent(
                                    goalTitle = goal.title,
                                    category = goal.category,
                                    description = goal.description,
                                    existingTitles = goal.milestones.map { it.title },
                                )
                            }
                        },
                    )
                }
            } else if (!isCompleted && practice == null) {
                // No list to sit under yet, so the draft is the section — but only for a goal that
                // is actually a checklist. A practice goal with no milestones is not missing
                // anything, and offering to draft some is the app inventing a problem.
                item(key = "coach_milestones") {
                    CoachMilestonesCard(
                        goalTitle = goal.title,
                        category = goal.category,
                        description = goal.description,
                        existingTitles = emptyList(),
                        onAdd = { title -> viewModel.addMilestone(goalId, title) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // Pillar 6: the divergent way out when an in-progress goal stalls. Ported from the
            // redesign screen when the two goal details were merged.
            if (FeatureFlags.PILLAR_POSSIBILITY && !isCompleted) {
                item {
                    AppButton(
                        text = "Feeling stuck? Explore possibilities",
                        onClick = { onExplorePossibilities(goalId) },
                        variant = AppButtonVariant.PRIMARY,
                        leadingIcon = PhosphorIcons.Regular.Sparkle,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
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
        WheelAreaPickerSheet(
            selected = goal.wheelArea
                ?: az.tribe.lifeplanner.domain.service.GoalWheelAreaInferrer.infer(
                    goal.category, goal.title, goal.description,
                ),
            scores = wheelReport?.scores.orEmpty(),
            onSelect = {
                viewModel.updateGoal(goal.copy(wheelArea = it))
                showValueSheet = false
            },
            onDismiss = { showValueSheet = false }
        )
    }
}
