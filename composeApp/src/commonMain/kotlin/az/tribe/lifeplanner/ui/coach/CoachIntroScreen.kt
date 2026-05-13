package az.tribe.lifeplanner.ui.coach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.ui.components.VideoBackground
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay

data class CoachIntroConfig(
    val videoUrl: String,
    val mascotImageUrl: String? = null,
    val coachLabel: String,
    val introText: String,
    val actionLabel: String = "Let's go!",
    val accentColor: Color = Color(0xFF6366F1),
    val accentLight: Color = Color(0xFF818CF8),
)

@Composable
fun CoachIntroScreen(config: CoachIntroConfig, onAction: () -> Unit) {
    var mascotIn by remember { mutableStateOf(false) }
    var bubbleIn by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf("") }
    var showButton by remember { mutableStateOf(false) }

    // Mascot slides in from off-screen left with a bouncy spring
    val mascotOffsetX by animateFloatAsState(
        targetValue = if (mascotIn) 0f else -900f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "mascotSlide",
    )

    LaunchedEffect(Unit) {
        delay(250L)
        mascotIn = true
        delay(750L)  // wait for mascot to settle before speech bubble pops
        bubbleIn = true
        delay(150L)
        config.introText.forEach { char ->
            displayedText += char
            delay(if (char == '\n') 160L else 30L)
        }
        delay(350L)
        showButton = true
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ── City / scene video background ────────────────────────────────────
        if (config.videoUrl.isNotBlank()) {
            VideoBackground(urls = listOf(config.videoUrl), modifier = Modifier.fillMaxSize())
        }

        // ── Cinematic dark vignette — heavier at bottom for readability ───────
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.00f to Color.Black.copy(alpha = 0.15f),
                    0.45f to Color.Black.copy(alpha = 0.35f),
                    0.72f to Color.Black.copy(alpha = 0.68f),
                    1.00f to Color.Black.copy(alpha = 0.94f),
                )
            )
        )

        // ── Coach name badge — top-left, street-tag style ────────────────────
        Text(
            text = config.coachLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = config.accentLight,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 20.dp, top = 14.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )

        // ── Mascot + speech bubble stage ─────────────────────────────────────
        Box(Modifier.fillMaxSize()) {

            // Mascot — slides in from the left like a street character walking by
            if (config.mascotImageUrl != null) {
                AsyncImage(
                    model = config.mascotImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(300.dp)
                        .widthIn(max = 190.dp)
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp)
                        .graphicsLayer { translationX = mascotOffsetX },
                )
            }

            // Speech bubble — pops in above mascot's head, to the right
            AnimatedVisibility(
                visible = bubbleIn,
                enter = fadeIn() + scaleIn(
                    transformOrigin = TransformOrigin(0.05f, 0.95f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 148.dp, end = 14.dp, bottom = 210.dp),
            ) {
                SpeechBubble(text = displayedText)
            }
        }

        // ── Action button — appears after typewriter finishes ─────────────────
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn() + slideInVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = config.accentColor),
            ) {
                Text(
                    text = config.actionLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}

// ── Comic-style speech bubble with a tail pointing down-left (toward mascot) ──

@Composable
private fun SpeechBubble(text: String) {
    Column {
        Box(
            Modifier
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0D0D0D),
                lineHeight = 22.sp,
            )
        }
        // Triangle tail — tip points down-left toward the mascot
        Canvas(Modifier.size(20.dp, 13.dp)) {
            drawPath(
                Path().apply {
                    moveTo(0f, 0f)           // top-left — flush with bubble bottom-left
                    lineTo(size.width, 0f)   // top-right
                    lineTo(0f, size.height)  // bottom-left — the pointing tip
                    close()
                },
                color = Color.White,
            )
        }
    }
}
