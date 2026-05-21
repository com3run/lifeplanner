package az.tribe.lifeplanner.ui.goal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachPersona
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sparkle

@Composable
internal fun GeneratingStep(category: GoalCategory) {
    val coach = remember(category) { CoachPersona.getByCategory(category) }
    val bgColor = try { Color(("FF" + coach.avatar.backgroundColor.removePrefix("#")).toLong(16)) }
    catch (_: Exception) { MaterialTheme.colorScheme.primary }
    val accentColor = try { Color(("FF" + coach.avatar.accentColor.removePrefix("#")).toLong(16)) }
    catch (_: Exception) { MaterialTheme.colorScheme.tertiary }

    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(
        initialValue = 0.88f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )
    val dotPhase by infinite.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "dots"
    )
    val dots = "\u2022".repeat(dotPhase.toInt().coerceIn(0, 3)).padEnd(3, ' ')

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(96.dp)
                    .background(
                        Brush.radialGradient(listOf(bgColor, accentColor.copy(alpha = 0.5f))),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(PhosphorIcons.Regular.Sparkle, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Crafting your goal$dots",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Personalising based on your answers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
