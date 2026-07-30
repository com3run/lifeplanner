# Spec: Sequences ("Guided bundles → skills")

**Status:** Draft for review. Target: **next release**, shipped **behind a feature flag**
(`FeatureFlags.SEQUENCES_ENABLED = false`) so it can merge incrementally and integrate later without
touching current behavior. Nothing below is wired into a live surface until the flag flips.

## 1. Summary

A **Sequence** is an ordered, guided bundle of steps the user works through toward one outcome. Two
flavors from the same model:

1. **Exercise sequence** (v0 anchor) — a hand-built ordered list of activities: "Morning Reset =
   ① 5 breaths → ② 10 pushups → ③ 2-min plank → ④ journal one line." A lightweight guided routine.
2. **Skill path** (the extension the user asked for) — "to earn *Consistent Runner*, do this bundle:
   build the *Run 3x/week* habit, complete the *First 5K* goal, finish the *Form basics* exercise."
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
- Step types: **exercise** (one-off checkable item, optional duration/reps note), **habit** (link an
  existing or new habit), **goal** (link an existing goal), **milestone** (a specific goal milestone).
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
| title | TEXT | shown label (denormalized for EXERCISE; cache for linked types) |
| ref_id | TEXT? | habit/goal/milestone id (null for EXERCISE) |
| detail | TEXT? | e.g. "10 reps", "2 min" (EXERCISE only) |
| + standard sync columns | | |

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

## 8. Build phases (each independently mergeable behind the flag)

1. **Data + sync**: schema v31, entities, mappers, repository, 3 syncers, Supabase tables + RLS.
   No UI. Verifiable by tests + a debug seed.
2. **Progress calculator + use cases** with unit tests (pure logic first, like `GoalOptimizer`).
3. **List + create/edit UI** (Routine mode only) behind the flag; a Sequences sub-tab.
4. **Run/track UI** with step resolution (exercise/habit/goal), reusing check-in + celebration.
5. **Skill paths**: `skillAbilityId`, Ability XP on completion (requires `ABILITIES_ENABLED`).
6. **Fast-follows** (post-release): AI-generated sequences, reminders, sharing sequences.

## 9. Open questions (need your call before build)

1. **Sub-tab vs. Habits-section** for the entry point (§6) — recommend sub-tab.
2. **Naming**: "Sequences" internally — is that the user-facing word, or "Routines" / "Paths" /
   "Playbooks"? (Affects copy + the Ability tie-in wording.)
3. **Routine XP**: how much per completed run, and should runs build their own streak separate from
   habit streaks? (Proposal: small fixed XP per run, own streak.)
4. **Path ↔ Ability**: one Ability per path, or can a path feed several abilities? (Proposal: one, to
   keep progress legible.)
5. **Exercise steps in PATH mode**: allowed (proposal: yes, checked once) or Path = links-only?
6. Do we want a small set of **built-in starter sequences** (e.g. "Morning Reset", "Wind-down") to
   seed the feature, like built-in coaches? (Proposal: yes, 2–3, ship with the release.)

---
*Once §9 is decided, Phase 1 (data + sync) is the first PR — no user-visible change, flag stays off.*
