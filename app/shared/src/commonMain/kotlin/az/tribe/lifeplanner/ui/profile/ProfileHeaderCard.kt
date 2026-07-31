package az.tribe.lifeplanner.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.sync.SyncState
import az.tribe.lifeplanner.data.sync.SyncStatus
import az.tribe.lifeplanner.domain.model.User
import az.tribe.lifeplanner.domain.model.UserProgress
import az.tribe.lifeplanner.ui.components.GradientProgressBar
import az.tribe.lifeplanner.ui.components.ProgressRing
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.Motion
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Cloud
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.CloudCheck
import com.adamglin.phosphoricons.regular.CloudSlash
import com.adamglin.phosphoricons.regular.Fire
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.User
import com.adamglin.phosphoricons.regular.WarningCircle

/** Amber used for the level badge and the leading edge of the XP bar, the one "trophy" accent. */
private val LEVEL_GOLD = Color(0xFFFFD479)
private val LEVEL_GOLD_DEEP = Color(0xFFF5A623)
private val LEVEL_GOLD_INK = Color(0xFF4A3200)

/**
 * The You tab's hero: a player card. Identity (avatar, name, rank) and progression (XP ring, level
 * badge, stat pods, XP-to-next-level bar) in one surface, so the most motivating numbers in the app
 * lead the screen instead of sitting in a muted card below it.
 *
 * Motion is reserved for meaning (D10): the ring and bar sweep in to *show* how far along the level
 * is, the XP counts up, and the streak flame only breathes once a streak is actually worth keeping.
 */
@Composable
internal fun PlayerCardHero(
    user: User?,
    userProgress: UserProgress?,
    syncStatus: SyncStatus,
    onRetrySync: () -> Unit,
    onEditName: () -> Unit = {}
) {
    val gradientStart by animateColorAsState(
        targetValue = when (syncStatus.state) {
            SyncState.SYNCED, SyncState.IDLE -> Color(0xFF667EEA)
            SyncState.SYNCING -> Color(0xFF7B8ED0)
            SyncState.ERROR -> Color(0xFF8A7BA0)
            SyncState.OFFLINE -> Color(0xFF7E7E96)
        },
        animationSpec = tween(800)
    )
    val gradientEnd by animateColorAsState(
        targetValue = when (syncStatus.state) {
            SyncState.SYNCED, SyncState.IDLE -> Color(0xFF764BA2)
            SyncState.SYNCING -> Color(0xFF8B6DAF)
            SyncState.ERROR -> Color(0xFF8E6E82)
            SyncState.OFFLINE -> Color(0xFF6E6E82)
        },
        animationSpec = tween(800)
    )

    val isRetryable = syncStatus.state == SyncState.ERROR || syncStatus.state == SyncState.OFFLINE
    val syncIcon = when (syncStatus.state) {
        SyncState.SYNCING -> PhosphorIcons.Regular.CloudArrowUp
        SyncState.SYNCED -> PhosphorIcons.Regular.CloudCheck
        SyncState.OFFLINE -> PhosphorIcons.Regular.CloudSlash
        SyncState.ERROR -> PhosphorIcons.Regular.WarningCircle
        SyncState.IDLE -> if (syncStatus.pendingChanges > 0) PhosphorIcons.Regular.CloudArrowUp else PhosphorIcons.Regular.Cloud
    }
    val syncIconColor = when (syncStatus.state) {
        SyncState.SYNCING -> Color.White
        SyncState.SYNCED -> Color(0xFFA8E6CF)
        SyncState.OFFLINE -> Color(0xFF9A9AAE)
        SyncState.ERROR -> Color(0xFFFFB4A2)
        SyncState.IDLE -> if (syncStatus.pendingChanges > 0) Color(0xFFB89BDB) else Color.White.copy(alpha = 0.4f)
    }
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse)
    )
    val syncIconAlpha = if (syncStatus.state == SyncState.SYNCING) pulseAlpha else 1f

    // Held at 0 for the first frame so the ring and the bar visibly fill toward the real value
    // rather than snapping to it (animate*AsState alone starts *at* its target).
    var levelTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(userProgress?.levelProgress) {
        levelTarget = userProgress?.levelProgress ?: 0f
    }
    val barProgress by animateFloatAsState(
        targetValue = levelTarget,
        animationSpec = tween(Motion.Duration.slow, easing = Motion.emphasized),
        label = "xpBar",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.extraLarge))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
    ) {
        // A single soft light source from the top-left, so the card reads as lit rather than flat.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    // Tight enough to read as a highlight in the corner; a wider one just washes
                    // the gradient out to a flat mauve.
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 440f,
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(enabled = isRetryable) { onRetrySync() }
                .padding(20.dp)
        ) {
            Icon(syncIcon, contentDescription = null, modifier = Modifier.size(20.dp).alpha(syncIconAlpha), tint = syncIconColor)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(
                    progress = levelTarget,
                    diameter = 104.dp,
                    strokeWidth = 6.dp,
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.22f),
                ) {
                    Box(
                        modifier = Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.selectedSymbol.isNullOrEmpty()) {
                            Text(user.selectedSymbol ?: "", fontSize = 40.sp)
                        } else {
                            Icon(PhosphorIcons.Regular.User, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                userProgress?.let { progress ->
                    LevelBadge(level = progress.currentLevel, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(user?.displayName ?: "Guest", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onEditName, modifier = Modifier.size(28.dp)) {
                    Icon(PhosphorIcons.Regular.PencilSimple, contentDescription = "Edit name", modifier = Modifier.size(15.dp), tint = Color.White.copy(alpha = 0.6f))
                }
            }

            val isGuest = user?.email == null
            Text(
                text = user?.email ?: "Guest account",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = if (isGuest) 0.5f else 0.8f)
            )

            userProgress?.let { progress ->
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.2f)) {
                    Text(
                        progress.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val xpAnim = remember { Animatable(0f) }
                    LaunchedEffect(progress.totalXp) {
                        xpAnim.animateTo(progress.totalXp.toFloat(), tween(Motion.Duration.slow, easing = Motion.emphasized))
                    }
                    StatPod(PhosphorIcons.Regular.Lightning, formatCompact(xpAnim.value.toInt()), "Total XP")
                    StatPod(PhosphorIcons.Regular.TrendUp, "${progress.currentLevel}", "Level")
                    // A streak only earns a heartbeat once it's long enough to be worth protecting.
                    StatPod(PhosphorIcons.Regular.Fire, "${progress.currentStreak}", "Streak", pulse = progress.currentStreak >= 3)
                }

                Spacer(Modifier.height(16.dp))

                Column(Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HeroCaption("LVL ${progress.currentLevel}")
                        HeroCaption("LVL ${progress.currentLevel + 1}")
                    }
                    Spacer(Modifier.height(6.dp))
                    GradientProgressBar(
                        progress = barProgress,
                        gradient = Brush.horizontalGradient(listOf(Color.White, LEVEL_GOLD)),
                        modifier = Modifier.fillMaxWidth(),
                        height = 10.dp,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${progress.xpInCurrentLevel} / ${progress.xpForCurrentLevel} XP · ${progress.xpRemainingForNextLevel} to go",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

/** The level number, sitting on the XP ring like a rank insignia. */
@Composable
private fun LevelBadge(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(LEVEL_GOLD, LEVEL_GOLD_DEEP)))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$level",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = LEVEL_GOLD_INK,
        )
    }
}

@Composable
private fun StatPod(icon: ImageVector, value: String, label: String, pulse: Boolean = false) {
    val transition = rememberInfiniteTransition()
    val breathe by transition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = Motion.standard), repeatMode = RepeatMode.Reverse)
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(if (pulse) breathe else 1f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        HeroCaption(label.uppercase())
    }
}

@Composable
private fun HeroCaption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.7f),
    )
}
