package az.tribe.lifeplanner.ui.journal

import az.tribe.lifeplanner.data.network.AiProxyService
import co.touchlab.kermit.Logger
import az.tribe.lifeplanner.domain.enum.Mood
import az.tribe.lifeplanner.domain.model.Goal
import az.tribe.lifeplanner.domain.model.Habit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Pillar 3, a choice the AI detected the user weighing inside a journal entry. Surfaced later as a
 * gentle "want to log this decision?" nudge (Phase 2); confirmed decisions are the only journal
 * signal allowed to move the Pillar 7 wiring dials. `null` on [AiJournalResult] means the entry
 * described no real fork, just a normal reflection.
 */
data class DetectedDecision(
    val question: String,
    val optionsConsidered: List<String> = emptyList(),
    /** The option the user seems to be leaning toward; may be blank if genuinely undecided. */
    val leaning: String = "",
    val reasoning: String = "",
)

/**
 * The structured output of a single journal-generation call. [title]/[content]/[tags] are the
 * user-facing entry (unchanged behaviour). The remaining fields are *extracted signal* from the
 * same reflection, consumed downstream:
 *   - [detectedDecision] → Pillar 3 Decision Journal (Phase 2)
 *   - [emotionalSignals] → short observations of how the user felt about specific actions/events;
 *     feed the facts store / Coach context only, never the wiring dials (Phase 3)
 *   - [memoryCandidates] → durable facts worth remembering about the user (Phase 3)
 *
 * All extracted fields are optional and default to empty, so an entry with nothing to extract (or a
 * model that ignores them) degrades cleanly to today's title/content/tags behaviour.
 */
data class AiJournalResult(
    val title: String,
    val content: String,
    val tags: List<String>,
    val detectedDecision: DetectedDecision? = null,
    val emotionalSignals: List<String> = emptyList(),
    val memoryCandidates: List<String> = emptyList(),
)

suspend fun generateAiJournalEntry(
    aiProxy: AiProxyService,
    mood: Mood,
    prompt: String,
    userNote: String,
    linkedGoal: Goal?,
    linkedHabit: Habit?
): AiJournalResult? = withContext(Dispatchers.IO) {
    try {
        val goalContext = linkedGoal?.let {
            """
            Linked Goal: "${it.title}"
            - Description: ${it.description.ifBlank { "No description" }}
            - Progress: ${(it.progress ?: 0).toInt()}%
            - Status: ${it.status.name}
            - Category: ${it.category.name.lowercase().replaceFirstChar { c -> c.uppercase() }}
            """.trimIndent()
        } ?: ""

        val habitContext = linkedHabit?.let {
            """
            Linked Habit: "${it.title}"
            - Description: ${it.description.ifBlank { "No description" }}
            - Current streak: ${it.currentStreak} days
            - Total completions: ${it.totalCompletions}
            - Frequency: ${it.frequency.displayName}
            """.trimIndent()
        } ?: ""

        val noteContext = if (userNote.isNotBlank()) {
            "Additional context from user: \"$userNote\""
        } else ""

        val aiPrompt = """
You are a personal journaling assistant helping someone write a journal entry.

User's current mood: ${mood.displayName} (${mood.emoji})
${if (prompt.isNotBlank()) "The user wants to reflect on: \"$prompt\"" else ""}
$goalContext
$habitContext
$noteContext

Generate a thoughtful journal entry with:
1. A concise, meaningful title (3-6 words) that captures the essence of the reflection
2. A personal, first-person journal entry (2-3 paragraphs) that:
   - Acknowledges the user's current mood authentically
   - If a prompt is given, uses it as the basis for reflection
   - If a goal is linked, connects the reflection to their progress or feelings about it
   - If a habit is linked, mentions how it relates to their journey
   - Is warm, honest, and introspective
   - Feels personal and genuine, not generic
3. 2-4 relevant tags (single words, no hashtags) that categorize this entry

Then, WITHOUT inventing anything the entry does not support, extract structured signal from what
the user actually wrote. It is correct and expected to leave these empty when the entry does not
support them, do not force them:
4. "detectedDecision": ONLY if the reflection genuinely describes the user weighing a choice,
   considering options, or facing a fork (e.g. "should I keep going or quit", "torn between X and
   Y"). Give the decision as a question, the options considered, which option they seem to be
   leaning toward (blank if truly undecided), and their stated reasoning. If the entry is just a
   normal reflection with no real decision, OMIT this field entirely (null).
5. "emotionalSignals": short, factual observations of how the user felt about a SPECIFIC action or
   event they mention (e.g. "Felt relieved after skipping the gym", "Frustrated at slow progress on
   the thesis"). These describe feelings tied to actions, not a diagnosis of the person. Empty if
   none are clearly expressed.
6. "memoryCandidates": durable facts worth remembering about this user for future personalization
   (e.g. "Prefers working out in the mornings", "Values time with family over career wins"). Only
   lasting facts, not one-off moods. Empty if nothing durable is expressed.
""".trimIndent()

        val schema = buildJournalSchema()
        val responseText = aiProxy.generateStructuredJson(aiPrompt, schema)
        parseAiJournalResult(responseText)
    } catch (e: Exception) {
        Logger.e("JournalAiHelper") { "AI journal entry generation failed: ${e.message}" }
        null
    }
}

/** The structured-output schema for a journal generation call. Pure, so it is easy to reason about. */
fun buildJournalSchema() = buildJsonObject {
    put("type", "object")
    putJsonObject("properties") {
        putJsonObject("title") { put("type", "string") }
        putJsonObject("content") { put("type", "string") }
        putJsonObject("tags") {
            put("type", "array")
            putJsonObject("items") { put("type", "string") }
        }
        putJsonObject("detectedDecision") {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("question") { put("type", "string") }
                putJsonObject("optionsConsidered") {
                    put("type", "array")
                    putJsonObject("items") { put("type", "string") }
                }
                putJsonObject("leaning") { put("type", "string") }
                putJsonObject("reasoning") { put("type", "string") }
            }
        }
        putJsonObject("emotionalSignals") {
            put("type", "array")
            putJsonObject("items") { put("type", "string") }
        }
        putJsonObject("memoryCandidates") {
            put("type", "array")
            putJsonObject("items") { put("type", "string") }
        }
    }
    putJsonArray("required") {
        add(JsonPrimitive("title"))
        add(JsonPrimitive("content"))
        add(JsonPrimitive("tags"))
    }
}

/**
 * Parse a structured-JSON journal response into an [AiJournalResult]. Pure and network-free so it is
 * unit-testable. Returns `null` only when the two essential fields (title, content) are missing; all
 * extracted fields degrade to null/empty rather than failing the whole entry.
 */
fun parseAiJournalResult(responseText: String): AiJournalResult? {
    val json = Json { ignoreUnknownKeys = true }
    val root = runCatching { json.parseToJsonElement(responseText).jsonObject }.getOrNull() ?: return null

    val title = root["title"]?.jsonPrimitive?.contentOrNull ?: return null
    val content = root["content"]?.jsonPrimitive?.contentOrNull ?: return null
    val tags = root["tags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

    val detectedDecision = root["detectedDecision"]
        ?.let { runCatching { it.jsonObject }.getOrNull() }
        ?.let { obj ->
            val question = obj["question"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            // A decision with no question is meaningless, treat it as "no decision".
            question?.let {
                DetectedDecision(
                    question = it,
                    optionsConsidered = obj["optionsConsidered"]?.jsonArray
                        ?.mapNotNull { o -> o.jsonPrimitive.contentOrNull } ?: emptyList(),
                    leaning = obj["leaning"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                    reasoning = obj["reasoning"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                )
            }
        }

    val emotionalSignals = root["emotionalSignals"]?.jsonArray
        ?.mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf { s -> s.isNotBlank() } } ?: emptyList()
    val memoryCandidates = root["memoryCandidates"]?.jsonArray
        ?.mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf { s -> s.isNotBlank() } } ?: emptyList()

    return AiJournalResult(
        title = title,
        content = content,
        tags = tags,
        detectedDecision = detectedDecision,
        emotionalSignals = emotionalSignals,
        memoryCandidates = memoryCandidates,
    )
}
