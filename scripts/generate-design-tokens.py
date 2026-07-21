#!/usr/bin/env python3
"""Generate design tokens from the Compose theme, for Claude Design.

Figma is gone (2026-07-18) and with it the hand-maintained `figma-variables-*.json`
exports. The direction of truth is now one-way:

    ui/theme/VisualIdentity.kt  ->  lifeplanner-assets/design/tokens.json

Code is authoritative. This script re-derives the design-side tokens from it, so the
two cannot drift the way `family` (#6236FF vs #F57C00) used to. Never hand-edit
tokens.json; edit the Kotlin and re-run this.

Usage:  python3 scripts/generate-design-tokens.py [--identity WARM_INK] [--check]

  --check  exit 1 if tokens.json is stale instead of writing it (for CI)
"""

import argparse
import json
import pathlib
import re
import sys

REPO = pathlib.Path(__file__).resolve().parent.parent
SOURCE = REPO / "app/shared/src/commonMain/kotlin/az/tribe/lifeplanner/ui/theme/VisualIdentity.kt"
# CLASSIC (v2) colors live in ModernColors, not VisualIdentity's Palette blocks.
CLASSIC_SOURCE = REPO / "app/shared/src/commonMain/kotlin/az/tribe/lifeplanner/ui/theme/LifePlannerColors.kt"
OUT = REPO.parent / "lifeplanner-assets/design/tokens.json"

# Palette object names in VisualIdentity.kt, per identity.
IDENTITIES = {
    "WARM_INK": ("WarmInkLight", "WarmInkDark"),
    "SAGE": ("SageLight", "SageDark"),
    "CLASSIC": None,  # special-cased: reads ModernColors (the frozen v2 palette)
}

# The roles the brief/tokens care about, in the order they appear in ModernColors.
CLASSIC_ROLES = [
    "primary", "primaryContainer", "secondary", "accent", "success", "warning", "error",
    "background", "surface", "surfaceVariant",
    "textPrimary", "textSecondary", "textTertiary", "textDisabled", "divider",
]
# CLASSIC's hero gradient is LifePlannerGradients.primary (static, no time-of-day shift).
CLASSIC_HERO = {"day": {"from": "#667EEA", "to": "#764BA2"}}


def hexed(argb: str) -> str:
    """0xAARRGGBB (as captured, without the 0x) -> #RRGGBB."""
    return "#" + argb[2:].upper()


def read_palette(src: str, name: str) -> dict:
    m = re.search(r"private val %s = Palette\((.*?)\)\.scheme\(\)" % name, src, re.S)
    if not m:
        sys.exit(f"error: palette {name} not found in {SOURCE.name}")
    fields = dict(re.findall(r"(\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)", m.group(1)))
    if not fields:
        sys.exit(f"error: palette {name} parsed but had no Color(0x...) fields")
    return fields


def read_hero(src: str, identity: str) -> dict:
    m = re.search(
        r"VisualIdentity\.%s -> Brush\.linearGradient\((.*?)\n        \)" % identity, src, re.S
    )
    if not m:
        sys.exit(f"error: hero gradient for {identity} not found")
    stops = re.findall(
        r"DayPhase\.(\w+) -> listOf\(Color\(0x([0-9A-Fa-f]{8})\), Color\(0x([0-9A-Fa-f]{8})\)\)",
        m.group(1),
    )
    return {p.lower(): {"from": hexed(a), "to": hexed(b)} for p, a, b in stops}


def read_classic() -> tuple:
    """Parse ModernColors (v2/CLASSIC): top-level object = light, nested `object Dark` = dark."""
    src = CLASSIC_SOURCE.read_text()
    dark_start = src.index("object Dark")
    light_src, dark_src = src[:dark_start], src[dark_start:]

    def grab(text):
        found = dict(re.findall(r"val (\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)", text))
        return {role: found[role] for role in CLASSIC_ROLES if role in found}

    light, dark = grab(light_src), grab(dark_src)
    missing = [r for r in CLASSIC_ROLES if r not in light or r not in dark]
    if missing:
        sys.exit(f"error: CLASSIC roles missing from ModernColors: {missing}")
    return light, dark


def build(identity: str) -> dict:
    if identity == "CLASSIC":
        light, dark = read_classic()
        hero = CLASSIC_HERO
        source_note = "the frozen v2 palette in LifePlannerColors.kt (ModernColors)"
    else:
        src = SOURCE.read_text()
        light_name, dark_name = IDENTITIES[identity]
        light, dark = read_palette(src, light_name), read_palette(src, dark_name)
        hero = read_hero(src, identity)
        source_note = "VisualIdentity.kt"

    def colors(p):
        return {k: {"value": hexed(v), "type": "color"} for k, v in p.items()}

    return {
        "$description": (
            f"LifePlanner '{identity}' identity. GENERATED from {source_note} "
            "by scripts/generate-design-tokens.py. Do not hand-edit; edit the Kotlin and re-run."
        ),
        "global": {
            "heroGradient": {
                phase: {
                    "from": {"value": v["from"], "type": "color"},
                    "to": {"value": v["to"], "type": "color"},
                }
                for phase, v in hero.items()
            }
        },
        "light": {"color": colors(light)},
        "dark": {"color": colors(dark)},
        "$themes": [
            {"id": "light", "name": "Light",
             "selectedTokenSets": {"global": "enabled", "light": "enabled"}},
            {"id": "dark", "name": "Dark",
             "selectedTokenSets": {"global": "enabled", "dark": "enabled"}},
        ],
    }


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--identity", default="WARM_INK", choices=sorted(IDENTITIES))
    ap.add_argument("--check", action="store_true",
                    help="exit 1 if tokens.json is stale instead of writing")
    args = ap.parse_args()

    if not SOURCE.exists():
        sys.exit(f"error: {SOURCE} not found")

    payload = json.dumps(build(args.identity), indent=2) + "\n"

    if args.check:
        current = OUT.read_text() if OUT.exists() else ""
        if current != payload:
            sys.exit(f"tokens.json is stale: re-run {pathlib.Path(__file__).name}")
        print("tokens.json is up to date")
        return

    if not OUT.parent.exists():
        sys.exit(f"error: {OUT.parent} not found (is lifeplanner-assets checked out alongside?)")
    OUT.write_text(payload)
    data = json.loads(payload)
    print(
        f"wrote {OUT}\n"
        f"  identity: {args.identity}\n"
        f"  light: {len(data['light']['color'])} tokens"
        f"  dark: {len(data['dark']['color'])} tokens"
        f"  hero bands: {len(data['global']['heroGradient'])}"
    )


if __name__ == "__main__":
    main()
