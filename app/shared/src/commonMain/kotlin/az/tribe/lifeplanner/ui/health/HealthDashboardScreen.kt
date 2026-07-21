package az.tribe.lifeplanner.ui.health

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import az.tribe.lifeplanner.ui.components.AnimatedIcon
import az.tribe.lifeplanner.ui.components.IconAnimationEffect
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.ArrowLeft
import com.adamglin.phosphoricons.bold.Barbell
import com.adamglin.phosphoricons.bold.CloudArrowUp
import com.adamglin.phosphoricons.bold.CloudCheck
import com.adamglin.phosphoricons.bold.CloudSlash
import com.adamglin.phosphoricons.bold.Footprints
import com.adamglin.phosphoricons.bold.Heartbeat
import com.adamglin.phosphoricons.bold.Moon
import kotlin.math.roundToInt
import org.koin.compose.viewmodel.koinViewModel

private enum class HealthScreenState { LOADING, NOT_AVAILABLE, NEEDS_PERMISSION, CONNECTED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    navController: NavController,
    viewModel: HealthViewModel = koinViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val permissionState by viewModel.permissionState.collectAsState()
    val todaySteps by viewModel.todaySteps.collectAsState()
    val stepsHistory by viewModel.stepsHistory.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val heartRateHistory by viewModel.heartRateHistory.collectAsState()
    val latestHeartRate by viewModel.latestHeartRate.collectAsState()
    val sleepHistory by viewModel.sleepHistory.collectAsState()
    val latestSleep by viewModel.latestSleep.collectAsState()
    val showWeightDialog by viewModel.showWeightDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val requestPermissions = rememberHealthPermissionLauncher { granted ->
        if (granted) {
            viewModel.onPermissionsGranted()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(PhosphorIcons.Bold.ArrowLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncHealth() }) {
                        val syncIcon = when {
                            isLoading -> PhosphorIcons.Bold.CloudArrowUp
                            permissionState == HealthPermissionState.DENIED ||
                            permissionState == HealthPermissionState.NOT_AVAILABLE -> PhosphorIcons.Bold.CloudSlash
                            else -> PhosphorIcons.Bold.CloudCheck
                        }
                        val syncTint = when {
                            isLoading -> MaterialTheme.colorScheme.primary
                            permissionState == HealthPermissionState.DENIED ||
                            permissionState == HealthPermissionState.NOT_AVAILABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.primary
                        }
                        AnimatedIcon(
                            imageVector = syncIcon,
                            contentDescription = "Sync",
                            animate = !isLoading && permissionState == HealthPermissionState.GRANTED,
                            tint = syncTint,
                            effect = IconAnimationEffect.PULSE
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // Pick one of four content states. Crossfading between them means that when the user grants
        // permission, the connect story fades out and the live metrics fade in ("story hides,
        // widgets appear") instead of a hard cut.
        val screenState = when {
            isLoading && todaySteps == null && weightHistory.isEmpty() -> HealthScreenState.LOADING
            permissionState == HealthPermissionState.NOT_AVAILABLE -> HealthScreenState.NOT_AVAILABLE
            permissionState == HealthPermissionState.DENIED -> HealthScreenState.NEEDS_PERMISSION
            else -> HealthScreenState.CONNECTED
        }

        Crossfade(
            targetState = screenState,
            animationSpec = tween(400),
            label = "health-content",
            modifier = Modifier.fillMaxSize().padding(padding)
        ) { state ->
            when (state) {
                HealthScreenState.LOADING -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                HealthScreenState.NOT_AVAILABLE -> Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) { HealthNotAvailableCard() }

                HealthScreenState.NEEDS_PERMISSION -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) { HealthConnectPreview(onRequestPermissions = requestPermissions) }

                HealthScreenState.CONNECTED -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Steps card with ring
                    StepsCard(todaySteps = todaySteps, stepsGoal = 10_000L)

                    // Steps last 7 days, collapsed, just total
                    if (stepsHistory.isNotEmpty()) {
                        val weeklyTotal = stepsHistory.sumOf { it.value }.toLong()
                        ExpandableMetricCard(
                            title = "Last 7 Days",
                            icon = PhosphorIcons.Bold.Footprints,
                            iconTint = MaterialTheme.colorScheme.primary,
                            summaryValue = formatCompact(weeklyTotal.toDouble()),
                            initialExpanded = false
                        ) {
                            StepsDetailView(data = stepsHistory)
                        }
                    }

                    // Heart rate, collapsed, just latest avg
                    if (heartRateHistory.isNotEmpty() || latestHeartRate != null) {
                        ExpandableMetricCard(
                            title = "Heart Rate",
                            icon = PhosphorIcons.Bold.Heartbeat,
                            iconTint = Color(0xFFE57373),
                            summaryValue = latestHeartRate?.let { "${it.roundToInt()} bpm" },
                            initialExpanded = false
                        ) {
                            HeartRateDetailView(data = heartRateHistory)
                        }
                    }

                    // Sleep, collapsed, just last night
                    if (sleepHistory.isNotEmpty() || latestSleep != null) {
                        ExpandableMetricCard(
                            title = "Sleep",
                            icon = PhosphorIcons.Bold.Moon,
                            iconTint = Color(0xFF7986CB),
                            summaryValue = latestSleep?.let { formatSleepDuration(it) },
                            initialExpanded = false
                        ) {
                            SleepDetailView(data = sleepHistory)
                        }
                    }

                    // Weight card
                    WeightCard(
                        latestWeight = latestWeight,
                        weightHistory = weightHistory,
                        onAddWeight = { viewModel.showAddWeightDialog() }
                    )

                    // Weight trend, collapsed
                    if (weightHistory.size >= 2) {
                        ExpandableMetricCard(
                            title = "Weight Trend",
                            icon = PhosphorIcons.Bold.Barbell,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            initialExpanded = false
                        ) {
                            WeightDetailView(data = weightHistory)
                        }
                    }
                }
            }
        }
    }

    if (showWeightDialog) {
        ManualWeightDialog(
            onDismiss = { viewModel.dismissWeightDialog() },
            onConfirm = { weight -> viewModel.addManualWeight(weight) }
        )
    }
}
