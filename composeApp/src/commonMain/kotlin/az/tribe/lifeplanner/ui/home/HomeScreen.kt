package az.tribe.lifeplanner.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalStatus
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.ObjectiveType
import az.tribe.lifeplanner.ui.ability.AbilityViewModel
import az.tribe.lifeplanner.ui.components.AddGoalBottomSheet
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.GoalDependencyWidget
import az.tribe.lifeplanner.ui.components.NewBadgesCard
import az.tribe.lifeplanner.ui.components.NextAction
import az.tribe.lifeplanner.ui.components.NextActionCard
import az.tribe.lifeplanner.ui.components.StoriesCarousel
import az.tribe.lifeplanner.ui.components.UpdateReminderBanner
import az.tribe.lifeplanner.ui.components.VerifyEmailBanner
import az.tribe.lifeplanner.ui.components.WeeklyInsightsCard
import az.tribe.lifeplanner.ui.gamification.GamificationViewModel
import az.tribe.lifeplanner.ui.habit.HabitViewModel
import az.tribe.lifeplanner.ui.health.HealthPermissionState
import az.tribe.lifeplanner.ui.health.HealthViewModel
import az.tribe.lifeplanner.ui.home.CompactAbilityRow
import az.tribe.lifeplanner.ui.home.CompactHomeHabitRow
import az.tribe.lifeplanner.ui.home.ConnectHealthCard
import az.tribe.lifeplanner.ui.home.HealthMetricCard
import az.tribe.lifeplanner.ui.home.HeroBanner
import az.tribe.lifeplanner.ui.home.HomeCoachAICard
import az.tribe.lifeplanner.ui.home.HomeNavCard
import az.tribe.lifeplanner.ui.home.HomeSectionHeader
import az.tribe.lifeplanner.ui.home.HomeViewModel
import az.tribe.lifeplanner.ui.home.generateDailyIntentionStory
import az.tribe.lifeplanner.ui.home.generateDailyRecapStory
import az.tribe.lifeplanner.ui.home.getCuratedTipStories
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectivesCard
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.viewmodel.AuthState
import az.tribe.lifeplanner.ui.viewmodel.AuthViewModel
import az.tribe.lifeplanner.ui.viewmodel.*
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.goal.*
import az.tribe.lifeplanner.core.FeatureFlags
import az.tribe.lifeplanner.ui.auth.AuthBottomSheet
import az.tribe.lifeplanner.ui.profile.SecureAccountCTABanner
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Barbell
import com.adamglin.phosphoricons.regular.Footprints
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Plus
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: GoalViewModel,
    onGoalClick: (Goal) -> Unit,
    goToAnalytics: () -> Unit,
    onAddGoalClick: () -> Unit,
    goToAiGeneration: () -> Unit,
    onNavigateToHabits: () -> Unit = {},
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToRetrospective: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToLifeBalance: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToStoryReader: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToAbilities: () -> Unit = {},
    onNavigateToAbilityDetail: (String) -> Unit = {},
    onContinueChat: (sessionId: String) -> Unit = {},
    onStartFocusForMilestone: (goalId: String, milestoneId: String) -> Unit = { _, _ -> },
    onNavigateToJournalEntry: (entryId: String) -> Unit = {},
    showUpdateReminder: Boolean = false,
    onUpdateClick: () -> Unit = {},
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showAddGoalSheet by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = koinInject()
    val gamificationViewModel: GamificationViewModel = koinViewModel()
    val habitViewModel: HabitViewModel = koinViewModel()
    val objectiveViewModel: BeginnerObjectiveViewModel = koinViewModel()
    val abilityViewModel: AbilityViewModel = koinViewModel()
    val homeViewModel: HomeViewModel = koinViewModel()
    val healthViewModel: HealthViewModel = koinViewModel()

    val authState by authViewModel.authState.collectAsState()
    val userProgress by gamificationViewModel.userProgress.collectAsState()
    val newBadges by gamificationViewModel.newBadges.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val habits by habitViewModel.habits.collectAsState()
    val abilities by abilityViewModel.abilities.collectAsState()
    val weeklySnapshots by homeViewModel.weeklySnapshots.collectAsState()
    val goalDependencies by homeViewModel.goalDependencies.collectAsState()
    val healthPermissionState by healthViewModel.permissionState.collectAsState()
    val todaySteps by healthViewModel.todaySteps.collectAsState()
    val latestSleep by healthViewModel.latestSleep.collectAsState()
    val latestHeartRate by healthViewModel.latestHeartRate.collectAsState()
    val latestWeight by healthViewModel.latestWeight.collectAsState()
    val recentSession by homeViewModel.recentSession.collectAsState()
    val recentCoach by homeViewModel.recentCoach.collectAsState()
    val beginnerObjectives by objectiveViewModel.objectives.collectAsState()
    val objectivesExpanded by objectiveViewModel.isExpanded.collectAsState()
    val objectivesDismissed by objectiveViewModel.isDismissed.collectAsState()
    val pendingVerifyEmail by authViewModel.pendingVerificationEmail.collectAsState()
    val remoteStories by homeViewModel.remoteStories.collectAsState()

    val currentUser = when (authState) {
        is AuthState.Authenticated -> (authState as AuthState.Authenticated).user
        is AuthState.Guest -> (authState as AuthState.Guest).user
        else -> null
    }

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val upcomingGoals = goals.filter { it.status != GoalStatus.COMPLETED }.sortedBy { it.dueDate }.take(5)
    val goalsDueToday = goals.filter { it.status != GoalStatus.COMPLETED && it.dueDate == today }

    val allStories = remember(habits, userProgress, today, remoteStories) {
        listOf(generateDailyIntentionStory(today), generateDailyRecapStory(userProgress, habits, today)) +
            getCuratedTipStories(today) + remoteStories
    }

    val habitsCompleted = habits.count { it.isCompletedToday }
    val totalHabits = habits.size

    val nextAction = remember(goalsDueToday, habits, upcomingGoals) {
        val firstGoalDueToday = goalsDueToday.firstOrNull()
        val nextUncheckedHabit = habits.firstOrNull { !it.isCompletedToday } ?: habits.firstOrNull()
        val highestProgressGoal = upcomingGoals.filter { (it.progress ?: 0L) > 0L }.maxByOrNull { it.progress ?: 0L }
        when {
            firstGoalDueToday != null -> NextAction.GoalDueToday(firstGoalDueToday)
            nextUncheckedHabit != null -> NextAction.NextHabit(nextUncheckedHabit)
            highestProgressGoal != null -> NextAction.ContinueGoal(highestProgressGoal)
            else -> NextAction.AllCaughtUp
        }
    }

    val nextActionAfterHabitDone = remember(goalsDueToday, upcomingGoals) {
        val firstGoalDueToday = goalsDueToday.firstOrNull()
        val highestProgressGoal = upcomingGoals.filter { (it.progress ?: 0L) > 0L }.maxByOrNull { it.progress ?: 0L }
        when {
            firstGoalDueToday != null -> NextAction.GoalDueToday(firstGoalDueToday)
            highestProgressGoal != null -> NextAction.ContinueGoal(highestProgressGoal)
            else -> NextAction.AllCaughtUp
        }
    }

    var displayedNextAction by remember(nextAction) { mutableStateOf(nextAction) }

    LaunchedEffect(nextAction) {
        val current = nextAction
        if (current is NextAction.NextHabit && current.habitWithStatus.isCompletedToday) {
            delay(2000)
            displayedNextAction = nextActionAfterHabitDone
        }
    }

    val greetingLine = remember(currentUser?.displayName) {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val part = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        part + (currentUser?.displayName?.let { ", $it" } ?: "") + "!"
    }

    val motivationLine = remember {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        when (hour) {
            in 5..11 -> "Let's make today count"
            in 12..16 -> "Keep the momentum going"
            in 17..20 -> "Great work today"
            else -> "Rest well, recharge"
        }
    }

    val level = userProgress?.currentLevel ?: 1
    val streak = userProgress?.currentStreak ?: 0

    LaunchedEffect(authState) {
        viewModel.loadAnalytics()
        gamificationViewModel.refresh()
    }

    fun handleStoryAction(action: String?) {
        when (action) {
            "habits" -> onNavigateToHabits()
            "add_habit" -> onNavigateToAddHabit()
            "goals" -> onNavigateToGoals()
            "focus" -> onNavigateToFocus()
            "journal" -> onNavigateToJournal()
            "achievements" -> onNavigateToAchievements()
            "ai_chat" -> onNavigateToChat()
            "life_balance" -> onNavigateToLifeBalance()
            "health" -> onNavigateToHealth()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    actionContentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 84.dp
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)
        ) {
            if (authState is AuthState.Authenticated) {
                item(key = "hero_banner") {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        HeroBanner(
                            greeting = greetingLine,
                            subtitle = motivationLine,
                            level = level,
                            streak = streak,
                            levelTitle = userProgress?.title ?: "Novice",
                            isSignedIn = currentUser?.email != null,
                            onProfileClick = onNavigateToProfile
                        )
                    }
                }
            }

            if (allStories.isNotEmpty()) {
                item(key = "stories_carousel") {
                    StoriesCarousel(stories = allStories, onStoryAction = { handleStoryAction(it) }, onOpenReader = { onNavigateToStoryReader() })
                }
            }

            if (showUpdateReminder) {
                item(key = "update_reminder") {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        UpdateReminderBanner(onUpdateClick = onUpdateClick)
                    }
                }
            }

            if (!objectivesDismissed && beginnerObjectives.isNotEmpty()) {
                item(key = "beginner_objectives") {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        BeginnerObjectivesCard(
                            objectives = beginnerObjectives,
                            isExpanded = objectivesExpanded,
                            allComplete = beginnerObjectives.isNotEmpty() && beginnerObjectives.all { it.isCompleted },
                            onToggleExpanded = { objectiveViewModel.toggleExpanded() },
                            onDismiss = { objectiveViewModel.dismiss() },
                            onObjectiveClick = { type ->
                                when (type) {
                                    ObjectiveType.CREATE_GOAL -> onAddGoalClick()
                                    ObjectiveType.CREATE_HABIT -> onNavigateToAddHabit()
                                    ObjectiveType.WRITE_JOURNAL -> onNavigateToJournal()
                                    ObjectiveType.COMPLETE_HABIT_CHECKIN -> onNavigateToHabits()
                                    ObjectiveType.START_FOCUS_SESSION -> onNavigateToFocus()
                                    ObjectiveType.CHAT_WITH_COACH -> onNavigateToChat()
                                    ObjectiveType.SET_REMINDER -> onNavigateToReminders()
                                    ObjectiveType.CHECK_LIFE_BALANCE -> onNavigateToLifeBalance()
                                    ObjectiveType.COMPLETE_GOAL -> onNavigateToGoals()
                                    ObjectiveType.SECURE_ACCOUNT -> showAccountSheet = true
                                }
                            }
                        )
                    }
                }
            }

            if (newBadges.isNotEmpty()) {
                item(key = "new_badges") {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        NewBadgesCard(badges = newBadges, onClick = onNavigateToAchievements)
                    }
                }
            }

            if (pendingVerifyEmail != null) {
                item(key = "verify_email_banner") {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        VerifyEmailBanner(email = pendingVerifyEmail!!, onResend = { authViewModel.resendVerificationEmail(pendingVerifyEmail!!) })
                    }
                }
            } else if (currentUser?.email == null) {
                item {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        SecureAccountCTABanner(onClick = { showAccountSheet = true })
                    }
                }
            }

            if (weeklySnapshots.isNotEmpty()) {
                item(key = "weekly_insights") {
                    WeeklyInsightsCard(
                        snapshots = weeklySnapshots,
                        onDayClick = { onNavigateToRetrospective() },
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                    )
                }
            }

            item(key = "life_balance_summary") {
                LifeBalanceSummaryCard(
                    onViewFullReport = onNavigateToLifeBalance,
                    modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                )
            }

            when (healthPermissionState) {
                HealthPermissionState.GRANTED -> {
                    item(key = "health_widgets") {
                        Column(modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                            HomeSectionHeader("Health", onSeeAll = onNavigateToHealth)
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    HealthMetricCard(
                                        icon = PhosphorIcons.Regular.Footprints,
                                        value = todaySteps?.let { s ->
                                            if (s >= 1000) "${s / 1000}.${(s % 1000) / 100}K" else s.toString()
                                        } ?: "—",
                                        label = "Steps", color = Color(0xFF28C76F), modifier = Modifier.weight(1f)
                                    )
                                    HealthMetricCard(
                                        icon = PhosphorIcons.Regular.Heart,
                                        value = latestHeartRate?.let { "${it.toInt()} bpm" } ?: "—",
                                        label = "Heart Rate", color = Color(0xFFEA5455), modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    HealthMetricCard(
                                        icon = PhosphorIcons.Regular.Moon,
                                        value = latestSleep?.let { h -> "${h.toLong()}.${((h % 1) * 10).toLong()}h" } ?: "—",
                                        label = "Sleep", color = Color(0xFF7A5AF8), modifier = Modifier.weight(1f)
                                    )
                                    HealthMetricCard(
                                        icon = PhosphorIcons.Regular.Barbell,
                                        value = latestWeight?.let { w -> "${w.toLong()}.${((w % 1) * 10).toLong()} kg" } ?: "—",
                                        label = "Weight", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                HealthPermissionState.DENIED -> {
                    item(key = "health_cta") {
                        ConnectHealthCard(
                            modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal),
                            onClick = onNavigateToHealth
                        )
                    }
                }
                else -> {}
            }

            if (goalDependencies.isNotEmpty()) {
                item(key = "goal_dependencies") {
                    GoalDependencyWidget(
                        dependencies = goalDependencies,
                        goals = goals,
                        onGoalClick = { onGoalClick(it) },
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                    )
                }
            }

            // Skill Progression (Abilities) — hidden when feature is disabled
            if (FeatureFlags.ABILITIES_ENABLED) run {
                val topAbilities = abilities.take(3)

                item(key = "abilities_header") {
                    HomeSectionHeader(
                        title = "Skill Progression",
                        modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal),
                        onSeeAll = if (abilities.isNotEmpty()) onNavigateToAbilities else null
                    )
                }

                if (abilities.isEmpty()) {
                    item(key = "abilities_empty_cta") {
                        HomeNavCard(
                            icon = PhosphorIcons.Regular.Lightning,
                            iconColor = MaterialTheme.colorScheme.primary,
                            title = "Define your abilities",
                            subtitle = "Link habits to skills and level up over time",
                            onClick = onNavigateToAbilities,
                            modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                        )
                    }
                } else {
                    item(key = "abilities_list") {
                        GlassCard(
                            modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal).fillMaxWidth(),
                            cornerRadius = 16.dp
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                topAbilities.forEachIndexed { index, ability ->
                                    CompactAbilityRow(ability = ability, onClick = { onNavigateToAbilityDetail(ability.id) })
                                    if (index < topAbilities.size - 1) {
                                        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Explore section
            item(key = "explore_header") {
                Text(
                    "Explore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                )
            }

            item(key = "retrospective_card") {
                HomeNavCard(
                    icon = PhosphorIcons.Regular.ClockCounterClockwise,
                    iconColor = Color(0xFF7C4DFF),
                    title = "Yesterday's Recap",
                    subtitle = "Review what you accomplished",
                    onClick = onNavigateToRetrospective,
                    modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                )
            }

            item(key = "flow_focus_card") {
                HomeNavCard(
                    icon = PhosphorIcons.Regular.Play,
                    iconColor = Color(0xFFFF6B35),
                    title = "Flow Focus",
                    subtitle = "Start a free-flow focus session",
                    onClick = onNavigateToFocus,
                    modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                )
            }

            item(key = "coach_ai_card") {
                HomeCoachAICard(
                    session = recentSession,
                    coach = recentCoach,
                    coachUnlocked = level >= 3,
                    onClick = {
                        if (level >= 3) {
                            if (recentSession != null) onContinueChat(recentSession!!.coachId)
                            else onNavigateToChat()
                        } else {
                            onNavigateToChat()
                        }
                    },
                    modifier = Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)
                )
            }

            if (displayedNextAction !is NextAction.AllCaughtUp || goals.isNotEmpty() || habits.isNotEmpty()) {
                item {
                    Box(Modifier.padding(horizontal = LifePlannerDesign.Padding.screenHorizontal)) {
                        NextActionCard(
                            nextAction = displayedNextAction,
                            onGoalClick = onGoalClick,
                            onHabitCheckIn = { habitId -> habitViewModel.toggleCheckIn(habitId) }
                        )
                    }
                }
            }
        }
    }

    if (showAddGoalSheet) {
        AddGoalBottomSheet(
            onDismiss = { showAddGoalSheet = false },
            onQuickAddClick = { showAddGoalSheet = false; onAddGoalClick() },
            onAiGenerateClick = { showAddGoalSheet = false; goToAiGeneration() }
        )
    }

    if (showAccountSheet) {
        val isGuest = authState is AuthState.Guest
        AuthBottomSheet(
            isSignUp = isGuest,
            authViewModel = authViewModel,
            authState = authState,
            onDismiss = { showAccountSheet = false },
            onSuccess = { showAccountSheet = false }
        )
    }
}
