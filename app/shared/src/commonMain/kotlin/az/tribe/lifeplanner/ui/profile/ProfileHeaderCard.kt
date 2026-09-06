package az.tribe.lifeplanner.ui.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.sync.SyncState
import az.tribe.lifeplanner.data.sync.SyncStatus
import az.tribe.lifeplanner.domain.model.User
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.ui.theme.Motion
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Cloud
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.CloudCheck
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.User
import com.adamglin.phosphoricons.regular.WarningCircle
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_edit_name
import leanlifeplanner.app.shared.generated.resources.cd_sync_status
import androidx.compose.ui.graphics.graphicsLayer

/** Amber for the XP line, the one "trophy" accent this header keeps. */
private val LEVEL_GOLD_DEEP = Color(0xFFF5A623)

/**
 * The You tab's header, written like a page rather than dealt like a player card.
 *
 * The old hero was a sync-tinted gradient with a corner sheen, a 104dp XP ring around the avatar,
 * and the level stated three times on one surface: a badge on the ring, a stat pod, and the bar's
 * end captions. Paper rules, same as goal, habit, and journal detail: the level and its rank are
 * one overline said once, identity is a byline row, and progression is a thin line with the
 * numbers written next to it. The sync cloud stays, because tapping it retries a failed sync;
 * only the gradient it used to recolour is gone.
 */
@Composable
internal fun ProfilePaperHeader(
    user: User?,
    userProgress: UserProgress?,
    syncStatus: SyncStatus,
    onRetrySync: () -> Unit,
    onEditName: () -> Unit = {}
) {
    val isRetryable = syncStatus.state == SyncState.ERROR || syncStatus.state == SyncState.OFFLINE
    val syncIcon = when (syncStatus.state) {
        SyncState.SYNCING -> PhosphorIcons.Regular.CloudArrowUp
        SyncState.SYNCED -> PhosphorIcons.Regular.CloudCheck
        SyncState.OFFLINE -> PhosphorIcons.Regular.CloudSlash
        SyncState.ERROR -> PhosphorIcons.Regular.WarningCircle
        SyncState.IDLE -> if (syncStatus.pendingChanges > 0) PhosphorIcons.Regular.CloudArrowUp
        else PhosphorIcons.Regular.Cloud
    }
    val syncIconColor = when (syncStatus.state) {
        SyncState.SYNCING -> MaterialTheme.colorScheme.onSurfaceVariant
        SyncState.SYNCED -> Color(0xFF2AAF6E)
        SyncState.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
        SyncState.ERROR -> MaterialTheme.colorScheme.error
        SyncState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = if (syncStatus.pendingChanges > 0) 1f else 0.5f
        )
    }
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse)
    )
    val syncIconAlpha = if (syncStatus.state == SyncState.SYNCING) pulseAlpha else 1f

    // Held at 0 for the first frame so the line visibly fills toward the real value rather than
    // snapping to it (animateFloatAsState alone starts *at* its target).
    var levelTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(userProgress?.levelProgress) {
        levelTarget = userProgress?.levelProgress ?: 0f
    }
    val barProgress by animateFloatAsState(
        targetValue = levelTarget,
        animationSpec = tween(Motion.Duration.slow, easing = Motion.emphasized),
        label = "xpBar",
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = userProgress
                    ?.let { "Level ${it.currentLevel} · ${it.title}".uppercase() }
                    ?: "PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                fontWeight = FontWeight.SemiBold,
                color = LEVEL_GOLD_DEEP,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.clickable(enabled = isRetryable) { onRetrySync() }) {
                Icon(
                    syncIcon,
                    contentDescription = stringResource(Res.string.cd_sync_status),
                    modifier = Modifier.size(20.dp).graphicsLayer { this.alpha = syncIconAlpha },
                    tint = syncIconColor
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!user?.selectedSymbol.isNullOrEmpty()) {
                    Text(user.selectedSymbol ?: "", fontSize = 24.sp)
                } else {
                    Icon(
                        PhosphorIcons.Regular.User,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user?.displayName ?: "Guest",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onEditName, modifier = Modifier.size(28.dp)) {
                        Icon(
                            PhosphorIcons.Regular.PencilSimple,
                            contentDescription = stringResource(Res.string.cd_edit_name),
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = user?.email ?: "Guest account",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        userProgress?.let { progress ->
            LinearProgressIndicator(
                progress = { barProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = LEVEL_GOLD_DEEP,
                trackColor = LEVEL_GOLD_DEEP.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = buildString {
                    append("${progress.xpInCurrentLevel} / ${progress.xpForCurrentLevel} XP")
                    append(" · ${progress.xpRemainingForNextLevel} to Level ${progress.currentLevel + 1}")
                    append(" · ${formatCompact(progress.totalXp)} total")
                    if (progress.currentStreak > 0) {
                        append(" · ${progress.currentStreak} day streak")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
