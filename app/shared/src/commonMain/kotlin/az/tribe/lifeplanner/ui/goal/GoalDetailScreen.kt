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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.lerp
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
import az.tribe.lifeplanner.ui.components.GoalDetailHeroHeader
import az.tribe.lifeplanner.ui.components.StatusToggleButtons
import az.tribe.lifeplanner.ui.components.backgroundColor
import az.tribe.lifeplanner.ui.dependency.GoalDependencyViewModel
import az.tribe.lifeplanner.ui.goal.AiReasoningCard
import az.tribe.lifeplanner.ui.goal.CoachInsightCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalViewModel,
    dependencyViewModel: GoalDependencyViewModel = koinInject(),
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
    val goals by viewModel.goals.collectAsState()
    val goal = goals.find { it.id == goalId }
    val dependencyUiState by dependencyViewModel.uiState.collectAsState()
    val lifeValues by viewModel.lifeValues.collectAsState()

    var journalEntries by remember { mutableStateOf<List<JournalEntry>>(emptyList()) }
    var poweredByAbilities by remember { mutableStateOf<List<Ability>>(emptyList()) }
    // Habits linked to this goal make it a practice rather than a checklist. Null until loaded and
    // whenever nothing is linked, which is the ordinary case.
    var practice by remember(goalId) { mutableStateOf<GoalPractice?>(null) }
    // The wheel, so the goal's why can carry the user's own score for that area rather than just
    // naming it. Null until the wheel has been filled in at least once.
    val wheelReport by wheelRepository.observeWheel()
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
    val gradientColors = goal.category.gradientColors()

    val listState = rememberLazyListState()
    // How far the bar has travelled off the hero and onto the page: 0 while it still sits on the
    // gradient, 1 once the page is what is behind it.
    //
    // Driven by the list rather than a collapsing-bar behaviour on purpose. This is a small
    // TopAppBar, and exitUntilCollapsed does not collapse one — it slides the whole bar away, which
    // took Back and the overflow menu off screen with it and meant the colour never crossed
    // anywhere the user could see. Pinned bar, colour on a scroll fraction.
    val density = LocalDensity.current
    val barHeightPx = with(density) { 64.dp.toPx() } +
        WindowInsets.statusBars.getTop(density)
    val fadeDistancePx = with(density) { 72.dp.toPx() }
    val barFraction by remember(barHeightPx, fadeDistancePx) {
        derivedStateOf {
            // Measured against the hero's bottom edge rather than raw scroll distance. Fading on
            // distance alone turned the bar dark while the gradient was still filling the screen
            // behind it, which just moved the seam rather than removing it.
            val hero = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
                // Hero scrolled out of the list entirely: nothing left to blend with.
                ?: return@derivedStateOf 1f
            val gradientUnderBar = (hero.offset + hero.size) - barHeightPx
            1f - (gradientUnderBar / fadeDistancePx).coerceIn(0f, 1f)
        }
    }
    // White reads on every category gradient and disappears against the page, so the icons have to
    // cross with the bar behind them.
    val barIconColor = lerp(Color.White, MaterialTheme.colorScheme.onSurface, barFraction)
    val barContainerColor =
        lerp(gradientColors.first(), MaterialTheme.colorScheme.background, barFraction)

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
                            contentDescription = "Back",
                            tint = barIconColor,
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                PhosphorIcons.Regular.DotsThreeVertical,
                                contentDescription = "More options",
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
                GoalDetailHeroHeader(
                    modifier = Modifier,
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

            // Pillar 1: Why-Chain (value → goal → milestones) + orphan nudge
            item {
                val linkedValue = lifeValues.find { it.id == goal.valueId }
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
                    segments.count { it.score == lowestScore } > 1 -> "among your lowest"
                    else -> "your lowest"
                }

                WhyChainComponent(
                    valueTitle = "${area.emoji} ${area.displayName}",
                    goalTitle = goal.title,
                    milestoneCount = goal.milestones.size,
                    areaScore = areaScore,
                    lowestNote = lowestNote,
                    onValueClick = { showValueSheet = true }
                )
            }

            // The why and the coach saying it are one card, sitting high on the page where the
            // question is still fresh. They used to be two: reasoning here, and a full coach
            // profile far below that reintroduced the same coach on every goal.
            item(key = "coach_why") {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val area = goal.wheelArea
                    ?: az.tribe.lifeplanner.domain.service.GoalWheelAreaInferrer.infer(
                        goal.category, goal.title, goal.description,
                    )
                val segs = wheelReport?.segments.orEmpty()
                val low = segs.minOfOrNull { it.score }
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
                    areaIsLowest = low != null &&
                        segs.firstOrNull { it.area == area }?.score == low,
                )
                CoachWhyCard(
                    coach = coach,
                    read = az.tribe.lifeplanner.domain.service.CoachGoalRead.read(coach.name, snapshot),
                    reasoning = goal.aiReasoning?.takeIf { it.isNotBlank() },
                    onMeetCoach = { onCoachClick(coach.id) },
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
                                    onAdd = { title -> viewModel.addMilestone(goalId, title) },
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
