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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.navigation.Screen
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.ThemeController
import az.tribe.lifeplanner.ui.theme.ThemeMode
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
import com.adamglin.phosphoricons.regular.CloudArrowUp
import com.adamglin.phosphoricons.regular.Scales
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.Sun
import org.koin.compose.koinInject

/**
 * D7 — the redesigned **You** canvas (D2): "who am I becoming, and where's my stuff?". Replaces the
 * old flat Profile junk-drawer with calm, grouped sections, and delivers the **appearance toggle**
 * (the user-facing half of D3/G2) wired live to [ThemeController].
 *
 * Identity (Becoming / Your Wiring) and Decisions are **Pillar 5/7/3 seams** — shown as a single
 * honest "arriving" line until those land on `main`; everything else links real destinations.
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.background,
                    titleContentColor = c.textPrimary,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 84.dp,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        ) {
            // Identity — Pillar 5/7 seam.
            item { SectionLabel("Identity") }
            item {
                InfoCard(
                    PhosphorIcons.Regular.Brain,
                    "Becoming & Your Wiring arrive with the next update — your values, identity, and how you're wired.",
                )
            }

            item { SectionLabel("Insights") }
            item {
                YouCard {
                    YouRow(PhosphorIcons.Regular.Scales, "Life Balance", "How your areas of life are tracking") { onNavigate(Screen.LifeBalance.route) }
                    YouRow(PhosphorIcons.Regular.ChartBar, "Analytics", "Your goal & habit statistics") { onNavigate(Screen.Analytics.route) }
                    YouRow(PhosphorIcons.Regular.Sliders, "My Patterns", "How you use the app + tips") { onNavigate(Screen.ScreenTimeInsight.route) }
                    YouRow(PhosphorIcons.Regular.ClockCounterClockwise, "Reviews", "Your periodic retrospective", last = true) { onNavigate(Screen.Retrospective.route) }
                }
            }

            item { SectionLabel("Growth") }
            item {
                YouCard {
                    YouRow(PhosphorIcons.Regular.Star, "Abilities", "Skills you're leveling up") { onNavigate(Screen.Abilities.route) }
                    YouRow(PhosphorIcons.Regular.Sparkle, "Achievements", "Badges you've earned", last = true) { onNavigate(Screen.Achievements.route) }
                }
            }

            item { SectionLabel("Coach") }
            item {
                YouCard {
                    YouRow(PhosphorIcons.Regular.ChatCircleText, "AI Coach", "Talk it through with your coach", last = true) { onNavigate(Screen.AIChat.route) }
                }
            }

            // Appearance — the user-facing half of G2.
            item { SectionLabel("Appearance") }
            item { AppearanceToggle(current = themeMode, onSelect = { themeController.setMode(it) }) }

            item { SectionLabel("Settings") }
            item {
                YouCard {
                    YouRow(PhosphorIcons.Regular.Bell, "Reminders", "Notification preferences") { onNavigate(Screen.Reminders.route) }
                    YouRow(PhosphorIcons.Regular.CloudArrowUp, "Backup & Sync", "Export and restore your data") { onNavigate(Screen.BackupSettings.route) }
                    YouRow(PhosphorIcons.Regular.ChatCircleText, "Send Feedback", "Report bugs, request features", last = true) { onNavigate(Screen.Feedback.route) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.modernColors.textPrimary,
        modifier = Modifier.padding(top = LifePlannerDesign.Spacing.xs),
    )
}

@Composable
private fun YouCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.modernColors.cardBackground,
        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.xs)) { content() }
    }
}

@Composable
private fun YouRow(icon: ImageVector, title: String, subtitle: String, last: Boolean = false, onClick: () -> Unit) {
    val c = MaterialTheme.modernColors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(LifePlannerDesign.Padding.cardContent),
        horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = c.primary, modifier = Modifier.size(LifePlannerDesign.IconSize.medium))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
        Icon(PhosphorIcons.Regular.CaretRight, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
    }
}

@Composable
private fun InfoCard(icon: ImageVector, text: String) {
    val c = MaterialTheme.modernColors
    Surface(modifier = Modifier.fillMaxWidth(), color = c.cardBackground, shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large)) {
        Row(
            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
            horizontalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = c.textTertiary, modifier = Modifier.size(LifePlannerDesign.IconSize.medium))
            Text(text, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
        }
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
                    color = if (selected) c.primaryContainer else c.surfaceVariant,
                    shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.small),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = LifePlannerDesign.Spacing.sm),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (mode == ThemeMode.SYSTEM) {
                            Icon(PhosphorIcons.Regular.Sun, contentDescription = null, tint = if (selected) c.onPrimaryContainer else c.textSecondary, modifier = Modifier.size(LifePlannerDesign.IconSize.small))
                        }
                        Text(
                            mode.label(),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) c.onPrimaryContainer else c.textSecondary,
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
