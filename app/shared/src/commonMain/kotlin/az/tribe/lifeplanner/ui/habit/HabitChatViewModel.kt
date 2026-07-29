package az.tribe.lifeplanner.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.data.mapper.createNewHabit
import az.tribe.lifeplanner.data.network.AiProxyService
import az.tribe.lifeplanner.data.network.toUserFacingAiMessage
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.enum.HabitFrequency
import az.tribe.lifeplanner.domain.enum.HabitType
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.service.HabitNumericParser
import az.tribe.lifeplanner.domain.service.SmartReminderManager
import az.tribe.lifeplanner.usecases.habit.CreateHabitUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A friendly, chat-based way to set up habits: the user says what they want in plain language, the
 * coach replies and proposes concrete habits inline, and tapping "Add" creates them (with a
 * duplicate guard so the habit list doesn't get messy). Added habits reflect straight onto the
 * Habits screen. Reuses the existing AI proxy + create-habit pipeline.
 */
class HabitChatViewModel(
    private val aiProxyService: AiProxyService,
    private val createHabitUseCase: CreateHabitUseCase,
    private val habitRepository: HabitRepository,
    private val smartReminderManager: SmartReminderManager,
) : ViewModel() {

    data class Message(
        val key: Long,
        val fromUser: Boolean,
        val text: String,
        val suggestions: List<GeneratedHabit> = emptyList(),
    )

    private var nextKey = 0L
    private fun key() = nextKey++

    private val _messages = MutableStateFlow(
        listOf(
            Message(
                key = key(),
                fromUser = false,
                text = "Tell me what you'd like to build or change, in your own words. For example: " +
                    "\"I want more energy\" or \"read before bed instead of scrolling\". I'll suggest habits you can add with one tap.",
            ),
        ),
    )
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _addedTitles = MutableStateFlow<Set<String>>(emptySet())
    val addedTitles: StateFlow<Set<String>> = _addedTitles.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun send(userText: String) {
        val text = userText.trim()
        if (text.isBlank() || _sending.value) return
        _messages.value = _messages.value + Message(key(), fromUser = true, text = text)
        _sending.value = true
        viewModelScope.launch {
            try {
                val reply = aiProxyService.generateText(
                    prompt = buildTranscriptPrompt(),
                    systemPrompt = SYSTEM_PROMPT,
                )
                val (message, suggestions) = split(reply)
                _messages.value = _messages.value + Message(
                    key = key(),
                    fromUser = false,
                    text = message.ifBlank { "Here are a couple of ideas." },
                    suggestions = suggestions,
                )
            } catch (e: Exception) {
                Logger.e("HabitChat") { "chat failed: ${e.message}" }
                _messages.value = _messages.value + Message(
                    key = key(), fromUser = false, text = e.toUserFacingAiMessage("suggest habits"),
                )
            } finally {
                _sending.value = false
            }
        }
    }

    /** Add a suggested habit, unless the user already has one like it. */
    fun add(habit: GeneratedHabit) {
        if (habit.title in _addedTitles.value) return
        viewModelScope.launch {
            try {
                val existing = runCatching { habitRepository.getAllHabits() }.getOrDefault(emptyList())
                if (existing.any { normalize(it.title) == normalize(habit.title) }) {
                    _addedTitles.value = _addedTitles.value + habit.title
                    _messages.value = _messages.value + Message(
                        key = key(), fromUser = false,
                        text = "You already have \"${habit.title}\" — skipped so your list stays clean.",
                    )
                    return@launch
                }
                val numeric = HabitNumericParser.parse(habit.title) ?: HabitNumericParser.parse(habit.description)
                val newHabit = createNewHabit(
                    title = habit.title,
                    description = habit.description,
                    category = habit.category,
                    frequency = habit.frequency,
                    type = habit.type,
                    targetCount = numeric?.first ?: 1,
                    unit = numeric?.second,
                    reminderTime = habit.suggestedTime,
                )
                createHabitUseCase(newHabit)
                smartReminderManager.syncRemindersForHabit(newHabit)
                _addedTitles.value = _addedTitles.value + habit.title
                _messages.value = _messages.value + Message(
                    key = key(), fromUser = false, text = "Added \"${habit.title}\" ${habit.emoji}. It's on your Habits screen now.",
                )
            } catch (e: Exception) {
                _messages.value = _messages.value + Message(key = key(), fromUser = false, text = "Couldn't add that one: ${e.message}")
            }
        }
    }

    private fun buildTranscriptPrompt(): String = buildString {
        appendLine("Conversation so far:")
        _messages.value.forEach { m ->
            appendLine("${if (m.fromUser) "User" else "Coach"}: ${m.text}")
        }
        appendLine("Coach:")
    }

    /** Split the model reply into the conversational text and any proposed habits JSON block. */
    private fun split(reply: String): Pair<String, List<GeneratedHabit>> {
        val fenceStart = reply.indexOf("```")
        val message = if (fenceStart >= 0) reply.substring(0, fenceStart).trim() else reply.trim()
        val suggestions = parseHabits(reply)
        return message to suggestions
    }

    private fun parseHabits(response: String): List<GeneratedHabit> = try {
        val cleaned = response.replace("```json", "").replace("```", "")
        val start = cleaned.indexOf('[')
        val end = cleaned.lastIndexOf(']')
        if (start == -1 || end == -1 || end <= start) emptyList()
        else json.decodeFromString<List<ChatHabitDto>>(cleaned.substring(start, end + 1)).map { dto ->
            GeneratedHabit(
                title = dto.title,
                description = dto.description,
                category = runCatching { GoalCategory.valueOf(dto.category.uppercase()) }.getOrDefault(GoalCategory.WELLBEING),
                frequency = runCatching { HabitFrequency.valueOf(dto.frequency.uppercase()) }.getOrDefault(HabitFrequency.DAILY),
                type = runCatching { HabitType.valueOf(dto.type.uppercase()) }.getOrDefault(HabitType.BUILD),
                emoji = dto.emoji,
                suggestedTime = dto.suggestedTime.takeIf { it.matches(Regex("""\d{2}:\d{2}""")) } ?: "08:00",
            )
        }
    } catch (e: Exception) {
        Logger.w("HabitChat") { "suggestion parse failed: ${e.message}" }
        emptyList()
    }

    private fun normalize(title: String): String =
        title.lowercase().filter { it.isLetterOrDigit() || it == ' ' }.trim().replace(Regex("\\s+"), " ")

    private companion object {
        val SYSTEM_PROMPT = """
            You are a warm, concise habit coach helping the user set up habits inside an app.
            Reply naturally in 1-2 short sentences. When it helps, propose 1-3 specific, achievable
            habits. If you propose habits, append AFTER your sentence a fenced json block:
            ```json
            [{"title":"short action title","description":"one sentence","category":"BODY|CAREER|MONEY|PEOPLE|WELLBEING|PURPOSE","frequency":"DAILY|WEEKDAYS|WEEKLY","type":"BUILD|BREAK_BAD_HABIT","emoji":"single emoji","suggestedTime":"HH:MM"}]
            ```
            Keep titles short and concrete. Pick suggestedTime sensibly (morning 06:00-09:00, midday
            11:00-13:00, afternoon 15:00-17:00, evening 19:00-22:00). If the user is only chatting or
            is already set, reply without a json block. Never repeat a habit they say they already have.
        """.trimIndent()
    }
}

@Serializable
private data class ChatHabitDto(
    val title: String,
    val description: String = "",
    val category: String = "WELLBEING",
    val frequency: String = "DAILY",
    val type: String = "BUILD",
    val emoji: String = "✅",
    val suggestedTime: String = "08:00",
)
