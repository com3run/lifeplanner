package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import chaintech.videoplayer.host.MediaPlayerEvent
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable

@Composable
actual fun VideoBackground(urls: List<String>, modifier: Modifier) {
    val validUrls = urls.filter { it.isNotBlank() }
    if (validUrls.isEmpty()) {
        Box(modifier = modifier.background(Color(0xFF121212)))
        return
    }

    val stableUrls = remember(validUrls.size) { validUrls }
    var currentIndex by remember { mutableIntStateOf(0) }
    val playerHost = remember { MediaPlayerHost(isMuted = true) }

    LaunchedEffect(currentIndex, stableUrls) {
        val url = stableUrls[currentIndex % stableUrls.size]
        playerHost.loadUrl(url)
    }

    LaunchedEffect(stableUrls) {
        playerHost.onEvent = { event ->
            if (event is MediaPlayerEvent.MediaEnd) {
                currentIndex = (currentIndex + 1) % stableUrls.size
            }
        }
    }

    VideoPlayerComposable(
        modifier = modifier,
        playerHost = playerHost,
        playerConfig = VideoPlayerConfig(
            showControls = false,
            isPauseResumeEnabled = false,
            isSeekBarVisible = false,
            isDurationVisible = false,
            isMuteControlEnabled = false,
            isAutoHideControlEnabled = true,
            isFastForwardBackwardEnabled = false,
            isFullScreenEnabled = false,
            isSpeedControlEnabled = false,
            isScreenResizeEnabled = false,
            isGestureVolumeControlEnabled = false,
            isZoomEnabled = false,
            loadingIndicatorColor = Color.Transparent,
            loaderView = {}
        )
    )
}
