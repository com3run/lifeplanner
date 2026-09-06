package az.tribe.lifeplanner.ui.retrospective

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.Sparkle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import kotlin.time.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_back
import leanlifeplanner.app.shared.generated.resources.cd_regenerate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetrospectiveScreen(
    onNavigateBack: () -> Unit,
    viewModel: RetrospectiveViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val isViewingToday = uiState.selectedDate == today

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Day Retrospective",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = stringResource(Res.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {}
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
                top = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Date Navigator
            item {
                DateNavigator(
                    selectedDate = uiState.selectedDate,
                    onPrevious = viewModel::goToPreviousDay,
                    onNext = viewModel::goToNextDay,
                    onDateTap = { showDatePicker = true }
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            } else if (uiState.snapshot != null && uiState.snapshot?.hasAnyActivity == false) {
                item { EmptyDayState() }
            } else {
                val snapshot = uiState.snapshot ?: return@LazyColumn

                // 2. Day Summary Card
                item { DaySummaryCard(snapshot) }

                // 3. AI Recap Card, always shown (loading, ready, or generate button)
                item {
                    AiRecapCard(
                        recap = uiState.aiRecap,
                        isGenerating = uiState.isGeneratingRecap,
                        onGenerate = viewModel::generateRecap
                    )
                }

                // 4. Compare with Today, shown automatically for past dates when today has data
                val todaySnap = uiState.todaySnapshot
                if (!isViewingToday && todaySnap?.hasAnyActivity == true) {
                    item {
                        CompareSection(
                            thenSnapshot = snapshot,
                            nowSnapshot = todaySnap,
                            thenDate = uiState.selectedDate
                        )
                    }
                }

                // 5. Mood & Journal
                if (snapshot.journalEntries.isNotEmpty()) {
                    item { SectionHeader("Mood & Journal") }
                    item { JournalSection(snapshot.journalEntries) }
                }

                // 6. Habits
                if (snapshot.habitSummary.habits.isNotEmpty()) {
                    item { SectionHeader("Habits (${snapshot.habitSummary.completedHabits}/${snapshot.habitSummary.totalHabits})") }
                    item { HabitsSection(snapshot.habitSummary) }
                }

                // 7. Focus Sessions
                if (snapshot.focusSessions.isNotEmpty()) {
                    item { SectionHeader("Focus Sessions") }
                    item { FocusSection(snapshot.focusSessions) }
                }

                // 8. Goal Changes
                if (snapshot.goalChanges.isNotEmpty()) {
                    item { SectionHeader("Goal Changes") }
                    item { GoalChangesSection(snapshot.goalChanges) }
                }

                // 9. Badges Earned
                if (snapshot.badgesEarned.isNotEmpty()) {
                    item { SectionHeader("Badges Earned") }
                    item { BadgesSection(snapshot.badgesEarned) }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate
                .atStartOfDayIn(kotlinx.datetime.TimeZone.UTC)
                .toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(kotlinx.datetime.TimeZone.UTC).date
                        viewModel.selectDate(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AiRecapCard(
    recap: String?,
    isGenerating: Boolean,
    onGenerate: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Column {
            // Gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                when {
                    isGenerating -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Generating your recap…",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "AI is reflecting on your day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).align(Alignment.CenterVertically),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    recap != null -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Day Recap",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                recap,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                            )
                        }
                        IconButton(
                            onClick = onGenerate,
                            modifier = Modifier.size(32.dp).align(Alignment.Top)
                        ) {
                            Icon(
                                PhosphorIcons.Regular.ArrowsClockwise,
                                contentDescription = stringResource(Res.string.cd_regenerate),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    else -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Day Recap",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Let AI summarize your day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = onGenerate,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                "Generate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
