package az.tribe.lifeplanner.ui.journal

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.domain.enum.Mood
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.DotsThreeVertical
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Trash
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryDetailScreen(
    entryId: String,
    viewModel: JournalViewModel = koinViewModel(),
    aiProxy: AiProxyService = koinInject(),
    onBackClick: () -> Unit,
    onNavigateToGoal: (String) -> Unit = {}
) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (entry == null) {
        EntryNotFoundState(onBackClick = onBackClick)
        return
    }

    val moodColor = getMoodColor(entry.mood)
    val animatedMoodColor by animateColorAsState(
        targetValue = moodColor,
        label = "mood_color"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Journal Entry") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.DotsThreeVertical,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(PhosphorIcons.Regular.PencilSimple, contentDescription = null)
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showEditSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        PhosphorIcons.Regular.Trash,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header with Mood
            item {
                JournalEntryHeroHeader(
                    entry = entry,
                    moodColor = animatedMoodColor
                )
            }

            // Content Card
            item {
                JournalContentCard(content = entry.content)
            }

            // Tags Section
            if (entry.tags.isNotEmpty()) {
                item {
                    JournalTagsSection(tags = entry.tags)
                }
            }

            // Prompt Used Section
            if (!entry.promptUsed.isNullOrBlank()) {
                item {
                    PromptUsedCard(prompt = entry.promptUsed)
                }
            }

            // Linked Goal Section
            if (!entry.linkedGoalId.isNullOrBlank()) {
                item {
                    LinkedGoalCard(
                        goalId = entry.linkedGoalId,
                        onClick = { onNavigateToGoal(entry.linkedGoalId) }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteEntryDialog(
            onConfirm = {
                viewModel.deleteEntry(entryId)
                showDeleteDialog = false
                onBackClick()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Edit Bottom Sheet
    if (showEditSheet) {
        EditJournalEntryBottomSheet(
            entry = entry,
            aiProxy = aiProxy,
            onDismiss = { showEditSheet = false },
            onSave = { title, content, mood, tags ->
                viewModel.updateEntry(
                    id = entryId,
                    title = title,
                    content = content,
                    mood = mood,
                    tags = tags
                )
                showEditSheet = false
            }
        )
    }
}

/**
 * Generates content and tags for editing an entry using Gemini structured output
 */
internal suspend fun generateAiContentForEdit(
    aiProxy: AiProxyService,
    mood: Mood,
    userTitle: String,
    prompt: String
): Pair<String, List<String>>? = withContext(Dispatchers.IO) {
    try {
        val aiPrompt = """
You are a personal journaling assistant helping someone write a journal entry.

User's current mood: ${mood.displayName} (${mood.emoji})
Entry title: "$userTitle"
${if (prompt.isNotBlank()) "Original prompt: \"$prompt\"" else ""}

Generate a personal, first-person journal entry (2-3 paragraphs) that:
- Matches the given title
- Reflects the user's current mood authentically
- Is warm, honest, and introspective
- Feels personal and genuine, not generic

Also suggest 2-4 relevant tags (single words, no hashtags).
""".trimIndent()

        val schema = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("content") { put("type", "string") }
                putJsonObject("tags") {
                    put("type", "array")
                    putJsonObject("items") { put("type", "string") }
                }
            }
            putJsonArray("required") {
                add(JsonPrimitive("content"))
                add(JsonPrimitive("tags"))
            }
        }

        val responseText = aiProxy.generateStructuredJson(aiPrompt, schema)

        val json = Json { ignoreUnknownKeys = true }
        val entryJson = json.parseToJsonElement(responseText).jsonObject
        val generatedContent = entryJson["content"]?.jsonPrimitive?.contentOrNull
        val generatedTags = entryJson["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        if (generatedContent != null) {
            Pair(generatedContent, generatedTags)
        } else null
    } catch (e: Exception) {
        Logger.e("JournalEntryDetail") { "AI journal regeneration failed: ${e.message}" }
        null
    }
}

internal fun getMoodColor(mood: Mood): Color {
    return when (mood) {
        Mood.VERY_HAPPY -> Color(0xFF4CAF50)
        Mood.HAPPY -> Color(0xFF8BC34A)
        Mood.NEUTRAL -> Color(0xFFFFC107)
        Mood.SAD -> Color(0xFFFF9800)
        Mood.VERY_SAD -> Color(0xFFF44336)
    }
}
