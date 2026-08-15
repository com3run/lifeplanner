# Spec: Sequences ("Guided bundles → skills")

**Status:** Draft, **deferred / parked for later**. Target: a **future release**, shipped **behind a
feature flag** (`FeatureFlags.SEQUENCES_ENABLED = false`). Not being built now.

**Direction locked (2026-07-30):**
- **v0 = Workout sequences**, then **expand to Yoga**. (Generic "any exercise" is later; the first
  concrete flavor is guided workouts.)
- **Do NOT author exercise content ourselves.** Exercise data (names, target muscles, equipment,
  demo images/GIFs, instructions) comes from a **ready-made third-party exercise API**, cached
  locally. We build the *sequencing + tracking*, not an exercise library.
- **Integration is deferred** — pick and wire the API when we actually start this. See §4a for
  candidates and the (important) **cost** note.

## 1. Summary

A **Sequence** is an ordered, guided bundle of steps the user works through toward one outcome. Two
flavors from the same model:

1. **Workout sequence** (v0 anchor) — an ordered list of exercises sourced from a ready-made API:
   "Full-body Reset = ① 10 pushups → ② 15 squats → ③ 30s plank → ④ 10 lunges," each exercise carrying
   its API-provided demo image, target muscle, and instructions. **Yoga sequences are the next
   expansion** using the same model (yoga poses are just exercises from a yoga-capable source).
2. **Skill path** (the extension the user asked for) — "to earn *Consistent Runner*, do this bundle:
   build the *Run 3x/week* habit, complete the *First 5K* goal, finish the *Form basics* workout."
   Completing the path awards/levels a **skill**.

The key design bet: **the "skill" a path earns is an existing [Ability]** (`domain/model/Ability.kt`,
already models title, emoji, XP, level, and links to habits/goals — currently behind
`ABILITIES_ENABLED`). A Sequence is the *curriculum*; an Ability is the *skill it grows*. So a skill
path is a Sequence whose completion feeds XP into a linked Ability. This avoids inventing a second
progression system and reuses the habit/goal → Ability XP plumbing that already exists.

Sequences live in the **Artifact hub** (new sub-tab or entry point, flag-gated), matching where goals
and habits already are.

## 2. Goals / Non-goals

**Goals (v0, behind flag)**
- Create a sequence: title, emoji, ordered steps.
- Step types: **exercise** (an item picked from the third-party exercise API, with reps/duration),
  **habit** (link an existing or new habit), **goal** (link an existing goal), **milestone** (a
  specific goal milestone).
- **Workout builder**: search/browse the API's exercise catalog (by muscle, equipment) and add
  exercises as steps; each carries its demo image + instructions, cached locally for offline use.
- **Run/track a sequence**: step through it, mark steps done, see progress; a step backed by a
  habit/goal reflects that entity's real completion state (single source of truth).
- **Skill path**: optionally attach a sequence to an Ability; finishing steps grants Ability XP.
- Local-first, offline, **bidirectional sync** like every other entity.
- Ships dark by default; zero runtime cost when the flag is off (dead-code elimination).

**Non-goals (v0)**
- No marketplace / sharing of sequences between users (fast-follow; can reuse Community Journals'
  moderation pattern later).
- No AI auto-generation of sequences in v0 (fast-follow — the chat-habit-creation pattern transfers).
- No rest timers / rep counters / audio coaching for exercise steps (v0 is checklists + a note).
- No scheduling/reminders for sequences (v0 leans on the linked habits' existing reminders).
- No reordering-by-dependency logic; order is just the author's list order.

## 3. Concepts & relationships

```
Sequence 1───* SequenceStep
   │                 step.type ∈ {EXERCISE, HABIT, GOAL, MILESTONE}
   │                 step.refId → Habit.id / Goal.id / Milestone.id   (null for EXERCISE)
   │
   └──0..1 Ability   (skillAbilityId) — set ⇒ this is a "skill path"; completion feeds Ability XP
```

- **Exercise step**: self-contained. "Done" is a per-run checkmark stored on the sequence run.
- **Habit step**: "done for this run" = the linked habit was checked in today (reuses check-in data).
- **Goal / Milestone step**: "done" = that goal/milestone is completed (reuses goal state).
- A Sequence is **complete** when all steps are done. If `skillAbilityId` is set, completion (and
  optionally each step) awards Ability XP via the existing `awardXpToAbilitiesFor*` paths.

**Progress semantics.** Two modes, chosen per sequence:
- **Routine** (exercise-style): resettable. Each "run" is a fresh pass; completing a run can award a
  little XP and increment a run counter/streak. Good for "Morning Reset" you repeat daily.
- **Path** (skill-style): one-way. Steps complete as their real habits/goals complete; the path is a
  progress bar toward a skill, not something you re-run.

## 4. Data model (SQLDelight + sync)

Follows the project's fixed conventions (CLAUDE.md §Database, §Cloud sync). Schema bump **30 → 31**
(+ `.sqm` migration, + Android runtime `migrateToVersion31`, + mappers, + `TableSyncer`s).

**SequenceEntity**
| col | type | notes |
|---|---|---|
| id | TEXT PK | uuid |
| title | TEXT | |
| emoji | TEXT | default "🧩" |
| mode | TEXT | `ROUTINE` \| `PATH` |
| skill_ability_id | TEXT? | FK → AbilityEntity; set ⇒ skill path |
| category | TEXT? | reuse `GoalCategory` for color/grouping |
| is_archived | INTEGER | |
| created_at | TEXT | |
| sync_updated_at, is_deleted, sync_version, last_synced_at | | standard sync columns |

**SequenceStepEntity**
| col | type | notes |
|---|---|---|
| id | TEXT PK | |
| sequence_id | TEXT | FK, cascade |
| order_index | INTEGER | author order |
| type | TEXT | `EXERCISE` \| `HABIT` \| `GOAL` \| `MILESTONE` |
| title | TEXT | shown label (exercise name from API; cache for linked types) |
| ref_id | TEXT? | habit/goal/milestone id (null for EXERCISE) |
| detail | TEXT? | e.g. "10 reps", "30s" (EXERCISE only) |
| api_exercise_id | TEXT? | third-party exercise id (EXERCISE only) |
| image_url | TEXT? | cached demo image/GIF url (EXERCISE only) |
| target_muscle | TEXT? | from API (EXERCISE only) |
| equipment | TEXT? | from API (EXERCISE only) |
| + standard sync columns | | |

We sync only the *reference* + our cached display fields, not the API's full payload. Images are
cached to disk (Coil already in the app) so a saved workout works offline even if the API is down.

**SequenceRunEntity** (ROUTINE mode only — one row per pass)
| col | type | notes |
|---|---|---|
| id, sequence_id, date | | |
| completed_step_ids | TEXT | JSON array of step ids done this run |
| completed_at | TEXT? | set when all steps done |
| + sync columns | | |

PATH mode needs no run table — step doneness derives from the linked entities; only exercise steps in
a PATH (rare) need a persisted checkmark, stored on a single implicit run row.

Sync: three `TableSyncer`s (`Sequence`, `SequenceStep`, `SequenceRun`) + DTOs in `SyncDtos.kt`,
registered in `SyncerFactory`. Supabase tables mirror the columns with RLS `user_id = auth.uid()`,
same as goals/habits.

## 4a. Exercise content source (ready-made API)

We do **not** build an exercise library. Steps of type `EXERCISE` reference a third-party catalog.
Decision deferred until build; candidates and trade-offs:

| Option | Cost | Content | Notes |
|---|---|---|---|
| **wger** (`wger.de` API) | **Free, open-source (AGPL data, CC-BY exercises)** | Strength + some mobility; community-curated; images vary | Self-hostable; no key needed for read; best fit for cost-conscious start |
| **ExerciseDB** (RapidAPI) | Free tier (rate-limited), **paid** above | ~1300 exercises w/ GIFs, muscle, equipment | Great media; RapidAPI key; watch quota/cost |
| **API-Ninjas Exercises** | Free tier, **paid** above | Names + instructions, **no images** | Cheap but no demo media |
| **Yoga**: dedicated pose APIs / datasets | mostly **free/open** | Yoga poses w/ images + Sanskrit names | Slot in for the Yoga expansion; same `EXERCISE` model |

**💰 Cost flag (important):** the richest media (ExerciseDB) sits behind a paid quota. To start free,
lean **wger** (open, no key), and only consider a paid tier if media quality forces it. Any key goes
**server-side via a small edge function** (like `ai-proxy`), never in the client — so we can swap
providers, cache, and rate-limit centrally without shipping a key.

**Integration shape (when we build):** a `WorkoutCatalogService` (Ktor) behind an interface, backed
by an edge function that proxies the chosen API + caches responses. The client only ever talks to our
proxy, mirroring the existing `ai-proxy` pattern. Catalog results are cached locally so browsing and
saved workouts work offline.

## 5. Domain / architecture

- `domain/model/`: `Sequence`, `SequenceStep`, `SequenceStepType`, `SequenceMode`, `SequenceRun`,
  and a derived `SequenceWithProgress` (steps + per-step done state + percent).
- `domain/repository/SequenceRepository` (interface) + `data/repository/SequenceRepositoryImpl`.
- `usecases/sequence/`: `CreateSequenceUseCase`, `ToggleSequenceStepUseCase` (resolves EXERCISE →
  run row, HABIT → check-in, GOAL/MILESTONE → completion), `CompleteSequenceRunUseCase`
  (awards routine XP), `AwardSkillXpForStepUseCase` (path → Ability XP).
- `domain/service/SequenceProgressCalculator` (pure): given a sequence + linked habit/goal state +
  today's run, returns per-step done + overall percent + `isComplete`. **Unit-tested** like
  `GoalOptimizer` — pure, no deps.
- Koin wiring in `appModule.kt`; `viewModel { SequenceViewModel(...) }`, `SequenceDetailViewModel`.

## 6. UX (all flag-gated)

**Entry point.** Artifact hub. Option A (recommended): a **"Sequences" sub-tab** appended to the hub
tab row when `SEQUENCES_ENABLED` (mirrors how the Abilities tab appends under `ABILITIES_ENABLED`).
Option B: a section on the Habits tab. Recommend A for room to grow.

**List screen.** Cards: emoji, title, a compact step-dot row (done/total), mode badge
("Routine" / skill emoji for path). FAB / "+ New sequence".

**Create/edit.** Title + emoji, mode toggle (Routine/Path). Add steps via a picker:
- "Add exercise" → title + optional detail (reps/time).
- "Add habit" → pick existing habit or create one inline (reuse the chat/create-habit flow).
- "Add goal" / "Add milestone" → pick from existing goals.
- Drag to reorder.
For PATH mode, an optional "Earns skill →" selector linking/creating an Ability.

**Run/track screen.** Ordered step list; current step highlighted.
- Exercise step → big check to mark done (haptic + the check-in confirm feel we built for habits).
- Habit step → "Check in" inline (same count-aware + hold behavior as the For You cards).
- Goal/Milestone step → shows state; tapping opens the goal (deliberate completion, like plan rows).
- Header progress: Routine shows "Run N · 🔥 streak"; Path shows a percent bar toward the skill.
- On full completion: the celebration animation + XP toast; Path also bumps the linked Ability's level.

**Reuse, don't reinvent:** the check-in confirm animation, the warm empty-state illustration, the
expand/collapse and card patterns, and (later) the chat-create flow all already exist.

## 7. Feature flag & rollout

- Add `const val SEQUENCES_ENABLED = false` to `core/FeatureFlags.kt` (next to `ABILITIES_ENABLED`).
- Every entry point (hub tab, nav route registration, any For-You surfacing) guards on it, so with the
  flag off the routes are unreachable and the tab is absent — same discipline as the pillar flags.
- The **schema/sync layer ships unconditionally** (tables + syncers exist even when the flag is off);
  only the UI/entry points are gated. This lets the DB + sync land early and de-risk the release.
- Because skill paths depend on Abilities, if `SEQUENCES_ENABLED` is on we also need
  `ABILITIES_ENABLED` on for the *skill* half; **exercise/routine sequences work with Abilities off**.
  v0 can ship Routine fully and Path as "earns a skill" only when Abilities is also enabled.

## 8. Build phases (each independently mergeable behind the flag) — PARKED

Not scheduled yet. When we pick this up:

1. **Data + sync**: schema v31, entities (incl. API exercise fields), mappers, repository, 3 syncers,
   Supabase tables + RLS. No UI. Verifiable by tests + a debug seed.
2. **Progress calculator + use cases** with unit tests (pure logic first, like `GoalOptimizer`).
3. **Workout catalog integration**: pick the API (§4a — likely wger to start), edge-function proxy +
   cache, `WorkoutCatalogService`, exercise search/browse. This is the piece with the **cost call**.
4. **List + create/edit UI** (workout builder: browse catalog → add exercises) behind the flag; a
   Sequences sub-tab.
5. **Run/track UI** with step resolution (exercise/habit/goal), reusing check-in + celebration.
6. **Skill paths**: `skillAbilityId`, Ability XP on completion (requires `ABILITIES_ENABLED`).
7. **Yoga expansion**: add a yoga-capable catalog source; same model.
8. **Fast-follows**: AI-generated workouts, reminders, sharing sequences.

## 9. Decisions & open questions

**Locked (2026-07-30):**
- Scope order: **Workout first → Yoga next** (§1).
- **Ready-made exercise API**, not self-authored content; key server-side via an edge proxy (§4a).
- **Deferred** — parked for a future release; flag stays off; nothing built now.
- Prefer a **free/open** source (wger) to start; treat paid media APIs as a later, cost-gated upgrade.

**Still open (decide when we start):**
1. **Entry point**: a "Sequences" sub-tab in the Artifact hub (recommended) vs. a Habits-tab section.
2. **User-facing name**: "Workouts" for v0? "Sequences" / "Routines" as the umbrella once yoga lands?
3. **Exercise API pick**: wger (free) vs. ExerciseDB (paid, richer GIFs) — §4a.
4. **Routine XP / streak**: small fixed XP per completed workout + its own streak? (Proposal: yes.)
5. **Path ↔ Ability**: one Ability per path (proposal) or several.

---
*Parked. When revived: settle §9's open items, then Phase 1 (data + sync) is the first PR, no
user-visible change, flag off. The workout-catalog phase (3) is where the API cost decision lands.*
