# LifePlanner v3.0.0 — Release Runbook

App id `az.tribe.lifeplanner` · versionName **3.0.0** · Android versionCode **11** · iOS build **8**
Supabase project `rkdggdfabwgukspylybu`. Prepared 2026-07-25.

## What's in v3 (since the 2.3 on the stores)
Pillar 6 Possibility Mode (now with a coach hand-off + instant options), the For You home
feed, the goal-as-journey redesign, habit track modes + health auto-complete, device
calendar + steps on home, the new **Learn hub** (lesson paths, synced progress,
recommendations), and Supabase-native monitoring.

---

## ✅ Verified (code + backend ready)
- **Release AAB builds clean** — `:app:androidApp:bundleRelease` succeeds (R8 + resource shrink + lint-vital). ~50 MB. Unsigned locally (see signing below).
- **iOS shared framework compiles** — `:app:shared:compileKotlinIosSimulatorArm64` passes with all v3 code.
- **Unit tests green** — 918 host tests pass.
- **DB schema v36** — the Learn hub added `KnowledgeReadEntity`. Runtime Android migration (`migrateToVersion36`) is wired into `DatabaseDriverFactory.onOpen()` and idempotent; `.sqm` present; iOS defensive schema covers it. 2.3 shipped at schema v21, so upgraders migrate 21→36 with all steps present.
- **Backend deployed & in sync** — all 7 edge functions ACTIVE; `ai-proxy` redeployed (chat provider-fallback fix); `knowledge_reads` table + RLS live; monitoring cron jobs (`lifeplanner-health` 5 min, `lifeplanner-store-watch` 30 min) active; secrets set.

## ⚠️ Blockers that need YOU (credentials / accounts — I can't do these)

1. **Android release signing.** The AAB builds **unsigned** until the keystore + `RELEASE_*` props are set. The keystore path is now configurable, so you do NOT need to copy the `.jks` into the repo. Add to `local.properties` (git-ignored):
   ```
   RELEASE_STORE_FILE=~/Documents/tribe/lifeplanner.jks
   RELEASE_STORE_PASSWORD=…
   RELEASE_KEY_ALIAS=…
   RELEASE_KEY_PASSWORD=…
   ```
   `RELEASE_STORE_FILE` accepts an absolute, `~`-relative, or repo-root-relative path; it defaults to `lifeplanner.jks` at the repo root if unset. Then `./gradlew :app:androidApp:bundleRelease` produces a signed AAB.
   - **OR** ship via CI (`.github/workflows/android.yml`) which signs from its own secrets.

2. **iOS `GoogleService-Info.plist`.** Missing from the checkout (git-ignored Firebase secret). Download from Firebase Console → the iOS app (`az.tribe.lifeplanner`) → save to `app/iosApp/iosApp/GoogleService-Info.plist`. Without it the Xcode app build fails.

3. **iOS `Info.plist` / `iosApp.entitlements` — REVIEW.** These have uncommitted edits (not made in this session). The **app group `group.az.tribe.lifeplanner` was removed from entitlements** (the widget needs it) and several `Info.plist` keys look dropped (`CFBundleVersion`, `LSRequiresIPhoneOS`, `UIApplicationSceneManifest`, `UIBackgroundModes`). Review/restore before shipping iOS or the widget + versioning may break.

4. **UI8 / Dotion license.** Confirm on your UI8 account that the Dotion illustration pack tier permits use in a store-distributed app. (Only compiled VectorDrawables ship, not source SVGs.)

5. **Play Console alert — target API 36 (FIXED IN CODE).** Play requires apps to target Android 16 (API 36) by 31 Aug 2026. Bumped `android-targetSdk` 35 → 36 (`gradle/libs.versions.toml`; compileSdk already 37). Clears once a signed v3 build targeting 36 is published to production. Smoke-tested on the emulator.

## Pre-submit checks (against the consoles — I can't see them)
- Android **versionCode 11 > last published** code.
- iOS **build 8 > last uploaded** build.
- **Data safety (Play) / privacy (App Store)** declare health data + AI usage.

## Store copy
`../lifeplanner-assets/docs/store/whatsnew-3.0.0.md` (Play ≤500 chars + Apple release notes), updated for the Learn hub.

## Ship
- **Android:** signed AAB → Play Console (internal testing → production), or push to `develop` for CI → Firebase App Distribution.
- **iOS:** Xcode archive scheme `iosApp` → TestFlight → App Store (needs items 2 & 3).
- **Backend:** already live. No action.

## Post-release
- Watch the Telegram monitoring alerts (health every 5 min, store-watch every 30 min).
- `store-watch` will alert when the new version goes live on each store.
