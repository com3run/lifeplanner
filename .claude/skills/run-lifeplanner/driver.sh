#!/usr/bin/env bash
# Driver for the LifePlanner app (az.tribe.lifeplanner), Android target.
#
# Build, install, launch, and DRIVE the running app on a connected device/emulator via adb.
# This is the harness the /run-lifeplanner skill points at. Every subcommand here was used to
# build + drive this app during development (compile, install, launch, screenshot, tap, dump).
#
# Usage:  .claude/skills/run-lifeplanner/driver.sh <command> [args]
# Pick a device with the ANDROID_SERIAL env var when more than one is attached.
set -euo pipefail

# --- Config (macOS + Android Studio defaults; override via env) ----------------------------------
: "${JAVA_HOME:=/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
export JAVA_HOME
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
APP_ID="az.tribe.lifeplanner"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"   # repo root, 3 levels up from skill dir
OUT="${OUT:-/tmp}"                                              # where screenshots/dumps land

# Resolve target serial: ANDROID_SERIAL wins; else the sole device; else ask.
resolve_serial() {
  if [ -n "${ANDROID_SERIAL:-}" ]; then echo "$ANDROID_SERIAL"; return; fi
  local ds; ds=$("$ADB" devices | grep -wv "List" | grep -w "device" | awk '{print $1}')
  local n; n=$(printf '%s\n' "$ds" | grep -c . || true)
  if [ "$n" = "1" ]; then echo "$ds"; else
    echo "Multiple/zero devices; set ANDROID_SERIAL. Attached:" >&2; "$ADB" devices >&2; exit 1
  fi
}

adbx() { "$ADB" -s "$(resolve_serial)" "$@"; }   # adb against the chosen device

cmd="${1:-help}"; shift || true
case "$cmd" in
  devices)                # list attached devices
    "$ADB" devices ;;

  build)                  # fast compile of the shared KMP module (Android) - quickest red/green
    cd "$ROOT"; ./gradlew :app:shared:compileAndroidMain ;;

  install)                # build + install the debug APK on the chosen device
    cd "$ROOT"; ANDROID_SERIAL="$(resolve_serial)" ./gradlew :app:androidApp:installDebug ;;

  launch)                 # cold-launch the app and wait until it has focus
    adbx shell monkey -p "$APP_ID" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
    for _ in $(seq 1 30); do
      adbx shell dumpsys window 2>/dev/null | grep -q "mCurrentFocus.*$APP_ID" && { echo "launched"; break; }
      sleep 0.5
    done
    adbx shell dumpsys window 2>/dev/null | grep "mCurrentFocus" ;;

  stop)                   # force-stop the app
    adbx shell am force-stop "$APP_ID"; echo "stopped" ;;

  screenshot)             # screenshot <name> -> $OUT/<name>.png  (handles the multi-display warning)
    name="${1:-shot}"
    adbx shell screencap -p /sdcard/cap.png        # to a file, NOT piped: piping is corrupted by the
    adbx pull /sdcard/cap.png "$OUT/$name.png" >/dev/null   # "[Warning] Multiple displays" line on stdout
    echo "$OUT/$name.png" ;;

  dump)                   # dump the view hierarchy to stdout (find tap targets by text + bounds)
    adbx shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
    adbx pull /sdcard/ui.xml "$OUT/ui.xml" >/dev/null
    # Print text="..." and its bounds="..." pairs - tap the center of a bounds box.
    grep -oE 'text="[^"]+"[^>]*bounds="\[[0-9]+,[0-9]+\]\[[0-9]+,[0-9]+\]"' "$OUT/ui.xml" || cat "$OUT/ui.xml" ;;

  tap)                    # tap <x> <y>
    adbx shell input tap "$1" "$2"; echo "tapped $1 $2" ;;

  swipe)                  # swipe <x1> <y1> <x2> <y2> [durationMs]  (also used for scrolling)
    adbx shell input swipe "$1" "$2" "$3" "$4" "${5:-300}"; echo "swiped" ;;

  text)                   # text "<string>"  (types into the focused field)
    adbx shell input text "${1// /%s}"; echo "typed" ;;

  logcat)                 # logcat [grep-pattern] - last 300 lines, optionally filtered
    if [ $# -gt 0 ]; then adbx logcat -d -t 300 | grep -iE "$1"; else adbx logcat -d -t 300; fi ;;

  help|*)
    grep -E '^\s+[a-z]+\)' "${BASH_SOURCE[0]}" | sed -E 's/\)//; s/#/->/' ;;
esac
