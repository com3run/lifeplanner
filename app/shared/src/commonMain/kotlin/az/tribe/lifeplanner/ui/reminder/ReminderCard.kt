package az.tribe.lifeplanner.ui.reminder

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Reminder
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Trash
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_delete
import leanlifeplanner.app.shared.generated.resources.cd_smart_timing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReminderCard(
    reminder: Reminder,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> { showDeleteDialog = true; false }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                animationSpec = tween(300),
                label = "swipeBg"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 1f else 0.8f,
                animationSpec = tween(300),
                label = "iconScale"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(PhosphorIcons.Regular.Trash, contentDescription = stringResource(Res.string.cd_delete), tint = Color.White, modifier = Modifier.size(28.dp).scale(scale))
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        ReminderCardContent(reminder = reminder, onToggle = onToggle, onClick = onClick)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(PhosphorIcons.Regular.Trash, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)) },
            title = { Text("Delete Reminder?") },
            text = { Text("Are you sure you want to delete \"${reminder.title}\"?") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
internal fun ReminderCardContent(reminder: Reminder, onToggle: () -> Unit, onClick: () -> Unit) {
    val typeColor = getReminderTypeColor(reminder.type)
    val isAuto = reminder.id.startsWith("auto-")

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(typeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = getReminderTypeIcon(reminder.type), contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (reminder.isEnabled) MaterialTheme.modernColors.textPrimary else MaterialTheme.modernColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isAuto) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUTO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                            )
                        }
                    }
                    if (reminder.isSmartTiming) {
                        Spacer(Modifier.width(4.dp))
                        Icon(imageVector = PhosphorIcons.Regular.Sparkle, contentDescription = stringResource(Res.string.cd_smart_timing), modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Spacer(Modifier.height(2.dp))

                if (reminder.message.isNotBlank()) {
                    Text(text = reminder.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.modernColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(PhosphorIcons.Regular.Clock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.modernColors.textSecondary)
                    Text(text = formatTime12h(reminder.scheduledTime), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.modernColors.textSecondary)
                    Text(text = "\u2022", color = MaterialTheme.modernColors.textSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(text = formatFrequency(reminder), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.modernColors.textSecondary)
                }
            }

            Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() }, modifier = Modifier.padding(start = 4.dp))
        }
    }
}
