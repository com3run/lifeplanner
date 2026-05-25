package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.LifeArea
import az.tribe.lifeplanner.domain.model.ObjectiveType
import az.tribe.lifeplanner.ui.health.HealthSection
import az.tribe.lifeplanner.ui.health.HealthViewModel
import az.tribe.lifeplanner.ui.health.ManualWeightDialog
import az.tribe.lifeplanner.ui.health.rememberHealthPermissionLauncher
import az.tribe.lifeplanner.ui.objectives.BeginnerObjectiveViewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeBalanceScreen(
    viewModel: LifeBalanceViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    showBackButton: Boolean = false,
    onCreateHabit: (LifeArea) -> Unit = {},
    onNavigateToCoach: (coachId: String, autoMessage: String) -> Unit = { _, _ -> },
    onNavigateToStoryReader: () -> Unit = {},
    onNavigateToHabitCreation: (LifeArea) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coachStories = remember { getCoachTipStories() }

    val healthViewModel: HealthViewModel = koinViewModel()
    val healthPermissionState by healthViewModel.permissionState.collectAsState()
    val todaySteps by healthViewModel.todaySteps.collectAsState()
    val stepsHistory by healthViewModel.stepsHistory.collectAsState()
    val weightHistory by healthViewModel.weightHistory.collectAsState()
    val latestWeight by healthViewModel.latestWeight.collectAsState()
    val heartRateHistory by healthViewModel.heartRateHistory.collectAsState()
    val latestHeartRate by healthViewModel.latestHeartRate.collectAsState()
    val sleepHistory by healthViewModel.sleepHistory.collectAsState()
    val latestSleep by healthViewModel.latestSleep.collectAsState()
    val showWeightDialog by healthViewModel.showWeightDialog.collectAsState()
    val requestPermissions = rememberHealthPermissionLauncher { granted ->
        if (granted) healthViewModel.onPermissionsGranted()
    }

    val objectiveViewModel: BeginnerObjectiveViewModel = koinViewModel()
    LaunchedEffect(Unit) {
        objectiveViewModel.markObjectiveCompleted(ObjectiveType.CHECK_LIFE_BALANCE)
    }

    LaunchedEffect(uiState.goalCreatedFeedback) {
        uiState.goalCreatedFeedback?.let { feedback ->
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearGoalFeedback()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Life Balance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadBalance(forceRefresh = true) }) {
                        Icon(PhosphorIcons.Regular.ArrowsClockwise, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Analysing your life balance...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBalance() }) { Text("Try Again") }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val report = uiState.report
                    if (report != null) {
                        // Life Constellation, score + animated web + AI cross-area insight
                        item { LifeWebCard(report = report) }

                        // Life Spectrum, proportional segment bar, focused areas in center
                        item { LifeBalanceSegmentBar(areaScores = report.areaScores) }

                        // Life areas grid
                        item { SectionHeader("Life Areas") }
                        item { AreaGrid(areaScores = report.areaScores) }

                        // Life Discovery, AI question flow → personalized goals
                        item { SectionHeader("Life Discovery") }
                        item {
                            DiscoverySection(
                                state = uiState.discovery,
                                onStart = { viewModel.startDiscovery() },
                                onSubmitAnswer = { viewModel.submitDiscoveryAnswer(it) },
                                onAddGoal = { viewModel.addDiscoveryGoal(it) },
                                onCreateHabit = { area -> onNavigateToHabitCreation(area) },
                                onReset = { viewModel.resetDiscovery() }
                            )
                        }
                    }

                    // Coach tips feed, with seen tracking and "all caught up"
                    item { SectionHeader("From Your Coaches") }
                    item {
                        CoachPostFeed(
                            stories = coachStories,
                            onOpenReader = { onNavigateToStoryReader() },
                            onStoryAction = { action ->
                                if (action?.startsWith("coach_") == true) {
                                    val coachId = action.removePrefix("coach_")
                                    onNavigateToCoach(coachId, "")
                                }
                            }
                        )
                    }

                    // Body & Health, embedded health dashboard
                    item { SectionHeader("Body & Health") }
                    item {
                        HealthSection(
                            permissionState = healthPermissionState,
                            todaySteps = todaySteps,
                            stepsHistory = stepsHistory,
                            heartRateHistory = heartRateHistory,
                            latestHeartRate = latestHeartRate,
                            sleepHistory = sleepHistory,
                            latestSleep = latestSleep,
                            weightHistory = weightHistory,
                            latestWeight = latestWeight,
                            onRequestPermissions = requestPermissions,
                            onAddWeight = { healthViewModel.showAddWeightDialog() }
                        )
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (showWeightDialog) {
        ManualWeightDialog(
            onDismiss = { healthViewModel.dismissWeightDialog() },
            onConfirm = { weight -> healthViewModel.addManualWeight(weight) }
        )
    }

    // Coach bottom sheet
    val currentSelectedInsight = uiState.selectedInsight
    if (uiState.showCoachSheet && currentSelectedInsight != null) {
        CoachSelectionSheet(
            insight = currentSelectedInsight,
            relevantCoaches = uiState.relevantCoaches,
            onCoachSelected = { coachId ->
                val message = viewModel.buildInsightMessage(currentSelectedInsight)
                viewModel.hideCoachSheet()
                onNavigateToCoach(coachId, message)
            },
            onCouncilSelected = {
                val message = viewModel.buildInsightMessage(currentSelectedInsight)
                viewModel.hideCoachSheet()
                onNavigateToCoach(CoachPersona.COUNCIL_ID, message)
            },
            onDismiss = { viewModel.hideCoachSheet() }
        )
    }
}
