package az.tribe.lifeplanner.ui.foryou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.tribe.lifeplanner.domain.repository.GamificationRepository
import az.tribe.lifeplanner.domain.repository.GoalRepository
import az.tribe.lifeplanner.domain.repository.HabitRepository
import az.tribe.lifeplanner.domain.repository.KnowledgeRepository
import az.tribe.lifeplanner.domain.service.KnowledgeBit
import az.tribe.lifeplanner.domain.service.KnowledgeCollection
import az.tribe.lifeplanner.domain.service.KnowledgeLibrary
import az.tribe.lifeplanner.domain.service.KnowledgeRecommender
import az.tribe.lifeplanner.domain.service.KnowledgeSignals
import az.tribe.lifeplanner.domain.service.KnowledgeTopic
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** One collection's state in the hub, with its own read progress and where to continue. */
data class CollectionUi(
    val collection: KnowledgeCollection,
    val lessons: List<KnowledgeBit>,   // unlocked lessons, in order
    val readCount: Int,
    val total: Int,                    // all lessons filed here, including locked
    val lockedCount: Int,
    val nextUnreadId: String?,         // drives the Continue button
) {
    val progress: Float get() = if (total == 0) 0f else readCount.toFloat() / total
    val isComplete: Boolean get() = readCount == total && total > 0
}

/** The whole Learn hub screen state. */
data class LearnHubUi(
    val loading: Boolean = true,
    val level: Int = 1,
    val levelTitle: String = "",
    val totalXp: Int = 0,
    val readCount: Int = 0,
    val totalUnlocked: Int = 0,
    val recommended: List<KnowledgeBit> = emptyList(),
    val collections: List<CollectionUi> = emptyList(),
    val readIds: Set<String> = emptySet(),
    // "Continue where you left off": the single next lesson to resume, from the path already in
    // progress (else the first not-yet-started path). Null when everything unlocked is read.
    val continueLessonId: String? = null,
    val continueLessonTitle: String? = null,
    val continueReadMin: Int = 0,
    val continuePathTitle: String? = null,
    val continueIsFresh: Boolean = false,
)

/**
 * Backs the Learn hub. Turns the user's activity (habits, goals, level) into topic weights, ranks
 * recommendations against them, and tracks read progress per collection. Recomputes live whenever the
 * set of read lessons changes, so marking one read updates progress and pushes fresh picks forward.
 */
class LearnHubViewModel(
    private val knowledgeRepository: KnowledgeRepository,
    private val gamificationRepository: GamificationRepository,
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LearnHubUi())
    val state: StateFlow<LearnHubUi> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val progress = runCatching { gamificationRepository.getUserProgress().first() }.getOrNull()
            val level = progress?.currentLevel ?: 1
            val levelTitle = progress?.title ?: ""
            val totalXp = progress?.totalXp ?: 0
            val affinity = runCatching { buildAffinity() }.getOrDefault(emptyMap())
            val daySeed = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfYear

            runCatching {
                knowledgeRepository.readIds().collect { readIds ->
                    _state.value = buildUi(level, levelTitle, totalXp, affinity, readIds, daySeed)
                }
            }.onFailure { Logger.w("LearnHubViewModel") { "readIds collect failed: ${it.message}" } }
        }
    }

    private suspend fun buildAffinity(): Map<KnowledgeTopic, Double> {
        val habitCount = runCatching { habitRepository.getAllHabits().size }.getOrDefault(0)
        val goalCount = runCatching { goalRepository.getAllGoals().size }.getOrDefault(0)
        // Every topic starts with a small baseline so nothing is ever fully starved, then the areas
        // the user is active in get boosted so their lessons rise.
        val m = KnowledgeTopic.entries.associateWith { 1.0 }.toMutableMap()
        m[KnowledgeTopic.HABITS] = 1.0 + habitCount.coerceAtMost(8) * 1.5
        m[KnowledgeTopic.GOALS] = 1.0 + goalCount.coerceAtMost(8) * 1.0
        m[KnowledgeTopic.PLANNING] = 1.0 + goalCount.coerceAtMost(8) * 0.5
        m[KnowledgeTopic.MOTIVATION] = 1.0 + if (goalCount > 0) 1.0 else 0.0
        return m
    }

    private fun buildUi(
        level: Int,
        levelTitle: String,
        totalXp: Int,
        affinity: Map<KnowledgeTopic, Double>,
        readIds: Set<String>,
        daySeed: Int,
    ): LearnHubUi {
        val signals = KnowledgeSignals(level = level, readIds = readIds, topicAffinity = affinity, daySeed = daySeed)
        val recommended = KnowledgeRecommender.recommend(signals, count = 3)
        val unlockedAll = KnowledgeLibrary.all.filter { it.minLevel <= level }

        val collections = KnowledgeLibrary.collections.map { c ->
            val all = KnowledgeLibrary.lessonsOf(c)
            val unlocked = all.filter { it.minLevel <= level }
            CollectionUi(
                collection = c,
                lessons = unlocked,
                readCount = unlocked.count { it.id in readIds },
                total = all.size,
                lockedCount = all.size - unlocked.size,
                nextUnreadId = unlocked.firstOrNull { it.id !in readIds }?.id,
            )
        }

        // Prefer the path already in progress; otherwise the first not-yet-started path.
        val inProgress = collections.firstOrNull { it.readCount in 1 until it.total && it.nextUnreadId != null }
        val continueCol = inProgress ?: collections.firstOrNull { it.nextUnreadId != null }
        val continueBit = continueCol?.nextUnreadId?.let { KnowledgeLibrary.byId(it) }

        return LearnHubUi(
            loading = false,
            level = level,
            levelTitle = levelTitle,
            totalXp = totalXp,
            readCount = unlockedAll.count { it.id in readIds },
            totalUnlocked = unlockedAll.size,
            recommended = recommended,
            collections = collections,
            readIds = readIds,
            continueLessonId = continueBit?.id,
            continueLessonTitle = continueBit?.title,
            continueReadMin = continueBit?.readMin ?: 0,
            continuePathTitle = continueCol?.collection?.title,
            continueIsFresh = inProgress == null,
        )
    }
}
