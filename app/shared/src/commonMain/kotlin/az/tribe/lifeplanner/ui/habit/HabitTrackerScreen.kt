package az.tribe.lifeplanner.ui.habit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.ui.components.SwipeableHabitCard
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.ui.journal.JournalViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Plus
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddHabit: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onOpenDetail: (String) -> Unit = {},
    onOpenLesson: (String) -> Unit = {},
    isFromBottomNav: Boolean = false,
    viewModel: HabitViewModel = koinViewModel(),
    journalViewModel: JournalViewModel = koinViewModel(),
    goalViewModel: GoalViewModel = koinViewModel(),
    aiProxy: AiProxyService = koinInject()
) {
    val habits by viewModel.habits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val recentCheckIn by viewModel.recentCheckIn.collectAsState()
    val goals by goalViewModel.goals.collectAsState()

    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var showReflectionSheet by remember { mutableStateOf(false) }
    var habitForReflection by remember { mutableStateOf<Habit?>(null) }
    var linkedGoalForReflection by remember { mutableStateOf<Goal?>(null) }
    var xpForReflection by remember { mutableStateOf(0) }
    var lessonForReflection by remember { mutableStateOf<KnowledgeBit?>(null) }


    val snackbarHostState = remember { SnackbarHostState() }

    // Show smart reminder snackbar events
    LaunchedEffect(Unit) {
        viewModel.reminderEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Earned XP. The reflection sheet carries it when it opens, so this is the fallback for the
    // check-ins that do not raise a sheet.
    LaunchedEffect(Unit) {
        viewModel.xpEvent.collect { xp ->
            if (xp > 0 && !showReflectionSheet) snackbarHostState.showSnackbar("+$xp XP")
        }
    }

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Auto-show reflection sheet after habit check-in, no snackbar intermediary
    LaunchedEffect(recentCheckIn) {
        recentCheckIn?.let { checkIn ->
            habitForReflection = checkIn.habit
            linkedGoalForReflection = checkIn.habit.linkedGoalId?.let { goalId ->
                goals.find { it.id == goalId }
            }
            xpForReflection = checkIn.xpAwarded
            lessonForReflection = checkIn.lesson
            showReflectionSheet = true
            viewModel.clearRecentCheckIn()
        }
    }

    val todayCompleted = viewModel.getTodayCompletedCount()
    val totalHabits = viewModel.getTotalHabitsCount()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Habit Tracker",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (totalHabits > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (todayCompleted == totalHabits)
                                    Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "$todayCompleted/$totalHabits",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (todayCompleted == totalHabits)
                                        Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (!isFromBottomNav) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Box(modifier = Modifier) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddHabit,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(PhosphorIcons.Regular.Plus, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Habit", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionColor = MaterialTheme.colorScheme.primary,
                    actionContentColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = PaddingValues(top = padding.calculateTopPadding()))
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 136.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = habits,
                        key = { it.habit.id }
                    ) { habitWithStatus ->
                        SwipeableHabitCard(
                            habitWithStatus = habitWithStatus,
                            onCheckIn = { viewModel.toggleCheckIn(habitWithStatus.habit.id) },
                            onDelete = { viewModel.deleteHabit(habitWithStatus.habit.id) },
                            onEdit = { habitToEdit = habitWithStatus.habit },
                            onCardClick = { onOpenDetail(habitWithStatus.habit.id) },
                            onFocusClick = { onNavigateToFocus() },
                            onIncrement = if (habitWithStatus.habit.trackMode == HabitTrackMode.COUNT) {
                                { viewModel.incrementCheckIn(habitWithStatus.habit.id) }
                            } else null,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        // Edit Habit Bottom Sheet
        habitToEdit?.let { habit ->
            EditHabitBottomSheet(
                habit = habit,
                onDismiss = { habitToEdit = null },
                onConfirm = { updatedHabit ->
                    viewModel.updateHabit(updatedHabit)
                    habitToEdit = null
                }
            )
        }

        // Quick Reflection Bottom Sheet
        val currentHabitForReflection = habitForReflection
        if (showReflectionSheet && currentHabitForReflection != null) {
            QuickReflectionBottomSheet(
                habit = currentHabitForReflection,
                linkedGoal = linkedGoalForReflection,
                aiProxy = aiProxy,
                xpAwarded = xpForReflection,
                lesson = lessonForReflection,
                onOpenLesson = onOpenLesson,
                onDismiss = {
                    showReflectionSheet = false
                    habitForReflection = null
                    linkedGoalForReflection = null
                },
                onSave = { title, content, mood ->
                    journalViewModel.createEntry(
                        title = title,
                        content = content,
                        mood = mood,
                        linkedGoalId = linkedGoalForReflection?.id,
                        linkedHabitId = currentHabitForReflection.id
                    )
                    showReflectionSheet = false
                    habitForReflection = null
                    linkedGoalForReflection = null
                }
            )
        }
    }
}
