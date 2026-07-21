package az.tribe.lifeplanner.ui.health

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Barbell
import com.adamglin.phosphoricons.bold.Footprints
import com.adamglin.phosphoricons.bold.Heartbeat
import com.adamglin.phosphoricons.bold.Moon

/**
 * Permission ask that looks like the dashboard itself: a compact connect card followed by
 * sample metric widgets, so the user sees exactly what they unlock before granting access.
 * Tapping anywhere (card, button, or any preview tile) launches the permission request.
 */
@Composable
internal fun HealthConnectPreview(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    connecting: Boolean = false,
    compact: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ConnectCard(onRequestPermissions = onRequestPermissions, connecting = connecting)

        Text(
            text = "WHAT YOU'LL SEE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
        )

        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                previewMetrics.forEach { metric ->
                    PreviewMetricTile(
                        metric = metric,
                        onClick = onRequestPermissions,
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                }
            }
        } else {
            previewMetrics.chunked(2).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowMetrics.forEach { metric ->
                        PreviewMetricTile(
                            metric = metric,
                            onClick = onRequestPermissions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectCard(
    onRequestPermissions: () -> Unit,
    connecting: Boolean,
) {
    Card(
        onClick = onRequestPermissions,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Bold.Heartbeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Your health, filled in automatically",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Steps, sleep, heart rate and weight from your phone's health app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onRequestPermissions,
                enabled = !connecting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (connecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (connecting) "Connecting..." else "Connect")
            }
            Text(
                text = "Read only. Your data stays on your device.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private data class PreviewMetric(
    val title: String,
    val sampleValue: String,
    val sampleCaption: String,
    val icon: ImageVector,
    val tint: Color?,
    /** 0..1 fills the sample progress bar; null hides it. */
    val sampleProgress: Float? = null,
)

private val previewMetrics = listOf(
    PreviewMetric("Steps", "7,842", "of 10,000 today", PhosphorIcons.Bold.Footprints, null, sampleProgress = 0.78f),
    PreviewMetric("Sleep", "7h 20m", "last night", PhosphorIcons.Bold.Moon, Color(0xFF7986CB)),
    PreviewMetric("Heart", "68 bpm", "resting avg", PhosphorIcons.Bold.Heartbeat, Color(0xFFE57373)),
    PreviewMetric("Weight", "72.4 kg", "trending down", PhosphorIcons.Bold.Barbell, null),
)

@Composable
private fun PreviewMetricTile(
    metric: PreviewMetric,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val tint = metric.tint ?: MaterialTheme.colorScheme.primary
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 10.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    tint = tint.copy(alpha = 0.8f),
                    modifier = Modifier.size(if (compact) 14.dp else 18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = metric.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = metric.sampleValue,
                style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            if (!compact) {
                Text(
                    text = metric.sampleCaption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                metric.sampleProgress?.let { progress ->
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = tint.copy(alpha = 0.45f),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
