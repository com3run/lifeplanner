---
name: run-lifeplanner
description: Build, install, launch, screenshot, and drive the LifePlanner Android app (az.tribe.lifeplanner) on an emulator or device. Use when asked to run, build, install, launch, screenshot, or UI-test the app, or to verify a change on a device.
---

# Run LifePlanner

LifePlanner is a Kotlin Multiplatform app (Android + iOS). All app logic and UI live in
`app/shared/` (KMP library, AGP 9 `com.android.kotlin.multiplatform.library`); `app/androidApp/`
is a thin Android host. This skill drives the **Android** build, which is fully scriptable here via
`adb`. iOS builds from Xcode (see Human path).

The driver is **`.claude/skills/run-lifeplanner/driver.sh`** (paths below are relative to the repo
root). It wraps the exact `gradlew` + `adb` flow used to build and drive this app: compile, install,
launch, screenshot, dump the view tree, tap, swipe, type. Run it instead of remembering the flags.

```bash
.claude/skills/run-lifeplanner/driver.sh help        # list commands
.claude/skills/run-lifeplanner/driver.sh devices
```

## Prerequisites

macOS with **Android Studio** installed (it ships the JBR this build needs) and the **Android SDK
platform-tools** (`adb`). A running emulator or a connected device. No Homebrew JDK is required, and
there is usually **no `java` on PATH** here, so the JDK path matters (see Gotchas).

- `adb` at `~/Library/Android/sdk/platform-tools/adb` (override with `ADB=...`).
- `JAVA_HOME` = `/Applications/Android Studio.app/Contents/jbr/Contents/Home` (the driver sets this
  default; override with `JAVA_HOME=...`).
- `local.properties` with `sdk.dir`. Backend/AI features need `SUPABASE_URL`, `SUPABASE_ANON_KEY`,
  etc. (see root `CLAUDE.md`); the app still builds and runs without them (guest mode, no cloud).

Confirm a device is attached:

```bash
.claude/skills/run-lifeplanner/driver.sh devices
```

## Build

Fastest red/green is compiling the shared module (do this after editing Kotlin, before installing):

```bash
.claude/skills/run-lifeplanner/driver.sh build        # ./gradlew :app:shared:compileAndroidMain
```

Install the debug APK (build + push to the device). With more than one device attached, pick one
with `ANDROID_SERIAL`:

```bash
ANDROID_SERIAL=emulator-5554 .claude/skills/run-lifeplanner/driver.sh install
```

## Run (agent path) — drive it with the driver

This is the path to use. Cold-launch lands on the "For You" home feed; drive and screenshot from
there. Screenshots and view dumps land in `/tmp` (override with `OUT=...`).

```bash
export ANDROID_SERIAL=emulator-5554            # pick the target once

D=.claude/skills/run-lifeplanner/driver.sh
$D stop                                        # force-stop for a clean cold start
$D launch                                      # monkey-launch, waits for window focus
$D screenshot home                             # -> /tmp/home.png  (then Read the PNG)
```

Drive the UI. To find a tap target, dump the view tree and tap the **center of a `bounds` box**:

```bash
$D dump                                        # prints text="..." bounds="[x1,y1][x2,y2]" pairs
$D tap 555 1178                                # tap a row/button
$D swipe 540 1800 540 600 350                  # scroll up (drag from low y to high y)
$D screenshot after_tap
$D logcat "Koin|FATAL|Exception"               # recent log lines, filtered
```

Every command above was run against `emulator-5554` this session: `install` (BUILD SUCCESSFUL),
`stop` / `launch` (focus = `az.tribe.lifeplanner/.MainActivity`), `screenshot` (PNGs read back),
`dump`, `tap`, `swipe`. Tapping a feed card deep-linked into the goal/habit detail screens; scrolling
revealed the Reflect + Learn feed sections.

## Direct invocation — test internal logic without the full app

Most logic (use cases, engines, view models) is covered by host unit tests on the JVM, no device
needed. Run one class while iterating:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :app:shared:testAndroidHostTest --tests "az.tribe.lifeplanner.usecases.GoalUseCasesTest"
```

The whole suite is `:app:shared:testAndroidHostTest` (see Gotchas: it can be very slow / hang, so a
filtered run or `build` is the faster signal while developing).

## Run (human path)

- **Android:** open the repo in Android Studio and Run, or `./gradlew :app:androidApp:installDebug`
  then launch from the launcher. Useless for scripted verification, but fine for hands-on use.
- **iOS:** open `app/iosApp/iosApp.xcodeproj` (scheme `iosApp`) in Xcode and run; the run-script
  invokes `./gradlew :app:shared:embedAndSignAppleFrameworkForXcode`. Not drivable via this skill.

## Gotchas

- **No `java` on PATH.** Gradle fails with "Unable to locate a Java Runtime" unless `JAVA_HOME`
  points at Android Studio's JBR (`/Applications/Android Studio.app/Contents/jbr/Contents/Home`).
  The driver exports this by default.
- **AGP 9 task names differ.** The compile task is `:app:shared:compileAndroidMain`. The old
  `:app:shared:compileDebugKotlinAndroid` does **not** exist under the
  `com.android.kotlin.multiplatform.library` plugin and fails with "task not found".
- **`adb screencap -p` piped to a file is corrupted here.** This emulator prints a
  `[Warning] Multiple displays were found...` banner to stdout, which lands inside the PNG bytes and
  makes it unreadable. Always `screencap` to `/sdcard` then `adb pull` (the `screenshot` command does
  this). The warning on stderr is harmless.
- **Two devices attached** (a physical Samsung over wifi-adb and `emulator-5554`). `gradlew
  installDebug` and `adb` both honor `ANDROID_SERIAL`. Prefer the **emulator** for scripted
  screenshots: consistent resolution (1080x2400) and you do not disturb the real phone.
- **Finding tap targets:** `dump` gives `text="..."` plus `bounds="[x1,y1][x2,y2]"`; tap the box
  center. On list rows the leading check-circle is its **own** tap target (e.g. x 63-189), separate
  from the row body, so a tap at low x toggles instead of opening. Aim mid-row to open.
- **Gamification celebration overlays** ("Badge Earned!", level-up) can appear over the UI on launch
  or after a check-in, covering content. They say "Tap to dismiss" — tap once before continuing, but
  note a stray tap can fall through onto a card and navigate.
- **`testAndroidHostTest` can hang.** This session a test executor spun at 100% CPU for ~28 min with
  no progress and had to be killed. Use `build` (compile) for fast validation; run the test suite
  filtered (`--tests ...`) or with a timeout.
- **App entry points live in `:app:shared`** (`MainActivity` / `MainApplication`), declared by FQN in
  `app/androidApp/.../AndroidManifest.xml`. Launching by package (`monkey`) is simplest.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Unable to locate a Java Runtime` | Set `JAVA_HOME` to the Android Studio JBR (see Prerequisites). |
| Gradle: `Task 'compileDebugKotlinAndroid' not found` | Use `:app:shared:compileAndroidMain`. |
| `install` fails with more than one device | `export ANDROID_SERIAL=<serial>` (from `driver.sh devices`). |
| Screenshot PNG is unreadable / "not a valid PNG" | Use `driver.sh screenshot` (screencap to file + pull), never a piped `screencap`. |
| App crashes on launch with `no such column: ...` | A schema bump shipped without its runtime Android migration; see the migrations note in root `CLAUDE.md` (`DatabaseMigrations.kt`). |
| `launch` prints no focus line | The app is still starting; re-run `launch`, or check `logcat "FATAL|Exception"`. |
