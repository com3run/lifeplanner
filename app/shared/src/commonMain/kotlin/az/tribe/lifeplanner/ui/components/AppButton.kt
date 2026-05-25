package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors

/**
 * D4, canonical button primitive. The single source of truth for buttons, replacing ~345 ad-hoc
 * `Button`/`OutlinedButton`/`TextButton` call sites. Token-pure: every color/size/shape/type comes
 * from the D3 tokens, never a raw value.
 *
 * States covered (D4 matrix): default · pressed (M3 ripple) · focused (M3) · disabled · loading.
 * (`empty`/`error` are container-level states, not button states, see the D4 spec.)
 */
enum class AppButtonVariant { PRIMARY, SECONDARY, TERTIARY, DESTRUCTIVE }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val c = MaterialTheme.modernColors
    // Foreground color drives both the label and (when busy) the spinner.
    val content = when (variant) {
        AppButtonVariant.PRIMARY -> Color.White
        AppButtonVariant.SECONDARY -> c.onPrimaryContainer
        AppButtonVariant.TERTIARY -> c.primary
        AppButtonVariant.DESTRUCTIVE -> Color.White
    }
    val container = when (variant) {
        AppButtonVariant.PRIMARY -> c.primary
        AppButtonVariant.SECONDARY -> c.primaryContainer
        AppButtonVariant.TERTIARY -> Color.Transparent
        AppButtonVariant.DESTRUCTIVE -> c.error
    }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = LifePlannerDesign.ComponentSize.buttonHeight),
        // Loading implies not-clickable, but keeps the filled look (no "disabled" graying).
        enabled = enabled && !loading,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (variant == AppButtonVariant.TERTIARY) Color.Transparent else c.disabledBackground,
            disabledContentColor = c.disabledContent,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = content,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
                    Spacer(Modifier.width(LifePlannerDesign.Spacing.xs))
                }
                Text(text, style = MaterialTheme.typography.labelLarge, color = content)
            }
        }
    }
}
