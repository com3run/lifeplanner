package az.tribe.lifeplanner.ui.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import az.tribe.lifeplanner.domain.enum.Mood
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check

@Composable
internal fun ReviewAndSaveStep(
    mood: Mood,
    prompt: String?,
    title: String,
    content: String,
    tags: List<String>,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onTagsChanged: (List<String>) -> Unit,
    onSave: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var tagsText by remember(tags) { mutableStateOf(tags.joinToString(", ")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "${mood.emoji} ${mood.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (prompt != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Title
        item {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChanged,
                label = { Text("Title") },
                placeholder = { Text("Entry title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Content
        item {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChanged,
                label = { Text("Your reflection") },
                placeholder = { Text("Write your thoughts...") },
                minLines = 8,
                maxLines = 15,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Tags
        item {
            OutlinedTextField(
                value = tagsText,
                onValueChange = {
                    tagsText = it
                    onTagsChanged(it.split(",").map { t -> t.trim() }.filter { t -> t.isNotBlank() })
                },
                label = { Text("Tags") },
                placeholder = { Text("gratitude, goals, reflection") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Save button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && content.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(PhosphorIcons.Regular.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
