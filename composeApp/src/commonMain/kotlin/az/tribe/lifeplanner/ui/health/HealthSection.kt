package az.tribe.lifeplanner.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.HealthMetric
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Barbell
import com.adamglin.phosphoricons.bold.Footprints
import com.adamglin.phosphoricons.bold.Heartbeat
import com.adamglin.phosphoricons.bold.Moon
import kotlin.math.roundToInt

@Composable
fun HealthSection(
    permissionState: HealthPermissionState,
    todaySteps: Long?,
    stepsHistory: List<HealthMetric>,
    heartRateHistory: List<HealthMetric>,
    latestHeartRate: Double?,
    sleepHistory: List<HealthMetric>,
    latestSleep: Double?,
    weightHistory: List<HealthMetric>,
    latestWeight: Double?,
    onRequestPermissions: () -> Unit,
    onAddWeight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        permissionState == HealthPermissionState.NOT_AVAILABLE -> {}
        permissionState == HealthPermissionState.DENIED -> {
            PermissionDeniedCard(onRequestPermissions = onRequestPermissions)
        }
        else -> {
            Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StepsCard(todaySteps = todaySteps, stepsGoal = 10_000L)

                if (stepsHistory.isNotEmpty()) {
                    ExpandableMetricCard(
                        title = "Last 7 Days",
                        icon = PhosphorIcons.Bold.Footprints,
                        iconTint = MaterialTheme.colorScheme.primary,
                        summaryValue = formatCompact(stepsHistory.sumOf { it.value }),
                        initialExpanded = false
                    ) {
                        StepsDetailView(data = stepsHistory)
                    }
                }

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

                WeightCard(
                    latestWeight = latestWeight,
                    weightHistory = weightHistory,
                    onAddWeight = onAddWeight
                )

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
