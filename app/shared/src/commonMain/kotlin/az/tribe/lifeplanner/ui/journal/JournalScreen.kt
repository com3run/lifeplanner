package az.tribe.lifeplanner.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Moon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.model.JournalEntry
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.ui.components.CompactGoalRow
import az.tribe.lifeplanner.ui.home.CompactHomeMilestoneRow
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.components.DayEntriesBottomSheet
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.MoodCalendar
import az.tribe.lifeplanner.ui.components.SwipeableGoalItem
import az.tribe.lifeplanner.ui.components.SwipeableHabitCard
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.ui.ability.AbilityCard
import az.tribe.lifeplanner.ui.ability.AbilityViewModel
import az.tribe.lifeplanner.ui.habit.HabitViewModel
import az.tribe.lifeplanner.ui.habit.*
import az.tribe.lifeplanner.ui.planner.WeeklyPlannerContent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateBack: () -> Unit,
    onEntryClick: (String) -> Unit = {},
    onNavigateToWizard: () -> Unit = {},
    isFromBottomNav: Boolean = false,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onGoalClick: (Goal) -> Unit = {},
    onAddGoalClick: () -> Unit = {},
    onAddHabitClick: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onAbilityClick: (String) -> Unit = {},
    onCreateAbility: () -> Unit = {},
    onStartFocusForMilestone: (goalId: String, milestoneId: String) -> Unit = { _, _ -> },
    viewModel: JournalViewModel = koinViewModel(),
    goalViewModel: GoalViewModel = koinViewModel(),
    habitViewModel: HabitViewModel = koinViewModel(),
    abilityViewModel: AbilityViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showNewEntryDialog by viewModel.showNewEntryDialog.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    val goals by goalViewModel.goals.collectAsState()
    val habitsWithStatus by habitViewModel.habits.collectAsState()
    val habits = habitsWithStatus.map { it.habit }
    val abilities by abilityViewModel.abilities.collectAsState()

    // Own tab state locally, the NavGraphBuilder closure captures selectedTab once at
    // graph-build time, so the parent parameter is stale after first composition.
    // We call onTabSelected as a side-effect so App.kt's navContextAction FAB stays in sync.
    var currentTab by remember { mutableStateOf(selectedTab) }

    var isCalendarExpanded by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    val listState = rememberLazyListState()
    val error by viewModel.error.collectAsState()

    val habitsCompleted = habitsWithStatus.count { it.isCompletedToday }
    val habitsTotal = habitsWithStatus.size
    val activeGoalCount = goals.count { it.status != GoalStatus.COMPLETED }

    val habitGroups = remember(habitsWithStatus) {
        val grouped = habitsWithStatus.groupBy { habitTimeSlot(it.habit.reminderTime) }
        listOf(HabitTimeSlot.MORNING, HabitTimeSlot.AFTERNOON, HabitTimeSlot.EVENING, HabitTimeSlot.ANYTIME)
            .mapNotNull { slot -> grouped[slot]?.takeIf { it.isNotEmpty() }?.let { slot to it } }
    }

    val sortedGoals = remember(goals) {
        val order = mapOf(GoalStatus.IN_PROGRESS to 0, GoalStatus.NOT_STARTED to 1, GoalStatus.COMPLETED to 2)
        goals.sortedBy { order[it.status] ?: 3 }
    }

    val upcomingGoals = remember(goals) {
        goals.filter { it.status != GoalStatus.COMPLETED }.sortedBy { it.dueDate }.take(3)
    }

    val nextMilestones = remember(goals) {
        goals.filter { it.status != GoalStatus.COMPLETED && it.milestones.isNotEmpty() }
            .mapNotNull { goal -> goal.milestones.firstOrNull { !it.isCompleted }?.let { goal to it } }
            .take(5)
    }
    val milestoneFocusMinutes by homeViewModel.milestoneFocusMinutes.collectAsState()
    LaunchedEffect(nextMilestones) { homeViewModel.loadMilestoneFocusMinutes(nextMilestones) }

    val bannerTitle = when (currentTab) {
        1 -> "Goals"
        2 -> "Habits"
        3 -> if (FeatureFlags.ABILITIES_ENABLED) "Abilities" else "Planner"
        else -> "Planner"
    }
    val bannerSubtitle = when (currentTab) {
        1 -> if (activeGoalCount == 0) "No active goals" else "$activeGoalCount active"
        2 -> if (habitsTotal == 0) "No habits yet" else "$habitsCompleted/$habitsTotal done today"
        3 -> if (FeatureFlags.ABILITIES_ENABLED) {
            if (abilities.isEmpty()) "No abilities yet" else "${abilities.size} abilities"
        } else {
            "Weekly view"
        }
        else -> "Weekly view"
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = MaterialTheme.colorScheme.inverseSurface, contentColor = MaterialTheme.colorScheme.inverseOnSurface, actionColor = MaterialTheme.colorScheme.primary, actionContentColor = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 84.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hub_tabs") {
                HubTabRow(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        onTabSelected(tab)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Tab 0: Weekly Planner ───────────────────────────────────────
            if (currentTab == 0) {
                item(key = "weekly_planner") {
                    WeeklyPlannerContent(
                        habitsWithStatus = habitsWithStatus,
                        onCheckIn = { habitViewModel.toggleCheckIn(it) },
                        activeGoalCount = activeGoalCount,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // ── Tab 1: Goals ────────────────────────────────────────────────
            else if (currentTab == 1) {
                if (nextMilestones.isNotEmpty()) {
                    item(key = "next_steps_header") {
                        Text("Next Steps (${nextMilestones.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                    item(key = "next_steps_card") {
                        GlassCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), cornerRadius = 16.dp) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                nextMilestones.forEachIndexed { index, (goal, milestone) ->
                                    CompactHomeMilestoneRow(
                                        goal = goal, milestone = milestone,
                                        focusMinutes = milestoneFocusMinutes[milestone.id] ?: 0,
                                        onRowClick = { onGoalClick(goal) },
                                        onStartFocus = { onStartFocusForMilestone(goal.id, milestone.id) }
                                    )
                                    if (index < nextMilestones.size - 1) {
                                        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                                    }
                                }
                            }
                        }
                    }
                }

                if (upcomingGoals.isNotEmpty()) {
                    item(key = "priority_goals_header") {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Priority Goals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${upcomingGoals.size} upcoming", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                    item(key = "priority_goals_card") {
                        GlassCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), cornerRadius = 16.dp) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                upcomingGoals.forEachIndexed { index, goal ->
                                    CompactGoalRow(goal = goal, onClick = { onGoalClick(goal) })
                                    if (index < upcomingGoals.lastIndex) {
                                        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                                    }
                                }
                            }
                        }
                    }
                    item(key = "all_goals_divider") {
                        Text("All Goals", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (sortedGoals.isEmpty()) {
                    item(key = "goals_empty") {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No goals yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(6.dp))
                            Text("Tap Add Goal to set your first target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    items(items = sortedGoals, key = { "goal_${it.id}" }) { goal ->
                        SwipeableGoalItem(
                            goal = goal, onClick = { onGoalClick(goal) },
                            onComplete = { goalViewModel.updateGoalStatus(goal.id, GoalStatus.COMPLETED) },
                            onDelete = { goalViewModel.deleteGoal(goal.id) },
                            scrollState = listState, modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            // ── Tab 2: Habits ───────────────────────────────────────────────
            else if (currentTab == 2) {
                if (habitsWithStatus.isEmpty()) {
                    item(key = "habits_empty") {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No habits yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(6.dp))
                            Text("Tap New Habit to start building consistency", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    habitGroups.forEach { (slot, habitsInSlot) ->
                        item(key = "header_${slot.name}") { HabitSectionHeader(slot) }
                        items(items = habitsInSlot, key = { "habit_${it.habit.id}" }) { habitWithStatus ->
                            SwipeableHabitCard(
                                habitWithStatus = habitWithStatus,
                                onCheckIn = { habitViewModel.toggleCheckIn(habitWithStatus.habit.id) },
                                onDelete = { habitViewModel.deleteHabit(habitWithStatus.habit.id) },
                                onEdit = { habitToEdit = habitWithStatus.habit },
                                onFocusClick = onNavigateToFocus,
                                modifier = Modifier.padding(horizontal = 16.dp).animateItem()
                            )
                        }
                    }
                }
            }

            // ── Tab 3: Abilities (hidden behind feature flag) ───────────────
            else if (currentTab == 3 && FeatureFlags.ABILITIES_ENABLED) {
                if (abilities.isEmpty()) {
                    item(key = "abilities_empty") {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡", style = MaterialTheme.typography.displaySmall)
                            Spacer(Modifier.height(12.dp))
                            Text("No abilities yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Spacer(Modifier.height(6.dp))
                            Text("Tap Add Ability to start leveling up", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    items(items = abilities, key = { "ability_${it.id}" }) { ability ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp).animateItem()) {
                            AbilityCard(ability = ability, onClick = { onAbilityClick(ability.id) })
                        }
                    }
                }
            }
        }

        // Journal entry sheet, accessible from any tab via FAB
        if (showNewEntryDialog) {
            NewJournalEntryBottomSheet(
                onDismiss = { viewModel.hideNewEntryDialog() },
                onConfirm = { title, content, mood, tags, linkedGoalId, linkedHabitId, promptUsed ->
                    viewModel.createEntry(title = title, content = content, mood = mood, linkedGoalId = linkedGoalId, linkedHabitId = linkedHabitId, tags = tags, promptUsed = promptUsed)
                },
                goals = goals,
                habits = habits,
                viewModel = viewModel,
            )
        }

        selectedDay?.let { date ->
            DayEntriesBottomSheet(
                date = date,
                entries = viewModel.getEntriesForDay(date),
                onDismiss = { viewModel.clearSelectedDay() },
                onEntryClick = { entryId -> viewModel.clearSelectedDay(); onEntryClick(entryId) },
                onAddEntry = { viewModel.clearSelectedDay(); viewModel.showNewEntryDialog() }
            )
        }

        habitToEdit?.let { habit ->
            EditHabitBottomSheet(
                habit = habit,
                onDismiss = { habitToEdit = null },
                onConfirm = { updatedHabit -> habitViewModel.updateHabit(updatedHabit); habitToEdit = null }
            )
        }
    }
}

@Composable
private fun HubTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tabs = if (FeatureFlags.ABILITIES_ENABLED)
        listOf("Planner", "Goals", "Habits", "Abilities")
    else
        listOf("Planner", "Goals", "Habits")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tabs.forEachIndexed { index, label ->
            val isSelected = selectedTab == index
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)) else null,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

// ─── Habit time-of-day grouping ─────────────────────────────────────────────

private enum class HabitTimeSlot { MORNING, AFTERNOON, EVENING, ANYTIME }

private fun habitTimeSlot(reminderTime: String?): HabitTimeSlot {
    val hour = reminderTime?.split(":")?.firstOrNull()?.toIntOrNull() ?: return HabitTimeSlot.ANYTIME
    return when (hour) {
        in 5..11 -> HabitTimeSlot.MORNING
        in 12..16 -> HabitTimeSlot.AFTERNOON
        else -> HabitTimeSlot.EVENING
    }
}

@Composable
private fun HabitSectionHeader(slot: HabitTimeSlot) {
    val (icon, label, tint) = when (slot) {
        HabitTimeSlot.MORNING -> Triple(PhosphorIcons.Regular.Sun, "Morning", Color(0xFFF59E0B))
        HabitTimeSlot.AFTERNOON -> Triple(PhosphorIcons.Regular.Sun, "Afternoon", Color(0xFFEF4444))
        HabitTimeSlot.EVENING -> Triple(PhosphorIcons.Regular.Moon, "Evening", Color(0xFF6366F1))
        HabitTimeSlot.ANYTIME -> Triple(PhosphorIcons.Regular.Sparkle, "Anytime", Color(0xFF6B7280))
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = tint)
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
