package az.tribe.lifeplanner.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.ObjectiveType
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Plus
import com.mmk.kmpnotifier.notification.NotifierManager
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val objectiveViewModel: BeginnerObjectiveViewModel = koinViewModel()
    LaunchedEffect(Unit) {
        objectiveViewModel.markObjectiveCompleted(ObjectiveType.SET_REMINDER)
    }

    LaunchedEffect(Unit) {
        NotifierManager.getPermissionUtil().askNotificationPermission()
    }

    val sections = remember(uiState.reminders) { groupReminders(uiState.reminders) }
    val activeCount = uiState.reminders.count { it.isEnabled }
    val autoCount = uiState.reminders.count { it.id.startsWith("auto-") }

    Scaffold(
        containerColor = MaterialTheme.modernColors.background,
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.modernColors.background,
                    titleContentColor = MaterialTheme.modernColors.textPrimary
                ),
                title = {
                    Column {
                        Text("Smart Reminders", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Text("$activeCount active \u2022 $autoCount auto-managed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.modernColors.textSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back), tint = MaterialTheme.modernColors.textPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showSettingsSheet() }) {
                        Icon(PhosphorIcons.Regular.Gear, contentDescription = stringResource(Res.string.cd_settings), tint = MaterialTheme.modernColors.textPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                text = { Text("Add Reminder") },
                icon = { Icon(PhosphorIcons.Regular.Plus, contentDescription = null) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { GlobalReminderToggle(isEnabled = uiState.settings.isEnabled, onToggle = { viewModel.toggleGlobalReminders(it) }) }

            if (uiState.reminders.isNotEmpty()) {
                item { Spacer(Modifier.height(4.dp)); SummaryCard(reminders = uiState.reminders) }
            }

            item { TestNotificationCard() }

            if (uiState.settings.smartTimingEnabled) {
                item { SmartTimingCard() }
            }

            if (uiState.reminders.isEmpty()) {
                item { Spacer(Modifier.height(8.dp)); EmptyRemindersCard(onAddClick = { viewModel.showAddDialog() }) }
            } else {
                sections.forEach { section ->
                    item {
                        Spacer(Modifier.height(8.dp))
                        ReminderSectionHeader(title = section.title, icon = section.icon, color = section.color, count = section.reminders.size)
                    }
                    items(section.reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onClick = { viewModel.selectReminder(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder.id) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (uiState.showAddDialog) {
        AddReminderSheet(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, message, type, frequency, time, days, smartTiming ->
                viewModel.createReminder(title = title, message = message, type = type, frequency = frequency, scheduledTime = time, scheduledDays = days, isSmartTiming = smartTiming)
            }
        )
    }

    if (uiState.showEditDialog && uiState.selectedReminder != null) {
        EditReminderSheet(
            reminder = uiState.selectedReminder!!,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { updated -> viewModel.updateReminder(updated) },
            onDelete = { viewModel.deleteReminder(uiState.selectedReminder!!.id); viewModel.hideEditDialog() }
        )
    }

    if (uiState.showSettingsSheet) {
        ReminderSettingsSheet(
            settings = uiState.settings,
            onDismiss = { viewModel.hideSettingsSheet() },
            onSave = { settings -> viewModel.updateSettings(settings); viewModel.hideSettingsSheet() }
        )
    }
}
