package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A standardized empty state component for use across the app.
 *
 * @param icon The icon to display
 * @param title The main title text
 * @param subtitle The secondary description text
 * @param actionLabel Optional button label (if null, no button is shown)
 * @param onAction Optional callback when the action button is clicked
 * @param modifier Modifier for the component
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LifePlannerDesign.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = LifePlannerDesign.Alpha.overlay),
            modifier = Modifier.size(LifePlannerDesign.IconSize.emptyState)
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xl))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xl))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small)
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * A compact variant of the empty state for use within cards or smaller areas.
 */
@Composable
fun CompactEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(LifePlannerDesign.Padding.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = LifePlannerDesign.Alpha.overlay),
            modifier = Modifier.size(LifePlannerDesign.IconSize.extraLarge)
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xxs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Illustration-backed variant of [EmptyStateCard], for the primary empty screens
 * where a line-art illustration carries more warmth than a tinted icon.
 *
 * The illustrations ship as a light set in `composeResources/drawable/` and a
 * luminance-inverted set in `composeResources/drawable-dark/`. Compose Resources
 * resolves the right one for the active theme, so callers never branch on it.
 *
 * @param illustration One of the `Res.drawable.illus_*` resources
 */
@Composable
fun EmptyStateCard(
    illustration: DrawableResource,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    illustrationSize: Dp = 160.dp
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(LifePlannerDesign.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(illustration),
            contentDescription = null,
            modifier = Modifier.size(illustrationSize)
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xl))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xl))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small)
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Illustration-backed variant of [CompactEmptyState], for use inside cards.
 */
@Composable
fun CompactEmptyState(
    illustration: DrawableResource,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    illustrationSize: Dp = 96.dp
) {
    Column(
        modifier = modifier.padding(LifePlannerDesign.Padding.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(illustration),
            contentDescription = null,
            modifier = Modifier.size(illustrationSize)
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.md))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(LifePlannerDesign.Spacing.xxs))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
