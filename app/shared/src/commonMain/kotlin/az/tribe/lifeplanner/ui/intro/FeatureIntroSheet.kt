package az.tribe.lifeplanner.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Scales
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Target
import com.adamglin.phosphoricons.regular.TrendUp

/**
 * First-touch explanation for a feature: what it is, what you get, what it will ask of you, and one
 * way forward. Shown once before the feature opens, so nobody lands on a screen wondering what it
 * wants from them.
 *
 * Dismissing is not a rejection. [onDismiss] leaves the intro unseen so it can introduce itself
 * again next time, while [onContinue] both marks it seen and opens the feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureIntroSheet(
    intro: FeatureIntro,
    accent: Color,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = LifePlannerDesign.Spacing.xl)
                .padding(top = LifePlannerDesign.Spacing.xl, bottom = LifePlannerDesign.Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(LifePlannerDesign.CornerRadius.large))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    intro.icon.vector(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(LifePlannerDesign.IconSize.large),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xxs),
            ) {
                Text(
                    intro.eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                    color = accent,
                )
                Text(
                    intro.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = c.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    intro.whatItIs,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(top = LifePlannerDesign.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
            ) {
                intro.benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            benefit.icon.vector(),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(LifePlannerDesign.IconSize.medium),
                        )
                        Text(
                            benefit.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = c.cardBackground,
                shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
            ) {
                Row(
                    modifier = Modifier.padding(LifePlannerDesign.Padding.medium),
                    horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        PhosphorIcons.Regular.Lock,
                        contentDescription = null,
                        tint = c.textTertiary,
                        modifier = Modifier.size(LifePlannerDesign.IconSize.extraSmall),
                    )
                    Text(
                        intro.asks,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                    )
                }
            }

            AppButton(
                text = intro.ctaLabel,
                onClick = onContinue,
                variant = AppButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Not now",
                onClick = onDismiss,
                variant = AppButtonVariant.TERTIARY,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun IntroIcon?.vector(): ImageVector = when (this) {
    IntroIcon.TARGET -> PhosphorIcons.Regular.Target
    IntroIcon.TREND -> PhosphorIcons.Regular.TrendUp
    IntroIcon.EYE -> PhosphorIcons.Regular.Eye
    IntroIcon.CLOCK -> PhosphorIcons.Regular.Clock
    IntroIcon.COMPASS -> PhosphorIcons.Regular.Compass
    IntroIcon.LOCK -> PhosphorIcons.Regular.Lock
    IntroIcon.SCALES -> PhosphorIcons.Regular.Scales
    IntroIcon.CHART -> PhosphorIcons.Regular.ChartBar
    null -> PhosphorIcons.Regular.Sparkle
}
