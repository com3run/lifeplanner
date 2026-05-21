package az.tribe.lifeplanner.ui.coach

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.model.CoachIcons
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CustomCoach

@Composable
internal fun GroupPreviewCard(
    name: String,
    icon: String,
    selectedBuiltinCoaches: List<CoachPersona>,
    selectedCustomCoaches: List<CustomCoach>
) {
    val totalCount = selectedBuiltinCoaches.size + selectedCustomCoaches.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Group Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (totalCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                // Coach avatars
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    items(selectedBuiltinCoaches.take(4)) { coach ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                                .padding(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                ),
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
                                Text(
                                    text = coach.emoji,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    items(selectedCustomCoaches.take(4 - selectedBuiltinCoaches.size.coerceAtMost(4))) { coach ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                                .padding(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coach.icon,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    if (totalCount > 4) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${totalCount - 4}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalCount coaches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun CoachSelectionItem(
    name: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GroupIconPickerBottomSheet(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Choose an Icon",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CoachIcons.PRESETS) { icon ->
                    val isSelected = icon == selectedIcon
                    val backgroundColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )

                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onIconSelected(icon) },
                        shape = CircleShape,
                        color = backgroundColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = icon,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
