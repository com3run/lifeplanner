package az.tribe.lifeplanner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.GoalDependency
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.GitBranch

// ============== GOAL DEPENDENCY WIDGET ==============

@Composable
fun GoalDependencyWidget(
    dependencies: List<GoalDependency>,
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dependencies.isEmpty()) return

    val goalMap = goals.associateBy { it.id }
    val sourceGoals = dependencies.map { it.sourceGoalId }.distinct()
        .mapNotNull { goalMap[it] }.take(3)
    val targetGoals = dependencies.map { it.targetGoalId }.distinct()
        .mapNotNull { goalMap[it] }.take(3)

    if (sourceGoals.isEmpty() || targetGoals.isEmpty()) return

    val lineAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        lineAlpha.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val la = lineAlpha.value

    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    val mapHeight = 160.dp
    val nodeWidth = 112.dp

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ─────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    PhosphorIcons.Regular.GitBranch,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Goal Dependencies",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${dependencies.size} link${if (dependencies.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Node + line map ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val nodeColPx = nodeWidth.toPx()
                    val lineStartX = nodeColPx
                    val lineEndX = size.width - nodeColPx

                    val srcYs = sourceGoals.mapIndexed { i, _ ->
                        (i + 0.5f) * size.height / sourceGoals.size
                    }
                    val tgtYs = targetGoals.mapIndexed { i, _ ->
                        (i + 0.5f) * size.height / targetGoals.size
                    }

                    dependencies.forEach { dep ->
                        val si = sourceGoals.indexOfFirst { it.id == dep.sourceGoalId }
                        val ti = targetGoals.indexOfFirst { it.id == dep.targetGoalId }
                        if (si < 0 || ti < 0) return@forEach

                        val sy = srcYs[si]
                        val ey = tgtYs[ti]
                        val cx = (lineStartX + lineEndX) / 2f

                        val path = Path().apply {
                            moveTo(lineStartX, sy)
                            cubicTo(cx, sy, cx, ey, lineEndX, ey)
                        }

                        // Soft glow track
                        drawPath(
                            path,
                            outlineVariant.copy(alpha = 0.35f * la),
                            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Main line
                        drawPath(
                            path,
                            primary.copy(alpha = 0.55f * la),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Arrowhead
                        val arrowLen = 6.dp.toPx()
                        drawLine(
                            primary.copy(alpha = 0.8f * la),
                            Offset(lineEndX - arrowLen, ey - arrowLen * 0.55f),
                            Offset(lineEndX, ey),
                            1.5.dp.toPx(), StrokeCap.Round
                        )
                        drawLine(
                            primary.copy(alpha = 0.8f * la),
                            Offset(lineEndX - arrowLen, ey + arrowLen * 0.55f),
                            Offset(lineEndX, ey),
                            1.5.dp.toPx(), StrokeCap.Round
                        )

                        // Terminal dots
                        drawCircle(
                            primary.copy(alpha = 0.25f * la),
                            5.dp.toPx(), Offset(lineStartX, sy)
                        )
                        drawCircle(
                            primary.copy(alpha = la),
                            2.5.dp.toPx(), Offset(lineStartX, sy)
                        )
                        drawCircle(
                            primary.copy(alpha = 0.25f * la),
                            5.dp.toPx(), Offset(lineEndX, ey)
                        )
                        drawCircle(
                            primary.copy(alpha = la),
                            2.5.dp.toPx(), Offset(lineEndX, ey)
                        )
                    }
                }

                // Source nodes (left)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(nodeWidth)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    sourceGoals.forEach { goal ->
                        DependencyNode(
                            title = goal.title,
                            isSource = true,
                            onClick = { onGoalClick(goal) }
                        )
                    }
                }

                // Target nodes (right)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(nodeWidth)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.End
                ) {
                    targetGoals.forEach { goal ->
                        DependencyNode(
                            title = goal.title,
                            isSource = false,
                            onClick = { onGoalClick(goal) }
                        )
                    }
                }
            }

            // ── Legend ─────────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    "Source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Icon(
                    PhosphorIcons.Regular.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
                Text(
                    "Depends on",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun DependencyNode(
    title: String,
    isSource: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSource)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)

    val textColor = if (isSource)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSecondaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (isSource) TextAlign.Start else TextAlign.End
        )
    }
}
