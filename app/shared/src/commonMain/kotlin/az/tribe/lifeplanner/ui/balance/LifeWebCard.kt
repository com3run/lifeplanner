package az.tribe.lifeplanner.ui.balance

import az.tribe.lifeplanner.ui.theme.LocalIsDarkTheme
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.model.LifeAreaScore
import az.tribe.lifeplanner.domain.model.LifeBalanceReport
import az.tribe.lifeplanner.ui.components.GlassCard
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sparkle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun LifeWebCard(
    report: LifeBalanceReport,
    modifier: Modifier = Modifier
) {
    val aiProxy: AiProxyService = koinInject()
    val scope = rememberCoroutineScope()
    var streamText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    var hasInsight by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current

    val inf = rememberInfiniteTransition(label = "webcard")
    val dotPulse by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dots"
    )

    fun triggerInsight() {
        scope.launch {
            isStreaming = true
            streamText = ""
            aiProxy.chatStream(
                messages = listOf(AiProxyService.ChatMessage("user", buildWebPrompt(report))),
                systemPrompt = "You are a life harmony analyst. Identify powerful cross-area patterns. Be poetic, personal, and specific. 2-3 sentences max. Never use bullet points or lists."
            ).collect { event ->
                when (event) {
                    is AiProxyService.StreamEvent.TextChunk -> streamText += event.text
                    is AiProxyService.StreamEvent.Done -> { isStreaming = false; hasInsight = true }
                    is AiProxyService.StreamEvent.Error -> isStreaming = false
                }
            }
        }
    }

    GlassCard(modifier = modifier, cornerRadius = 20.dp) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header: title + overall score
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Life Constellation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9B85FF)
                    )
                    Text(
                        "Your 6-area energy map",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Overall score pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF7B61FF).copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${report.overallScore}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF9B85FF)
                        )
                        Text(
                            "/ 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9B85FF).copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Web canvas, self-contained with no overlapping siblings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                LifeWebCanvas(
                    areaScores = report.areaScores,
                    modifier = Modifier.size(260.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Area legend: 2-column grid so labels don't crowd
            val sorted = report.areaScores.sortedByDescending { it.score }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sorted.take(3).forEach { areaScore ->
                        AreaLegendChip(areaScore = areaScore, isDark = isDark)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sorted.drop(3).take(3).forEach { areaScore ->
                        AreaLegendChip(areaScore = areaScore, isDark = isDark)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            // AI insight section
            when {
                hasInsight && streamText.isNotEmpty() -> {
                    Text(
                        text = streamText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = { streamText = ""; hasInsight = false; triggerInsight() },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            "Refresh pattern",
                            color = Color(0xFF9B85FF),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                isStreaming -> {
                    if (streamText.isNotEmpty()) {
                        Text(
                            streamText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .alpha(((dotPulse + i * 0.33f) % 1f))
                                    .background(Color(0xFF9B85FF), CircleShape)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Reading your constellation…",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9B85FF).copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { triggerInsight() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF7B61FF).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF9B85FF).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                PhosphorIcons.Regular.Sparkle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF9B85FF)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Reveal Life Pattern",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF9B85FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaLegendChip(areaScore: az.tribe.lifeplanner.domain.model.LifeAreaScore, isDark: Boolean) {
    val color = getAreaColor(areaScore.area, isDark)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Text(
            areaScore.area.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            "${areaScore.score}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 10.sp
        )
    }
}

private fun buildWebPrompt(report: LifeBalanceReport): String = buildString {
    appendLine("Life energy snapshot, overall score ${report.overallScore}/100:")
    report.areaScores.forEach { s ->
        appendLine(
            "  ${s.area.displayName}: ${s.score}/100 · " +
            "${s.activeGoals} active goals · ${s.habitCount} habits · trend ${s.trend.name.lowercase()}"
        )
    }
    if (report.strongestAreas.isNotEmpty()) {
        appendLine("Strongest: ${report.strongestAreas.joinToString { it.displayName }}")
    }
    if (report.weakestAreas.isNotEmpty()) {
        appendLine("Needs attention: ${report.weakestAreas.joinToString { it.displayName }}")
    }
    appendLine()
    appendLine(
        "Find ONE powerful hidden connection between the life areas, a pattern that reveals " +
        "something meaningful about this person's journey right now. " +
        "Start with 'Your' or describe a specific cross-area dynamic. Be personal and specific."
    )
}
