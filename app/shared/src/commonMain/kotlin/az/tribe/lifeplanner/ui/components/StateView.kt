package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * D12 — the canonical empty / error / loading state. Designed states, never dead ends (P2/P6):
 * friendly, warm, and (where useful) offering a way forward. Token-pure.
 */
@Composable
fun StateView(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    loading: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(LifePlannerDesign.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
    ) {
        if (loading) {
            CircularProgressIndicator(color = MaterialTheme.modernColors.primary)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.modernColors.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.emptyState))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.modernColors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.modernColors.textSecondary,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            AppButton(text = actionLabel, onClick = onAction, modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs))
        }
    }
}
