package az.tribe.lifeplanner.ui.components.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.CheckCircle

/**
 * Copy + benefits that describe a connectable capability (Health, Calendar, ...).
 * The visual hero is passed separately so each feature can supply its own animated illustration.
 */
data class ConnectStoryContent(
    /** Small uppercase label above the title, e.g. "HEALTH". */
    val eyebrow: String,
    val title: String,
    /** Informative narrative the user reads before deciding to connect. */
    val story: String,
    /** Short "here's what you get" points, each rendered with a check. */
    val benefits: List<String> = emptyList(),
    val ctaLabel: String = "Connect",
    /** Optional fine print under the CTA (setup caveats, platform notes). */
    val footnote: String? = null,
)

/**
 * A reusable "connect this feature" story: an animated [hero], a short narrative, the benefits of
 * connecting, and a single CTA that kicks off the platform permission flow. When the permission is
 * granted the caller swaps this whole surface out for the feature's live widgets (see the
 * `Crossfade` in `HealthDashboardScreen`), which gives the "story hides, widgets appear" transition.
 *
 * Deliberately self-contained (its own [Card]) so it looks identical whether it is the only thing on
 * a dedicated screen or embedded inside a home/section list.
 */
@Composable
fun FeatureConnectStory(
    content: ConnectStoryContent,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
    connecting: Boolean = false,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    hero: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            hero()

            Spacer(Modifier.height(16.dp))

            Text(
                text = content.eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = accent
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = content.story,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (content.benefits.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    content.benefits.forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                PhosphorIcons.Bold.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = accent
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onConnect,
                enabled = !connecting,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (connecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Connecting…")
                } else {
                    Text(content.ctaLabel)
                }
            }

            if (content.footnote != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = content.footnote,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
