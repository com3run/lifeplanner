package az.tribe.lifeplanner.ui.causal

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter

/**
 * TRI-70 — RevenueCat Customer Center: self-service subscription management (view plan,
 * cancel, restore, request refund, etc.), driven by the RevenueCat dashboard config. The
 * `CustomerCenter` composable renders the whole flow; we just host it and handle dismissal.
 */
@Composable
fun CustomerCenterScreen(onBackClick: () -> Unit) {
    CustomerCenter(
        modifier = Modifier.fillMaxSize(),
        onDismiss = onBackClick,
    )
}
