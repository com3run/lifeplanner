package az.tribe.lifeplanner.data.repository

import kotlinx.serialization.Serializable

// ============================================================================
// REQUEST DATA CLASSES - What we send to Gemini
// ============================================================================

@Serializable
data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 2048,
    val topP: Double = 0.9,
    val topK: Int = 40,
    val responseMimeType: String = "application/json",
    val responseSchema: ResponseSchema = ResponseSchema()
)

@Serializable
data class ResponseSchema(
    val type: String = "object",
    val properties: Map<String, SchemaProperty> = emptyMap(),
    val required: List<String> = listOf("messages", "suggestions")
)

@Serializable
data class SchemaProperty(
    val type: String,
    val description: String = "",
    val items: SchemaProperty? = null,
    val properties: Map<String, SchemaProperty>? = null,
    val enum: List<String>? = null,
    val maxItems: Int? = null
)

// ============================================================================
// RESPONSE DATA CLASSES - What Gemini returns
// ============================================================================

@Serializable
data class CoachResponseData(
    val messages: List<String> = emptyList(),
    val suggestions: List<SuggestionData> = emptyList()
)

// Council-specific response format where each message has a coach
@Serializable
data class CouncilResponseData(
    val messages: List<CouncilMessage> = emptyList(),
    val suggestions: List<SuggestionData> = emptyList()
)

@Serializable
data class CouncilMessage(
    val coach: String = "",
    val text: String = ""
)

@Serializable
data class SuggestionData(
    val type: String = "",
    val label: String = "",
    val data: SuggestionPayload = SuggestionPayload()
)

@Serializable
data class SuggestionPayload(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val timeline: String? = null,
    val frequency: String? = null,
    val content: String? = null,
    val mood: String? = null,
    val habitId: String? = null,
    val habitTitle: String? = null,
    val goalId: String? = null,
    val targetCount: Int? = null,
    val targetUnit: String? = null,
    // Milestones for goals
    val milestones: List<MilestoneData> = emptyList(),
    // Question fields
    val question: String? = null,
    val options: List<OptionData> = emptyList(),
    val questionType: String? = null
)

@Serializable
data class MilestoneData(
    val title: String = "",
    val weekOffset: Int = 0
)

@Serializable
data class OptionData(
    val id: String = "",
    val label: String = "",
    val value: String = "",
    val description: String? = null
)
