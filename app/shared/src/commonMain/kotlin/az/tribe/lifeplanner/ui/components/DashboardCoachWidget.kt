package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.CaretRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.ui.theme.LifePlannerDesign

/**
 * Personal Coach card for HomeScreen, quick access to Luna AI coach.
 */
@Composable
fun PersonalCoachCard(
    lastMessage: String?,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onChatClick),
        cornerRadius = LifePlannerDesign.CornerRadius.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LifePlannerDesign.Padding.standard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        PhosphorIcons.Regular.Brain,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        "Personal Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        lastMessage ?: "Chat with Luna for personalized guidance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                PhosphorIcons.Regular.CaretRight,
                contentDescription = "Open coach",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
