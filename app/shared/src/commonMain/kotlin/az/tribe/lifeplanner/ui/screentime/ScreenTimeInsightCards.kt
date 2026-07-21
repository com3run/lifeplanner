package az.tribe.lifeplanner.ui.screentime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.AppUsagePattern
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarDots
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Sparkle
import kotlinx.coroutines.delay

@Composable
fun OverviewCard(pattern: AppUsagePattern) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(PhosphorIcons.Regular.Lightning, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Text("Your App Patterns", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.Clock, null, modifier = Modifier.size(16.dp)) },
                        label = "Peak time",
                        value = pattern.peakHourLabel(),
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.CalendarDots, null, modifier = Modifier.size(16.dp)) },
                        label = "Sessions/week",
                        value = formatOneDecimal(pattern.sessionsPerWeek),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.ChartBar, null, modifier = Modifier.size(16.dp)) },
                        label = "Avg session",
                        value = "${pattern.sessionAvgMinutes.toLong()} min",
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        icon = { Icon(PhosphorIcons.Regular.CalendarDots, null, modifier = Modifier.size(16.dp)) },
                        label = "Best days",
                        value = pattern.mostActiveDays.take(2).joinToString(", "),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun formatOneDecimal(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 10).toLong()
    return "$intPart.$decPart"
}

@Composable
private fun StatChip(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            icon()
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
        }
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun HourActivityBar(pattern: AppUsagePattern) {
    val maxCount = pattern.mostActiveHours.size.coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Peak Hours", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (pattern.mostActiveHours.isEmpty()) {
                Text("No data yet", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            } else {
                pattern.mostActiveHours.take(5).forEachIndexed { idx, hour ->
                    val weight = (maxCount - idx).toFloat() / maxCount
                    val animProgress by animateFloatAsState(weight, tween(600, idx * 100))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val h = if (hour < 10) "0$hour:00" else "$hour:00"
                        Text(h, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            modifier = Modifier.width(44.dp))
                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            strokeCap = StrokeCap.Round,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureEngagementCard(pattern: AppUsagePattern) {
    val topFeatures = pattern.topFeatures(5)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Where You Spend Time", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (topFeatures.isEmpty()) {
                Text("Explore more screens to see your patterns", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            } else {
                val maxMs = topFeatures.maxOfOrNull { pair -> pair.second }?.coerceAtLeast(1L) ?: 1L
                topFeatures.forEachIndexed { idx, pair ->
                    val screen = pair.first
                    val avgMs = pair.second
                    val fraction = avgMs.toFloat() / maxMs
                    val animProgress by animateFloatAsState(fraction, tween(600, idx * 80))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            screen.replace("_", " ").replaceFirstChar { it.uppercase() }.take(14),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            modifier = Modifier.width(90.dp)
                        )
                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            strokeCap = StrokeCap.Round,
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        )
                        Text(
                            "${avgMs / 1000}s",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }
            }
        }
    }
}

private val dayOrder = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun DayActivityCard(pattern: AppUsagePattern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Most Active Days", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                dayOrder.forEach { day ->
                    val isActive = pattern.mostActiveDays.contains(day)
                    val isTop = pattern.mostActiveDays.firstOrNull() == day
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isTop -> MaterialTheme.colorScheme.primary
                                    isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.take(1),
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isTop -> MaterialTheme.colorScheme.onPrimary
                                isActive -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface.copy(0.4f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(rec: BehaviorRecommendation, index: Int, onClick: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 120L); visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { 30 }) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Regular.Sparkle, null,
                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(rec.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(rec.body, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f), lineHeight = 18.sp)
                    if (rec.actionRoute != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Open →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                // The button was styled but never wired: onClick was passed in and
                                // never called, so tapping "Open →" did nothing.
                                .clickable { onClick(rec.actionRoute) }
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPatternCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(PhosphorIcons.Regular.ChartBar, null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            Text("Not enough data yet", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(
                "Use the app for a few more days and your personalized patterns will appear here.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                lineHeight = 20.sp
            )
        }
    }
}
