# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Layout

This is a multi-target monorepo, not just an Android app. Be aware of which surface you're editing:

- `app/shared/` — Kotlin Multiplatform library (Android + iOS). All app logic, UI, widgets, services, and Android `actual` implementations live here. Namespace `az.tribe.lifeplanner.shared`. Uses the AGP 9 `com.android.kotlin.multiplatform.library` plugin and exposes an iOS framework named `ComposeApp`.
- `app/androidApp/` — Thin Android application module. Contains only `AndroidManifest.xml`, `google-services.json`, `proguard-rules.pro`, the signing/release build types, Firebase Gradle plugins (`google-services`, `crashlytics`, `firebase-perf`), and Play services (in-app update/review). Application id `az.tribe.lifeplanner`. Depends on `:app:shared` via `implementation(projects.app.shared)`.
- `app/iosApp/` — Swift/SwiftUI host that embeds the `ComposeApp` framework from `:app:shared`. iOS Kotlin code changes happen in `app/shared/src/iosMain` or `commonMain`, not here. Xcode run-script invokes `./gradlew :app:shared:embedAndSignAppleFrameworkForXcode`.
- `web/` — Separate Next.js 16 / React 19 marketing + admin + chat site (`lifeplanner-web`). Independent from the mobile app; uses its own `package.json`.
- `supabase/` — Postgres schema (`schema.sql`), seed SQL for built-in coaches and system prompts, and Edge Functions (`functions/ai-proxy`, `auth-redirect`, `mcp-server`, `persona-sync-webhook`, `resend-verification`).
- `../lifeplanner-assets/` (sibling, not in this repo) — `docs/`, `design/`, `screenshots/`, `postman/`, `scripts/` live here. `../lifeplanner-assets/docs/terminology.md` remains the authoritative glossary for domain terms (categories, XP rules, badge tiers, dependency types, sync states, route names) — consult it before naming new concepts.

Root project name `LeanLifePlanner`. The repo only targets Android + iOS — desktop/JVM and web targets are intentionally absent.

## Common commands

All KMP commands run from the repo root via `./gradlew`. The `web/` directory has its own npm scripts run from inside `web/`.

### Build / run

```bash
./gradlew build                                  # Full multiplatform build
./gradlew :app:androidApp:installDebug           # Install Android debug on connected device
./gradlew :app:androidApp:assembleDebug          # Build debug APK
./gradlew :app:androidApp:bundleRelease          # Signed AAB (used by CI → Firebase Distribution)
```

iOS is built from Xcode using `app/iosApp/iosApp.xcodeproj` (scheme `iosApp`). The iOS framework is produced by the `:app:shared` `iosArm64`/`iosSimulatorArm64` targets — locally only those two compile; CI also builds `iosX64` (gated by `System.getenv("CI") != null` in `app/shared/build.gradle.kts`).

### Tests

```bash
./gradlew :app:shared:allTests                                  # All KMP targets
./gradlew :app:shared:testAndroidHostTest                       # Android host unit tests
./gradlew :app:shared:iosSimulatorArm64Test                     # iOS simulator tests

# Single test class or method (Gradle pattern):
./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.usecases.GoalUseCasesTest"
```

Coverage (Kover) is not currently wired into the new module layout — re-add filters in `app/shared/build.gradle.kts` if needed.

### Web

```bash
cd web
npm install
npm run dev      # next dev
npm run build    # next build
npm run lint     # next lint
```

## Local configuration

`local.properties` (gitignored) supplies build-time secrets via `buildkonfig` into `az.tribe.lifeplanner.BuildKonfig`. Required keys for full functionality:

- `sdk.dir` — Android SDK path
- `SUPABASE_URL`, `SUPABASE_ANON_KEY` — backend
- `POSTHOG_API_KEY`, `POSTHOG_HOST` (defaults to `https://us.i.posthog.com`)
- `PERSONA_API_SECRET` — Persona identity verification
- `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` — only if signing release builds; requires `lifeplanner.jks` at repo root

`isDebug` is automatically `false` for the `release` build type and all iOS release targets.

## Architecture

The mobile app follows clean architecture with strict layer boundaries enforced by directory structure under `app/shared/src/commonMain/kotlin/az/tribe/lifeplanner/`:

- `domain/` — pure Kotlin: `model/`, `enum/`, `repository/` (interfaces), `service/`. No platform or framework deps.
- `data/`: `repository/` implements domain interfaces, each class named for what it wraps (`SqlDelightGoalRepository`, `SupabaseStoryRepository`, `OpenMeteoWeatherRepository`), never with an `Impl` suffix; `mapper/` converts SQLDelight entities ↔ domain; `network/` Ktor + Supabase services (`AiProxyService`, `GeminiService`, `BuiltinCoachFetcher`, `PersonaApiFetcher`, `SystemPromptFetcher`); `sync/` bidirectional cloud sync; `auth/` (`SupabaseAuthService`); `behavior/`, `health/`, `review/`, `tutorial/`, `analytics/`.
- `usecases/` — single-purpose actions (split into subdirs by feature: `habit/`, `journal/`, `ability/`, `health/`).
- `ui/` — Compose screens, components, and ViewModels, organized by feature (`goal/`, `habit/`, `journal/`, `chat/`, `coach/`, `focus/`, `gamification/`, `balance/`, `dependency/`, `planner/`, `calendar/`, `retrospective/`, `screentime/`, `onboarding/`, `auth/`, `profile/`, `analytics/`, `reminder/`, `backup/`, `ability/`, `health/`, `objectives/`, `feedback/`, `search/`, `theme/`, `navigation/`, `utils/`, `viewmodel/`).
- `di/`: Koin modules, one file per feature holding `<feature>DataModule`, `<feature>DomainModule` (use cases) and `<feature>PresentationModule` (ViewModels), with `coreDataModule`, `networkModule` and `supabaseModule` for shared infrastructure. `Koin.kt` assembles them all into `appModules`; `KoinModulesTest` verifies the graph on the JVM. Prefer `singleOf`/`factoryOf`/`viewModelOf` and fall back to a lambda only for factory functions or route parameters. `DatabaseDriverFactory`, `FileSharer`, `FilePicker` are `expect`/`actual` declarations.
- `infrastructure/SharedDatabase.kt` — wraps the SQLDelight `LifePlannerDB`.
- `notification/`, `widget/`, `worker/`, `util/`, `core/` — cross-cutting.

### Source set hierarchy (important)

Source sets are wired in `app/shared/build.gradle.kts`. The `com.android.kotlin.multiplatform.library` plugin replaces the legacy `com.android.library` + `kotlinMultiplatform` combo and uses the standard hierarchy: `commonMain` → `androidMain` / `iosMain`. Host tests live in `androidHostTest` (formerly `androidUnitTest`). Key consequences:

- The default KMP hierarchy template is in effect — `iosArm64Main`, `iosSimulatorArm64Main`, optionally `iosX64Main` (CI) all extend `iosMain` automatically.
- Three Android-only libs without a JVM artifact (`firebase-crashlytics`, `cmpcharts`, `compose-mediaplayer`) are added to both `androidMain.dependencies` and `iosMain.dependencies`. If you bump these versions, update both places.
- The androidApp module re-declares `androidx-activity-compose`, `koin-android`, `koin-compose`, `play-app-update`, `play-review`, `health-connect`, and the Firebase BOM because Firebase Gradle plugins instrument the application classpath.

### Database — SQLDelight

- Schema in `app/shared/src/commonMain/sqldelight/az/tribe/lifeplanner/database/` (`.sq` files). Generated package: `az.tribe.lifeplanner.database`. Async generation is enabled (`generateAsync = true`).
- **The current schema version is the `version = NN` line in `app/shared/build.gradle.kts`** — read it there, it is the source of truth. Every schema bump must:
  1. Increment the `version` number,
  2. Add a migration `.sqm` file in `app/shared/src/commonMain/sqldelight/migrations/`,
  3. Append a one-line comment after the version number summarizing the change — the project already follows this convention (e.g. `// 28: ScreenTimeEventEntity + UserActivityPattern behavioral columns`),
  4. **Add a matching runtime migration for Android** (see migrations gotcha below) — add a `migrateToVersionNN(db)` to `app/shared/src/androidMain/.../di/DatabaseMigrations.kt` and call it from `DatabaseDriverFactory.onOpen()`,
  5. Add or update mapper(s) under `data/mapper/`,
  6. Add a corresponding `TableSyncer` in `data/sync/syncers/` and register it in `SyncerFactory` if the table needs cloud sync.

> **Migrations gotcha (read this).** The `.sqm` files are **not** applied at runtime on Android. Android runs idempotent migrations manually in `DatabaseDriverFactory.onOpen()` (the `migrateToVersionNN(db)` chain, using `CREATE TABLE IF NOT EXISTS` / `addColumnSafe`); the `.sqm` files only drive **iOS** (via `DefensiveSchema` wrapping the generated `Schema`) and SQLDelight's **compile-time** migration verification. So a build can pass while the running Android app crashes with `no such column: …`. Skipping step 4 is exactly that trap. (Function names in `DatabaseMigrations.kt` are loosely numbered and decoupled from `.sqm` numbers; match the `.sqm` version number for new ones and keep them idempotent.)

### Cloud sync

Local SQLite ↔ Supabase Postgres is bidirectional and **soft-delete based**. The pattern is fixed across all tables:

- Each synced table has `sync_updated_at`, `is_deleted`, `sync_version`, `last_synced_at` columns.
- One `TableSyncer<DomainModel, Dto>` per table, all wired through `SyncerFactory` and orchestrated by `SyncManager`.
- Direction: push then pull. Conflict resolution by `sync_version` counter.
- Triggered with a 2-second debounce after any mutation.
- Sync states: `Idle` / `Syncing` / `Synced` / `Offline` / `Error` — see `data/sync/SyncState.kt`.

When adding a new synced entity, follow an existing syncer (`GoalTableSyncer`, `HabitTableSyncer`) as a template; the DTOs live in `data/sync/dto/SyncDtos.kt`.

### AI

There are **no client-side AI API keys**. All AI calls (Gemini, OpenAI, Grok) route through the `ai-proxy` Supabase Edge Function (`supabase/functions/ai-proxy`) via `AiProxyService`/`EdgeFunctionAiProxyService` and `GeminiService`/`ProxiedGeminiService`. The user picks the provider in profile settings; the proxy decides which upstream to call. Built-in coach prompts and personas are seeded from `supabase/builtin_coaches.sql` and `supabase/system_prompts.sql` and fetched at runtime via `BuiltinCoachFetcher` / `SystemPromptFetcher`.

### Entry points

- Android: `app/shared/src/androidMain/.../MainApplication.kt` + `MainActivity.kt`. These live in the **shared** library (not in `androidApp`) — `androidApp` only contains the `AndroidManifest.xml` that declares them by FQN.
- iOS: `app/shared/src/iosMain/.../MainViewController.kt` (consumed by the SwiftUI host in `app/iosApp/`). The Kotlin framework baseName is kept as `ComposeApp` so all Swift `import ComposeApp` statements work unchanged.

### Auth

Dual-stack: Firebase Auth + Supabase Auth + a Guest mode. States: `Loading`, `Authenticated`, `Guest`, `Unauthenticated`. `SupabaseAuthService` is the primary façade; ViewModels should depend on `AuthService` (the interface), not the impl.

## Conventions

- Kotlin / JetBrains style; prefer `val`, keep Composables small, hoist state.
- The Android/KMP architecture skills (`android-presentation-mvi`, `android-error-handling`, `android-data-layer`, `android-di-koin`, `android-compose-ui`, `android-testing`, `android-navigation`, `android-module-structure`) are the house style. Where the app already follows them, keep it that way:
  - **Errors**: expected failures return `Result<D, E>` from `domain/model` (`onSuccess`/`onFailure`/`map`/`asEmptyResult`), never throw; shared errors are `DataError.Network`/`DataError.Local`, mapped to copy with `DataError.toUiText()` in `ui/`. Network calls go through `safeCall`.
  - **Screens**: new screens follow `ui/habit/HabitDetail*` as the template. One `<Screen>State` data class, a sealed `<Screen>Action`, a sealed `<Screen>Event` sent through a `Channel`, a ViewModel with `onAction`, a `<Screen>Root` that owns the ViewModel and observes events with `ObserveAsEvents`, and a `<Screen>Screen` that takes only state and `onAction` with a `@Preview`. Collect with `collectAsStateWithLifecycle()`; inject with `koinViewModel()` only in the Root.
  - **Compose**: `contentDescription` comes from `Res.string.cd_*`; animate alpha and offsets in `graphicsLayer`/`offset {}` lambdas; keep `remember` for Compose-owned state (list, scroll, snackbar host), not app state.
  - **Tests**: kotlin.test + Turbine + AssertK with `UnconfinedTestDispatcher` and the fakes in `commonTest/testutil`; JVM-only checks (Koin graph, Roborazzi previews) live in `androidHostTest`.
  - **Known deviations**: navigation is still string-routed (`Screen.kt`, deep links from both platforms depend on it), and the app is a single `app/shared` module rather than `:feature:*` modules. Both are tracked as follow-ups, not conventions to copy.
- Per `../lifeplanner-assets/docs/terminology.md`: feature names, route strings, XP rewards, badge categories, level titles, and category enums are stable — use the exact spellings already documented there (e.g. `Career`/`Financial`/`Physical`/`Social`/`Emotional`/`Spiritual`/`Family`, plus `Personal Growth` only as the 8th Life Balance area mapping to Career).
- Navigation routes are string-keyed and listed in `../lifeplanner-assets/docs/terminology.md` — match the existing pattern (`goal_detail/{goalId}`, `journal_entry_detail/{entryId}`, etc.) when adding screens.
- Logging via Kermit, not platform loggers, so logs flow through both Android and iOS.
- The implementation plan in `../lifeplanner-assets/docs/implementation-plan.md` is the historical phase log — useful for context on completed work, but not a forward-looking spec.

## CI

GitHub Actions in `.github/workflows/` trigger on pushes to `develop` (not `main`):

- `android.yml` — builds release AAB and uploads to Firebase App Distribution (group `internal`).
- `ios.yml` — archives via `xcodebuild`, exports IPA, uploads to TestFlight.
- `manual_deploy_to_firebase.yml` — manually triggered Firebase deploy.

Release builds need `lifeplanner.jks` and `RELEASE_*` properties; without them the build still succeeds but is unsigned (the keystore block is conditional on `keystoreFile.exists()`).
