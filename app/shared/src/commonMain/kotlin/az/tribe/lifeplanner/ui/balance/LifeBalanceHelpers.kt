package az.tribe.lifeplanner.ui.balance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.tribe.lifeplanner.domain.model.BalanceInsight
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.LifeArea
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.UsersThree

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
internal fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

// ─── Coach Selection Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CoachSelectionSheet(
    insight: BalanceInsight,
    relevantCoaches: List<CoachPersona>,
    onCoachSelected: (String) -> Unit,
    onCouncilSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "Who should you talk to?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "About: ${insight.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "RECOMMENDED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            relevantCoaches.forEach { coach ->
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { onCoachSelected(coach.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (coach.imageUrl != null) {
                                AsyncImage(
                                    model = coach.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(coach.emoji, fontSize = 20.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(coach.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(coach.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "GROUP DISCUSSION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onCouncilSelected() },
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            PhosphorIcons.Regular.UsersThree,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("The Council", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("All coaches discuss together", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

// isDark=false → vivid colors designed for light surfaces
// isDark=true  → Material 200-tonal equivalents, all pass WCAG AA (≥4.5:1) on dark surfaces
internal fun getAreaColor(area: LifeArea, isDark: Boolean = false): Color {
    return if (isDark) {
        when (area) {
            LifeArea.CAREER -> Color(0xFFB39DDB)
            LifeArea.MONEY -> Color(0xFF81C784)
            LifeArea.BODY -> Color(0xFFEF9A9A)
            LifeArea.PEOPLE -> Color(0xFF90CAF9)
            LifeArea.WELLBEING -> Color(0xFFFFCC80)
            LifeArea.PURPOSE -> Color(0xFFCE93D8)
        }
    } else {
        when (area) {
            LifeArea.CAREER -> Color(0xFF5E35B1)
            LifeArea.MONEY -> Color(0xFF43A047)
            LifeArea.BODY -> Color(0xFFE53935)
            LifeArea.PEOPLE -> Color(0xFF1E88E5)
            LifeArea.WELLBEING -> Color(0xFFFF9800)
            LifeArea.PURPOSE -> Color(0xFF8E24AA)
        }
    }
}

internal fun getScoreColor(score: Int): Color = when {
    score >= 70 -> Color(0xFF4CAF50)
    score >= 50 -> Color(0xFF8BC34A)
    score >= 30 -> Color(0xFFFFC107)
    score >= 15 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}
