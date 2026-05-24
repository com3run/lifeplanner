package az.tribe.lifeplanner.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay

private const val LUNA_INTRO_VIDEO_URL =
    "https://rkdggdfabwgukspylybu.supabase.co/storage/v1/object/public/assets/coaches/luna-clip.mp4"

private const val INTRO_TEXT =
    "Hi, I'm Luna, your personal life coach.\n\nBefore we dive in, I'd love to get to know you a little. It'll only take 2 minutes."

private val IndigoAccent = Color(0xFF6366F1)
private val IndigoLight = Color(0xFF818CF8)

@Composable
internal actual fun LunaVideoIntro(onContinue: () -> Unit) {
    val playerHost = remember { MediaPlayerHost(isMuted = false) }
    var displayedText by remember { mutableStateOf("") }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Logger.d("LunaVideoIntro") { "Loading video URL: $LUNA_INTRO_VIDEO_URL" }
        playerHost.loadUrl(LUNA_INTRO_VIDEO_URL)
    }

    LaunchedEffect(Unit) {
        delay(700L)
        INTRO_TEXT.forEach { char ->
            displayedText += char
            delay(if (char == '\n') 200L else 30L)
        }
        delay(350L)
        showButton = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                showControls = false,
                isPauseResumeEnabled = false,
                isSeekBarVisible = false,
                isDurationVisible = false,
                isMuteControlEnabled = false,
                isAutoHideControlEnabled = false,
                isFastForwardBackwardEnabled = false,
                isFullScreenEnabled = false,
                isSpeedControlEnabled = false,
                isScreenResizeEnabled = false,
                isGestureVolumeControlEnabled = false,
                isZoomEnabled = false,
                loadingIndicatorColor = IndigoAccent,
                loaderView = {}
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 32.dp)
        ) {
            Column {
                Text(
                    text = "✨  Luna",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = IndigoLight
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = displayedText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(Modifier.height(28.dp))
                AnimatedVisibility(
                    visible = showButton,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoAccent)
                    ) {
                        Text(
                            text = "Let's do it!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
