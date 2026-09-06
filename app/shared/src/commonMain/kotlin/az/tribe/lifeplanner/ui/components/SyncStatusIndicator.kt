package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Cloud
import com.adamglin.phosphoricons.regular.CloudCheck
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.WarningCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.data.sync.SyncState
import az.tribe.lifeplanner.data.sync.SyncStatus
import kotlinx.coroutines.flow.StateFlow
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_all_data_synced
import leanlifeplanner.app.shared.generated.resources.cd_connected
import leanlifeplanner.app.shared.generated.resources.cd_no_internet_connection
import leanlifeplanner.app.shared.generated.resources.cd_sync_failed_tap_to_retry
import leanlifeplanner.app.shared.generated.resources.cd_syncing_with_cloud
import leanlifeplanner.app.shared.generated.resources.cd_changes_waiting_to_sync
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun SyncStatusIndicator(
    syncStatus: StateFlow<SyncStatus>,
    onRetryClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val status by syncStatus.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = status.state,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
        modifier = modifier.padding(horizontal = 4.dp)
    ) { state ->
        when (state) {
            SyncState.SYNCING -> {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.CloudArrowUp,
                    contentDescription = stringResource(Res.string.cd_syncing_with_cloud),
                    modifier = Modifier.size(20.dp).graphicsLayer { this.alpha = alpha },
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            SyncState.SYNCED -> {
                Icon(
                    imageVector = PhosphorIcons.Regular.CloudCheck,
                    contentDescription = stringResource(Res.string.cd_all_data_synced),
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF4CAF50)
                )
            }

            SyncState.OFFLINE -> {
                Icon(
                    imageVector = PhosphorIcons.Regular.CloudSlash,
                    contentDescription = stringResource(Res.string.cd_no_internet_connection),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            SyncState.ERROR -> {
                IconButton(onClick = onRetryClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.WarningCircle,
                        contentDescription = stringResource(Res.string.cd_sync_failed_tap_to_retry),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            SyncState.IDLE -> {
                if (status.pendingChanges > 0) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CloudArrowUp,
                        contentDescription = stringResource(Res.string.cd_changes_waiting_to_sync, status.pendingChanges),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Cloud,
                        contentDescription = stringResource(Res.string.cd_connected),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
