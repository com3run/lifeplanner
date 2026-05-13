package az.tribe.lifeplanner.data.network

import az.tribe.lifeplanner.BuildKonfig
import az.tribe.lifeplanner.data.repository.BuiltinCoachStore
import az.tribe.lifeplanner.database.CachedPersonaEntity
import az.tribe.lifeplanner.domain.enum.GoalCategory
import az.tribe.lifeplanner.domain.model.CoachAvatar
import az.tribe.lifeplanner.domain.model.CoachMedia
import az.tribe.lifeplanner.domain.model.CoachPersona
import az.tribe.lifeplanner.domain.model.CoachProfile
import az.tribe.lifeplanner.infrastructure.SharedDatabase
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import kotlin.time.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

// ── Asset URL helpers ────────────────────────────────────────────────────────

private const val ASSET_BASE =
    "https://jimiwruckhlxkmnduhrn.supabase.co/storage/v1/object/public/assets/"

private fun avatarUrl(slug: String) = "${ASSET_BASE}coaches/$slug.png"
private fun posterUrl(slug: String) = "${ASSET_BASE}coaches/$slug-poster.jpg"
private fun clipUrl(slug: String) = "${ASSET_BASE}coaches/$slug-clip.mp4"

// ── TribeBot raw DTO (persona-api) ──────────────────────────────────────────

@Serializable
private data class TribeBotMediaDto(
    val id: String = "",
    val kind: String = "",       // "video" | "image"
    val format: String = "",     // "reels" | "image"
    val url: String = "",
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val topic: String? = null,
    val caption: String? = null,
)

@Serializable
private data class TribeBotPersonaDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val description: String? = null,
    val style: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("clip_url") val clipUrl: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("is_public") val isPublic: Boolean = true,
    val media: List<TribeBotMediaDto> = emptyList(),
)

@Serializable
private data class TribeBotPersonaListResponse(
    val personas: List<TribeBotPersonaDto>? = null,
    val persona: TribeBotPersonaDto? = null,
    val count: Int? = null,
)

// ── LifePlanner personas_cache DTO (rich, from our own Supabase) ─────────────

@Serializable
private data class PersonasCacheDto(
    val id: String,
    val name: String,
    val slug: String? = null,
    val title: String = "Coach",
    val description: String? = null,
    val style: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val category: String = "WELLBEING",
    val emoji: String = "✨",
    val greeting: String? = null,
    val specialties: List<String>? = null,
    val personality: String? = null,
    @SerialName("avatar_bg_color") val avatarBgColor: String = "#6366F1",
    @SerialName("avatar_accent_color") val avatarAccentColor: String = "#818CF8",
    @SerialName("avatar_icon_name") val avatarIconName: String = "star",
    val bio: String? = null,
    @SerialName("fun_fact") val funFact: String? = null,
    @SerialName("xp_to_unlock") val xpToUnlock: Int = 0,
    @SerialName("is_default_unlocked") val isDefaultUnlocked: Boolean = true,
    val timezone: String? = null,
    val city: String? = null,
    @SerialName("country_flag") val countryFlag: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

// ── Mappers ──────────────────────────────────────────────────────────────────

private fun PersonasCacheDto.toCoachPersona(): CoachPersona {
    val effectiveSlug = slug ?: id
    val resolvedClipUrl = clipUrl(effectiveSlug)
    Logger.d("PersonaApiFetcher") { "Cache coach '$name' (slug=$effectiveSlug): clipUrl=$resolvedClipUrl" }
    return CoachPersona(
        id = id,
        name = name,
        title = title,
        category = GoalCategory.entries.firstOrNull { it.name == category } ?: GoalCategory.WELLBEING,
        emoji = emoji,
        greeting = greeting ?: "Hi! I'm $name. How can I help you today?",
        specialties = specialties ?: emptyList(),
        personality = personality ?: description ?: "",
        avatar = CoachAvatar(
            backgroundColor = avatarBgColor,
            accentColor = avatarAccentColor,
            iconName = avatarIconName,
        ),
        profile = CoachProfile(
            bio = bio ?: description ?: "",
            funFact = funFact ?: "",
            xpToUnlock = xpToUnlock,
            isDefaultUnlocked = isDefaultUnlocked,
        ),
        imageUrl = avatarUrl ?: imageUrl ?: avatarUrl(effectiveSlug),
        posterUrl = posterUrl(effectiveSlug),
        clipUrl = resolvedClipUrl,
        timezone = timezone ?: "UTC",
        city = city ?: "",
        countryFlag = countryFlag ?: "",
    )
}

private fun TribeBotMediaDto.toCoachMedia() = CoachMedia(
    id = id,
    kind = kind,
    url = url,
    thumbnailUrl = thumbnailUrl,
    topic = topic ?: "",
    caption = caption ?: "",
)

private fun TribeBotPersonaDto.toCoachPersona(): CoachPersona {
    val effectiveSlug = slug ?: id
    val clipMedia = media.firstOrNull { it.kind == "video" && it.format == "reels" }
    // Top-level clip_url / video_url take priority; fallback to first reel in media[]
    val resolvedClipUrl = clipUrl ?: videoUrl ?: clipMedia?.url
    val resolvedPosterUrl = clipMedia?.thumbnailUrl ?: posterUrl ?: posterUrl(effectiveSlug)
    // Skip format="reels" images — they are slide thumbnails for video reels, not standalone content
    val coachMedia = media
        .filter { !(it.kind == "image" && it.format == "reels") }
        .map { it.toCoachMedia() }
    Logger.d("PersonaApiFetcher") { "TribeBot coach '$name' (slug=$effectiveSlug): clipUrl=$resolvedClipUrl media=${coachMedia.size}" }
    return CoachPersona(
        id = id,
        name = name,
        title = "Coach",
        category = GoalCategory.WELLBEING,
        emoji = "✨",
        greeting = "Hi! I'm $name. How can I help you today?",
        specialties = emptyList(),
        personality = description ?: "",
        avatar = CoachAvatar(
            backgroundColor = "#6366F1",
            accentColor = "#818CF8",
            iconName = "star",
        ),
        profile = CoachProfile(
            bio = description ?: "",
            funFact = "",
            xpToUnlock = 0,
            isDefaultUnlocked = true,
        ),
        imageUrl = avatarUrl ?: imageUrl ?: avatarUrl(effectiveSlug),
        posterUrl = resolvedPosterUrl,
        clipUrl = resolvedClipUrl,
        media = coachMedia,
    )
}

private fun CachedPersonaEntity.toCoachPersona(): CoachPersona {
    val effectiveSlug = slug ?: id
    // No clip_url column in SQLite cache — don't auto-generate a URL that may not exist.
    // mergeMedia() will keep the builtin coach's real clipUrl unchanged when this is null.
    Logger.d("PersonaApiFetcher") { "SQLite coach '$name' (slug=$effectiveSlug): clipUrl=null (no local copy)" }
    return CoachPersona(
        id = id,
        name = name,
        title = title,
        category = GoalCategory.entries.firstOrNull { it.name == category } ?: GoalCategory.WELLBEING,
        emoji = emoji,
        greeting = greeting ?: "Hi! I'm $name. How can I help you today?",
        specialties = specialties?.let {
            runCatching { Json.decodeFromString(ListSerializer(String.serializer()), it) }.getOrDefault(emptyList())
        } ?: emptyList(),
        personality = personality ?: description ?: "",
        avatar = CoachAvatar(
            backgroundColor = avatar_bg_color,
            accentColor = avatar_accent_color,
            iconName = avatar_icon_name,
        ),
        profile = CoachProfile(
            bio = bio ?: description ?: "",
            funFact = fun_fact ?: "",
            xpToUnlock = xp_to_unlock.toInt(),
            isDefaultUnlocked = is_default_unlocked == 1L,
        ),
        imageUrl = avatar_url ?: image_url ?: avatarUrl(effectiveSlug),
        posterUrl = null,
        clipUrl = null,
        timezone = timezone ?: "UTC",
        city = city ?: "",
        countryFlag = country_flag ?: "",
    )
}

// ────────────────────────────────────────────────────────────────────────────

class PersonaApiFetcher(
    private val httpClient: HttpClient,
    private val db: SharedDatabase,
) {
    companion object {
        private const val TRIBEBOT_API_URL =
            "https://jimiwruckhlxkmnduhrn.supabase.co/functions/v1/persona-api" +
            "?includeMedia=true&mediaLimit=24&sort=recent"
        private const val CACHE_TABLE = "personas_cache"
    }

    private val log = Logger.withTag("PersonaApiFetcher")

    /** Merge persisted TribeBot media from local SQLDelight into BuiltinCoachStore. No network needed. */
    suspend fun loadCache() {
        try {
            val cached = db { it.lifePlannerDBQueries.getAllCachedPersonas().executeAsList() }
            val coaches = cached.map { it.toCoachPersona() }
            if (coaches.isNotEmpty()) {
                BuiltinCoachStore.mergeMedia(coaches)
                log.i { "Merged media from ${coaches.size} cached personas" }
            }
        } catch (e: Exception) {
            log.w { "Failed to load cached personas: ${e.message}" }
        }
    }

    /**
     * Fetch personas from LifePlanner's own personas_cache table (populated by TribeBot webhook).
     * Falls back to calling TribeBot API directly if the cache is empty.
     */
    suspend fun fetch() {
        val fetched = fetchFromLifePlannerCache()
        if (fetched) return
        log.i { "personas_cache empty — falling back to TribeBot API" }
        fetchFromTribeBot()
    }

    /** Pull from LifePlanner's global personas_cache with full auth headers. */
    private suspend fun fetchFromLifePlannerCache(): Boolean {
        return try {
            val response = httpClient.get(
                "${BuildKonfig.SUPABASE_URL}/rest/v1/$CACHE_TABLE?is_active=eq.true&select=*"
            ) {
                header("apikey", BuildKonfig.SUPABASE_ANON_KEY)
                header("Authorization", "Bearer ${BuildKonfig.SUPABASE_ANON_KEY}")
                header("x-api-key", BuildKonfig.PERSONA_API_SECRET)
            }
            if (!response.status.isSuccess()) return false
            val rows = response.body<List<PersonasCacheDto>>()

            if (rows.isEmpty()) return false

            val now = Clock.System.now().toString()
            persistToLocal(rows, now)
            val coaches = rows.map { it.toCoachPersona() }
            BuiltinCoachStore.update(coaches)
            log.i { "Fetched ${coaches.size} coaches from personas_cache" }
            true
        } catch (e: Exception) {
            log.w { "Failed to fetch from personas_cache: ${e.message}" }
            false
        }
    }

    /** Direct call to TribeBot API — used as fallback when our cache is empty. */
    private suspend fun fetchFromTribeBot() {
        try {
            val response = httpClient.get(TRIBEBOT_API_URL) {
                header("x-api-key", BuildKonfig.PERSONA_API_SECRET)
            }
            if (!response.status.isSuccess()) {
                log.w { "TribeBot persona-api returned ${response.status}" }
                return
            }
            val body: TribeBotPersonaListResponse = response.body()
            val dtos = body.personas ?: listOfNotNull(body.persona)
            val active = dtos.filter { it.isPublic || it.isActive }
            if (active.isEmpty()) return

            val now = Clock.System.now().toString()
            db { database ->
                active.forEach { p ->
                    database.lifePlannerDBQueries.upsertCachedPersona(
                        id = p.id,
                        name = p.name,
                        description = p.description,
                        style = p.style,
                        image_url = p.imageUrl,
                        is_active = if (p.isPublic || p.isActive) 1L else 0L,
                        fetched_at = now,
                        title = "Coach",
                        category = "WELLBEING",
                        emoji = "✨",
                        greeting = "Hi! I'm ${p.name}. How can I help you today?",
                        specialties = null,
                        personality = p.description,
                        avatar_bg_color = "#6366F1",
                        avatar_accent_color = "#818CF8",
                        avatar_icon_name = "star",
                        bio = p.description,
                        fun_fact = null,
                        xp_to_unlock = 0L,
                        is_default_unlocked = 1L,
                        timezone = null,
                        city = null,
                        country_flag = null,
                        slug = p.slug,
                        avatar_url = p.avatarUrl ?: p.imageUrl,
                    )
                }
            }
            // Merge TribeBot media into existing builtin coaches by name
            val tribeBotCoaches = active.map { it.toCoachPersona() }
            BuiltinCoachStore.mergeMedia(tribeBotCoaches)
            log.i { "Merged media from ${tribeBotCoaches.size} TribeBot personas" }
        } catch (e: Exception) {
            log.w { "Failed to fetch from TribeBot: ${e.message}" }
        }
    }

    private suspend fun persistToLocal(rows: List<PersonasCacheDto>, now: String) {
        try {
            db { database ->
                rows.forEach { p ->
                    database.lifePlannerDBQueries.upsertCachedPersona(
                        id = p.id,
                        name = p.name,
                        description = p.description,
                        style = p.style,
                        image_url = p.imageUrl,
                        is_active = if (p.isActive) 1L else 0L,
                        fetched_at = now,
                        title = p.title,
                        category = p.category,
                        emoji = p.emoji,
                        greeting = p.greeting,
                        specialties = p.specialties
                            ?.let { Json.encodeToString(ListSerializer(String.serializer()), it) },
                        personality = p.personality,
                        avatar_bg_color = p.avatarBgColor,
                        avatar_accent_color = p.avatarAccentColor,
                        avatar_icon_name = p.avatarIconName,
                        bio = p.bio,
                        fun_fact = p.funFact,
                        xp_to_unlock = p.xpToUnlock.toLong(),
                        is_default_unlocked = if (p.isDefaultUnlocked) 1L else 0L,
                        timezone = p.timezone,
                        city = p.city,
                        country_flag = p.countryFlag,
                        slug = p.slug,
                        avatar_url = p.avatarUrl,
                    )
                }
            }
        } catch (e: Exception) {
            log.w { "Failed to persist personas to local DB: ${e.message}" }
        }
    }
}
