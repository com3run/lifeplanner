package az.tribe.lifeplanner.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.Ability
import az.tribe.lifeplanner.domain.model.ChatSession
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.MessageRole
import az.tribe.lifeplanner.domain.model.Milestone
import az.tribe.lifeplanner.domain.service.HabitTrackMode
import az.tribe.lifeplanner.domain.service.durationLabel
import az.tribe.lifeplanner.domain.service.trackMode
import az.tribe.lifeplanner.ui.components.GlassCard
import az.tribe.lifeplanner.ui.components.getIcon
import az.tribe.lifeplanner.ui.habit.HabitWithStatus
import az.tribe.lifeplanner.ui.theme.backgroundColor
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.UserCircle

/** Reusable section header with optional "See all" link. */
@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onSeeAll != null) {
            Surface(onClick = onSeeAll, shape = RoundedCornerShape(50), color = Color.Transparent) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** Reusable navigation card used in Explore section and empty CTAs. */
@Composable
fun HomeNavCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), cornerRadius = 16.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun HeroBanner(
    greeting: String,
    subtitle: String,
    level: Int,
    streak: Int,
    levelTitle: String,
    levelProgress: Float = 0f,
    isSignedIn: Boolean,
    onProfileClick: () -> Unit
) {
    val gradientBg = Brush.linearGradient(
        colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF1E3A8A)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val animRing = remember { Animatable(0f) }
    LaunchedEffect(levelProgress) {
        animRing.animateTo(levelProgress, tween(1100, easing = FastOutSlowInEasing))
    }
    val ringFraction = animRing.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBg)
    ) {
        Box(
            modifier = Modifier.size(160.dp).align(Alignment.TopEnd)
                .background(Brush.radialGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.25f), Color.Transparent)), CircleShape)
        )
        Box(
            modifier = Modifier.size(100.dp).align(Alignment.BottomStart)
                .background(Brush.radialGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.18f), Color.Transparent)), CircleShape)
        )

        Column(modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 18.dp, top = 22.dp, bottom = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(greeting, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 30.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isSignedIn) subtitle else "Sign in to sync & back up your data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val stroke = 5.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = Color.White.copy(alpha = 0.12f),
                            startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize
                        )
                        if (ringFraction > 0.01f) {
                            drawArc(
                                brush = Brush.sweepGradient(listOf(Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFF818CF8))),
                                startAngle = -90f, sweepAngle = 360f * ringFraction, useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$level", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White, lineHeight = 24.sp)
                        Text("LVL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onProfileClick, modifier = Modifier.size(36.dp)) {
                    Icon(PhosphorIcons.Regular.UserCircle, contentDescription = "Profile",
                        tint = if (isSignedIn) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.10f)))
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("🔥", fontSize = 14.sp)
                        Text("$streak", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFBBF24))
                        Text("day streak", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.55f))
                    }
                    Box(Modifier.size(width = 1.dp, height = 14.dp).background(Color.White.copy(alpha = 0.15f)))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("✶", fontSize = 11.sp, color = Color(0xFF818CF8))
                    Text(levelTitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
                }
                if (!isSignedIn) {
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFEF4444).copy(alpha = 0.25f)) {
                        Text("Guest mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactHomeHabitRow(
    habitWithStatus: HabitWithStatus,
    onCheckIn: () -> Unit,
    onIncrement: (() -> Unit)? = null
) {
    val habit = habitWithStatus.habit
    val categoryColor = habit.category.backgroundColor()
    val trackMode = habit.trackMode
    val isCountBased = trackMode == HabitTrackMode.COUNT
    val count = habitWithStatus.todayCount
    val completed = habitWithStatus.isCompletedToday
    val fraction = if (isCountBased && habit.targetCount > 0) count.toFloat() / habit.targetCount else if (completed) 1f else 0f

    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onCheckIn).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(categoryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = habit.category.getIcon(), contentDescription = null, tint = categoryColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (trackMode == HabitTrackMode.DURATION) {
                    Text(
                        text = habit.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (completed) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (habit.currentStreak > 0) {
                    Text(text = "${habit.currentStreak} day streak", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B35))
                }
            }
            if (isCountBased && !completed && onIncrement != null) {
                Surface(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        "$count/${habit.targetCount}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
            } else {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(if (completed) categoryColor.copy(alpha = 0.15f) else Color.Transparent)
                    .border(width = 1.5.dp, color = if (completed) categoryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Text("✓", style = MaterialTheme.typography.labelSmall, color = categoryColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (isCountBased && habit.targetCount > 0) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier.fillMaxWidth().padding(start = 44.dp).height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor.copy(alpha = 0.10f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(categoryColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun CompactHomeMilestoneRow(
    goal: Goal,
    milestone: Milestone,
    focusMinutes: Int = 0,
    onRowClick: () -> Unit,
    onStartFocus: () -> Unit
) {
    val categoryColor = goal.category.backgroundColor()

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onRowClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(categoryColor))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = milestone.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = goal.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (focusMinutes > 0) {
                Text(text = "${focusMinutes}m focused", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF6B35))
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFFF6B35).copy(alpha = 0.15f)).clickable(onClick = onStartFocus),
            contentAlignment = Alignment.Center
        ) {
            Icon(PhosphorIcons.Regular.Play, contentDescription = "Start focus", tint = Color(0xFFFF6B35), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CompactAbilityRow(
    ability: Ability,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(ability.iconEmoji, fontSize = 24.sp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(ability.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                    Text("Lv.${ability.currentLevel}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            LinearProgressIndicator(
                progress = { ability.levelProgress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
        Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun HealthMetricCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ConnectHealthCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEA5455).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(PhosphorIcons.Regular.Heart, contentDescription = null, modifier = Modifier.size(22.dp), tint = Color(0xFFEA5455))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Connect Health", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Track steps, sleep & heart rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HomeCoachAICard(
    session: ChatSession?,
    coach: CoachPersona?,
    coachUnlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), cornerRadius = 16.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(
                    if (coachUnlocked) Color(0xFF00BFA5).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PhosphorIcons.Regular.Brain,
                    contentDescription = null,
                    tint = if (coachUnlocked) Color(0xFF00BFA5) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!coachUnlocked) {
                    Text("Coach AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Unlocks at Level 3", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFF6B35))
                } else if (session != null && coach != null) {
                    Text("Continue with ${coach.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    val lastMsg = session.messages.lastOrNull { it.role == MessageRole.ASSISTANT }
                    Text(
                        lastMsg?.content?.take(60) ?: session.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text("Coach AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Start a conversation with your coach", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun CoachOnboardingDialog(onDismiss: () -> Unit, onSetUpNow: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Meet Your Coaches", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Complete a quick profile setup so Luna and your specialist coach can give you truly personalized guidance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(onClick = onSetUpNow) { Text("Set Up Now") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Maybe Later") }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
