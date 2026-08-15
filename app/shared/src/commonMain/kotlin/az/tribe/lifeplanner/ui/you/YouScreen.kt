package az.tribe.lifeplanner.ui.you

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.components.GradientHero
import az.tribe.lifeplanner.ui.components.IconChip
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.ThemeController
import az.tribe.lifeplanner.ui.theme.ThemeMode
import az.tribe.lifeplanner.ui.theme.bouncyClickable
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Bell
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ChatCircleText
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.Scales
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Star
import org.koin.compose.koinInject

/**
 * D7, the redesigned **You** canvas (D2). Premium gradient header + grouped cards with colored
 * icon chips, replacing the flat junk-drawer. Delivers G2's user-facing **Appearance** toggle wired
 * live to [ThemeController]. Identity (Becoming/Wiring) + Decisions are honest Pillar 5/7/3 seams.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(
    onBackClick: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val c = MaterialTheme.modernColors
    val themeController: ThemeController = koinInject()
    val themeMode by themeController.mode.collectAsState()

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("You", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(PhosphorIcons.Regular.ArrowLeft, contentDescription = "Back", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.background, titleContentColor = c.textPrimary),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.xs,
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            item {
                GradientHero(
                    eyebrow = "Your space",
                    title = "You",
                    subtitle = "Growth, insights, and who you're becoming.",
                )
            }

            item { SectionLabel("Identity") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.Brain, c.secondary, "Becoming", "Who you're becoming, values & identity") { onNavigate(Screen.Becoming.route) }
                    YouRow(PhosphorIcons.Regular.Sliders, c.primary, "Your Wiring", "How you're wired, your decision profile", last = true) { onNavigate(Screen.YourWiring.route) }
                }
            }

            item { SectionLabel("Insights") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.Scales, c.success, "Wheel of Life", "How each part of life is going") { onNavigate(Screen.WheelOfLife.route) }
                    YouRow(PhosphorIcons.Regular.ChartBar, c.primary, "Analytics", "Your goal & habit statistics") { onNavigate(Screen.Analytics.route) }
                    YouRow(PhosphorIcons.Regular.Sliders, c.secondary, "My Patterns", "How you use the app + tips") { onNavigate(Screen.ScreenTimeInsight.route) }
                    YouRow(PhosphorIcons.Regular.Lightning, c.warning, "Causal Insights", "What actually drives your progress") { onNavigate(Screen.CausalInsights.route) }
                    YouRow(PhosphorIcons.Regular.ClockCounterClockwise, c.accent, "Reviews", "Your periodic retrospective", last = true) { onNavigate(Screen.Retrospective.route) }
                }
            }

            item { SectionLabel("Decisions") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.ClockCounterClockwise, c.primary, "Decision Journal", "Your choices, reasoning & outcomes") { onNavigate(Screen.DecisionJournal.route) }
                    YouRow(PhosphorIcons.Regular.Scales, c.accent, "Review Decisions", "Grade your reasoning, not just outcomes", last = true) { onNavigate(Screen.DecisionReview.route) }
                }
            }

            item { SectionLabel("Growth") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.Star, c.warning, "Abilities", "Skills you're leveling up") { onNavigate(Screen.Abilities.route) }
                    YouRow(PhosphorIcons.Regular.Sparkle, c.accent, "Achievements", "Badges you've earned", last = true) { onNavigate(Screen.Achievements.route) }
                }
            }

            item { SectionLabel("Coach") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.ChatCircleText, c.secondary, "AI Coach", "Talk it through with your coach", last = true) { onNavigate(Screen.AIChat.route) }
                }
            }

            item { SectionLabel("Appearance") }
            item { AppearanceToggle(current = themeMode, onSelect = { themeController.setMode(it) }) }

            item { SectionLabel("Settings") }
            item {
                SectionCard {
                    YouRow(PhosphorIcons.Regular.Bell, c.primary, "Reminders", "Notification preferences") { onNavigate(Screen.Reminders.route) }
                    YouRow(PhosphorIcons.Regular.CloudArrowUp, c.success, "Backup & Sync", "Export and restore your data") { onNavigate(Screen.BackupSettings.route) }
                    YouRow(PhosphorIcons.Regular.ChatCircleText, c.secondary, "Send Feedback", "Report bugs, request features", last = true) { onNavigate(Screen.Feedback.route) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.modernColors.textPrimary,
        modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.modernColors.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.xs)) { content() }
    }
}

@Composable
private fun YouRow(icon: ImageVector, tint: Color, title: String, subtitle: String, last: Boolean = false, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Row(
        Modifier.fillMaxWidth().bouncyClickable(onClick = onClick).padding(horizontal = LifePlannerDesign.Padding.cardContent, vertical = LifePlannerDesign.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconChip(icon, tint = tint, boxSize = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
    }
}

@Composable
private fun AppearanceToggle(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val c = MaterialTheme.modernColors
    Surface(modifier = Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
        ) {
            ThemeMode.entries.forEach { mode ->
                val selected = mode == current
                Surface(
                    modifier = Modifier.weight(1f).clickable { onSelect(mode) },
                    color = if (selected) c.primary else c.surfaceVariant,
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.sm),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            mode.label(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Color.White else c.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
