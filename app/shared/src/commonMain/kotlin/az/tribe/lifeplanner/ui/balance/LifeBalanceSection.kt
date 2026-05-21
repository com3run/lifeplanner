package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.HealthMetric
import az.tribe.lifeplanner.ui.health.HealthPermissionState
import az.tribe.lifeplanner.ui.health.HealthSection
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.PlayCircle

private val hPad = Modifier.padding(horizontal = 16.dp)

fun LazyListScope.lifeBalanceItems(
    uiState: LifeBalanceUiState,
    habitsCompleted: Int,
    totalHabits: Int,
    onOpenStoryReader: () -> Unit,
    onOpenHealthStories: () -> Unit,
    onCoachAction: (String?) -> Unit,
    healthPermissionState: HealthPermissionState,
    todaySteps: Long?,
    stepsHistory: List<HealthMetric>,
    heartRateHistory: List<HealthMetric>,
    latestHeartRate: Double?,
    sleepHistory: List<HealthMetric>,
    latestSleep: Double?,
    weightHistory: List<HealthMetric>,
    latestWeight: Double?,
    onRequestHealthPermissions: () -> Unit,
    onAddWeight: () -> Unit,
) {
    val report = uiState.report

    if (report != null) {
        item(key = "life_rings") {
            LifeProgressRingsCard(
                report = report,
                habitsCompleted = habitsCompleted,
                totalHabits = totalHabits,
                modifier = hPad
            )
        }
        item(key = "life_web_card") { LifeWebCard(report = report, modifier = hPad) }
        item(key = "life_segment_bar") { LifeBalanceSegmentBar(areaScores = report.areaScores, modifier = hPad) }
    }

    item(key = "coach_posts_header") { SectionHeader("From Your Coaches", modifier = hPad) }
    item(key = "coach_posts") {
        val coachStories = remember { getCoachTipStories() }
        CoachPostFeed(
            stories = coachStories,
            onOpenReader = { onOpenStoryReader() },
            onStoryAction = onCoachAction,
            modifier = hPad
        )
    }

    val hasHealthData = healthPermissionState == HealthPermissionState.GRANTED &&
        (todaySteps != null || latestHeartRate != null || latestSleep != null || latestWeight != null)

    item(key = "health_header") {
        Row(
            modifier = hPad.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader("Body & Health")
            if (hasHealthData) {
                IconButton(
                    onClick = onOpenHealthStories,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        PhosphorIcons.Regular.PlayCircle,
                        contentDescription = "View as story",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
    item(key = "health_section") {
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
            onRequestPermissions = onRequestHealthPermissions,
            onAddWeight = onAddWeight,
            modifier = hPad
        )
    }
}
