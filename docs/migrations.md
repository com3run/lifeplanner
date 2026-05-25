# SQLDelight schema migrations

The local SQLite schema version lives in `app/shared/build.gradle.kts` (the `version = NN` literal,
with a one-line `// NN:` comment per bump). This guide is the consolidated history from v2.5 onward
(version 22). Versions 21 and earlier predate this guide.

## How migrations are applied (read this first)

Two separate mechanisms, by platform. They must stay in lock-step.

- **iOS + compile-time verification:** the `.sqm` files in
  `app/shared/src/commonMain/sqldelight/migrations/` drive iOS (via `DefensiveSchema`) and
  SQLDelight's compile-time migration check.
- **Android (runtime):** the `.sqm` files are **not** applied at runtime on Android. Android runs
  idempotent migrations manually in `DatabaseDriverFactory.onOpen()` via the `migrateToVersionNN(db)`
  chain in `app/shared/src/androidMain/.../di/DatabaseMigrations.kt` (using
  `CREATE TABLE IF NOT EXISTS` / `addColumnSafe`).

> A build can pass while a running Android app crashes with `no such column: ...` if a schema bump
> ships without its matching Android runtime migration. Every bump needs both.

## Version history (22 onward)

| Version | Change | Rationale |
|--------:|--------|-----------|
| 22 | `HabitEntity.unit` | Per-habit measurement unit (e.g. "pages", "km"). |
| 23 | `CachedPersonaEntity` | Cache built-in coach personas locally. |
| 24 | `HabitCheckInEntity.count` | Count-based check-ins (target > 1 per day). |
| 25 | Interim persona migration | Groundwork folded into 26 (no standalone column comment). |
| 26 | `CachedPersonaEntity.slug` + `avatar_url` | Stable persona slugs and avatars. |
| 27 | `UserSituationEntity` | Snapshot of body/meta/purpose context (energy, sleep, stress). |
| 28 | `ScreenTimeEventEntity` + `UserActivityPattern` behavioral columns | Screen-time / usage tracking. |
| 29 | `LifeValueEntity` table | Pillar 1: the values layer (synced). |
| 30 | `GoalEntity.valueId` | Pillar 1: link a goal to the value it serves (nullable). |
| 31 | `DecisionEntity` table | Pillar 3: decisions as first-class objects (synced). |
| 32 | `GoalEntity.predictedDueDate` + `MilestoneEntity.estimatedEffort` | Pillar 4: prediction vs actual. |
| 33 | `IdentityStatementEntity` table | Pillar 5: "becoming" identity statements (synced). |
| 34 | `DecisionProfileEntity` table | Pillar 7: the six tuning dials, one row per user (synced). |

## Supabase / backend consumers

New **synced** tables need a matching Postgres table in `supabase/schema.sql` plus a `TableSyncer`
registered in `SyncerFactory` (soft-delete columns: `sync_updated_at`, `is_deleted`, `sync_version`,
`last_synced_at`). Synced additions in this range:

- New tables: `life_values` (29), `decisions` (31), `identity_statements` (33), `decision_profiles` (34).
- New columns on existing synced tables: `goals.value_id` (30), `goals.predicted_due_date` +
  `milestones.estimated_effort` (32).

All additions are nullable or new tables, so no destructive change for existing rows. A backend
consumer reading these tables should treat the new columns as optional.

## Adding the next migration (checklist)

1. Increment `version` in `app/shared/build.gradle.kts` and append a one-line `// NN:` comment.
2. Add the `.sqm` file under `sqldelight/migrations/`.
3. Add `migrateToVersionNN(db)` to `DatabaseMigrations.kt` and call it from `DatabaseDriverFactory.onOpen()` (idempotent).
4. Update or add the mapper under `data/mapper/`.
5. If the table syncs, add the `TableSyncer`, the DTO in `data/sync/dto/SyncDtos.kt`, the `SyncerFactory` registration, and the Supabase table.
