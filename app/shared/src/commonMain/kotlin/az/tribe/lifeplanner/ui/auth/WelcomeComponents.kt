package az.tribe.lifeplanner.ui.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

internal val glitchChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!?<>/"
internal val neonCyan = Color(0xFF00F0FF)
internal val neonPink = Color(0xFFFF00E5)
internal val neonGreen = Color(0xFF39FF14)

@Composable
internal fun TypewriterHeadline() {
    val fullText = "Design\nYour\nFuture"
    var displayText by remember { mutableStateOf("") }
    var glitchPhase by remember { mutableStateOf(false) }
    var showCursor by remember { mutableStateOf(true) }
    var settled by remember { mutableStateOf(false) }

    // Character-by-character typing with glitch scramble per character
    LaunchedEffect(Unit) {
        delay(600)
        for (i in fullText.indices) {
            val targetChar = fullText[i]
            if (targetChar == '\n') {
                displayText += '\n'
                delay(300)
                continue
            }
            // Scramble 3 random chars before settling
            for (j in 0 until 3) {
                val scrambled = glitchChars[Random.nextInt(glitchChars.length)]
                displayText = fullText.substring(0, i) + scrambled
                delay(40)
            }
            displayText = fullText.substring(0, i + 1)
            delay(60)
        }
        // Glitch flash after complete
        delay(200)
        glitchPhase = true
        delay(100)
        glitchPhase = false
        delay(80)
        glitchPhase = true
        delay(60)
        glitchPhase = false
        settled = true
        // Blink cursor a few times then hide
        repeat(4) {
            delay(500)
            showCursor = !showCursor
        }
        showCursor = false
    }

    // Subtle neon pulse for the settled state
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Glitch horizontal offset
    val glitchOffset = if (glitchPhase) Random.nextInt(-8, 8) else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset { IntOffset(glitchOffset, 0) }
    ) {
        val lines = displayText.split('\n')
        lines.forEachIndexed { lineIndex, line ->
            Row {
                Text(
                    text = buildAnnotatedString {
                        line.forEachIndexed { charIndex, char ->
                            val color = if (settled) {
                                // Gradient effect: cyan → white → pink across the line
                                val ratio = if (line.length > 1) charIndex.toFloat() / (line.length - 1) else 0.5f
                                when {
                                    ratio < 0.3f -> neonCyan.copy(alpha = glowAlpha)
                                    ratio > 0.7f -> neonPink.copy(alpha = glowAlpha)
                                    else -> Color.White
                                }
                            } else if (charIndex == line.length - 1 && !settled) {
                                neonGreen // active typing char
                            } else {
                                Color.White.copy(alpha = 0.9f)
                            }
                            withStyle(SpanStyle(color = color)) {
                                append(char)
                            }
                        }
                        // Blinking cursor
                        if (showCursor && lineIndex == lines.lastIndex) {
                            withStyle(SpanStyle(color = neonCyan)) {
                                append("_")
                            }
                        }
                    },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    letterSpacing = 3.sp,
                    lineHeight = 52.sp
                )
            }
        }
    }
}

@Composable
internal fun InlineErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
