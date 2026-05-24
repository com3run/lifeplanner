package az.tribe.lifeplanner

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import az.tribe.lifeplanner.ui.goal.GoalViewModel
import az.tribe.lifeplanner.util.ShortcutHelper
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mmk.kmpnotifier.extensions.onCreateOrOnNewIntent
import com.mmk.kmpnotifier.notification.NotifierManager
import android.net.Uri
import co.touchlab.kermit.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    private val supabaseClient: SupabaseClient by inject()

    private var pendingPromoRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Prevent video background from stealing audio focus (e.g. pausing Spotify)
        volumeControlStream = AudioManager.STREAM_NOTIFICATION

        ShortcutHelper.register(this)
        NotifierManager.onCreateOrOnNewIntent(intent)
        handleAuthDeeplink(intent)
        handlePromoDeeplink(intent)
        handleGoalDeeplink(intent)
        handleShortcutDeeplink(intent)

        setContent {
            val systemUiController = rememberSystemUiController()

            val isDark = true


            SideEffect {
                systemUiController.setStatusBarColor(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    darkIcons = !isDark
                )
                systemUiController.setNavigationBarColor(
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    darkIcons = !isDark, navigationBarContrastEnforced = false
                )

            }
            val mainViewModel = koinInject<GoalViewModel>()

            App(viewModel = mainViewModel, promoRoute = pendingPromoRoute)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        NotifierManager.onCreateOrOnNewIntent(intent)
        handleAuthDeeplink(intent)
        handlePromoDeeplink(intent)
        handleGoalDeeplink(intent)
        handleShortcutDeeplink(intent)
    }

    private fun handleShortcutDeeplink(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != "lifeplanner") return
        pendingPromoRoute = when (uri.host) {
            "focus"       -> "focus_setup"
            "habits"      -> "journal_habits"
            "goal_wizard" -> "goal_wizard"
            "ai_chat"     -> "ai_chat"
            else          -> return
        }
    }

    private fun handlePromoDeeplink(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "lifeplanner" && uri.host == "promo") {
            val path = uri.pathSegments.firstOrNull() ?: return
            Logger.d("MainActivity") { "Promo deep link: $uri, path=$path" }
            when (path) {
                "chat" -> pendingPromoRoute = "ai_chat"
            }
        }
    }

    private fun handleGoalDeeplink(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.host == "tribe.az" && uri.pathSegments.contains("goal")) {
            val goalId = uri.lastPathSegment ?: return
            Logger.d("MainActivity") { "Goal deep link detected: $goalId" }
            pendingPromoRoute = "goal_detail/$goalId"
        }
    }

    /**
     * Handle auth deep links from both custom scheme (lifeplanner://auth)
     * and HTTPS universal links (https://tribe.az/lifeplanner/auth/callback).
     * Supabase's handleDeeplinks only recognizes the custom scheme,
     * so rewrite HTTPS URLs to the custom scheme before passing them.
     */
    private fun handleAuthDeeplink(intent: Intent) {
        val uri = intent.data ?: return // No deep link, skip entirely
        Logger.d("MainActivity") { "handleAuthDeeplink: $uri" }

        // Check for error in the callback fragment (e.g. expired magic link)
        val fragment = uri.fragment
        if (fragment != null && fragment.contains("error=")) {
            val params = fragment.split("&").associate {
                val parts = it.split("=", limit = 2)
                parts[0] to (parts.getOrNull(1)?.replace("+", " ") ?: "")
            }
            val errorDesc = params["error_description"] ?: params["error"] ?: "Authentication failed"
            Logger.w("MainActivity") { "Auth callback error: $errorDesc" }
            az.tribe.lifeplanner.ui.viewmodel.AuthViewModel.reportDeepLinkError(errorDesc)
            return
        }

        // Only pass to Supabase if it looks like an auth callback
        if (fragment?.contains("access_token") != true) return

        try {
            if (uri.scheme == "https") {
                val customUri = Uri.parse("lifeplanner://auth#$fragment")
                Logger.d("MainActivity") { "Rewriting to custom scheme: $customUri" }
                val rewrittenIntent = Intent(intent).setData(customUri)
                supabaseClient.handleDeeplinks(rewrittenIntent)
            } else {
                supabaseClient.handleDeeplinks(intent)
            }
        } catch (e: Exception) {
            Logger.e("MainActivity", e) { "Failed to handle auth deeplink: ${e.message}" }
        }
    }
}
