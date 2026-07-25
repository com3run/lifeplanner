package az.tribe.lifeplanner.ui.foryou

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.ui.components.AppButton
import az.tribe.lifeplanner.ui.components.AppButtonVariant
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign
import az.tribe.lifeplanner.ui.theme.modernColors
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft

/**
 * The full Learn lesson behind a "For You" knowledge card. Expands the one-line teaser into the real
 * lesson: a short read, a "try this" takeaway, honest attribution, and two ways to act on it, turn
 * the idea into a habit or a goal. Shared across Android and iOS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeDetailScreen(
    bit: KnowledgeBit,
    onBackClick: () -> Unit,
    onStartHabit: () -> Unit,
    onSetGoal: () -> Unit,
) {
    val c = MaterialTheme.modernColors
    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Learn", fontWeight = FontWeight.Bold) },
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
                top = padding.calculateTopPadding() + LifePlannerDesign.Spacing.sm,
                bottom = padding.calculateBottomPadding() + 96.dp,
                start = LifePlannerDesign.Padding.screenHorizontal,
                end = LifePlannerDesign.Padding.screenHorizontal,
            ),
            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.md),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm)) {
                    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        Surface(
                            color = c.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.medium),
                            modifier = Modifier.fillMaxSize(),
                        ) {}
                        Text(bit.emoji, style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        "LEARN · ${bit.readMin} MIN READ",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.primary,
                    )
                    Text(
                        bit.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.textPrimary,
                    )
                }
            }

            val paragraphs = bit.detail.ifEmpty { listOf(bit.body) }
            items(paragraphs, key = { it }) { paragraph ->
                Text(paragraph, style = MaterialTheme.typography.bodyLarge, color = c.textSecondary)
            }

            if (bit.takeaway.isNotBlank()) {
                item(key = "takeaway") {
                    Surface(
                        color = c.primary.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(LifePlannerDesign.CornerRadius.large),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(LifePlannerDesign.Padding.cardContent),
                            verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.xs),
                        ) {
                            Text(
                                "TRY THIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = c.primary,
                            )
                            Text(bit.takeaway, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary)
                        }
                    }
                }
            }

            bit.source?.let { src ->
                item(key = "source") {
                    Text(
                        "Source: $src",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textTertiary,
                    )
                }
            }

            item(key = "actions") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(LifePlannerDesign.Spacing.sm),
                    modifier = Modifier.padding(top = LifePlannerDesign.Spacing.sm),
                ) {
                    AppButton(
                        text = "Turn this into a habit",
                        onClick = onStartHabit,
                        variant = AppButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppButton(
                        text = "Set a goal around it",
                        onClick = onSetGoal,
                        variant = AppButtonVariant.SECONDARY,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
