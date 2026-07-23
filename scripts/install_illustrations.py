#!/usr/bin/env python3
"""Install Dotion empty-state SVGs into composeResources with dark variants.

The source art is a greyscale ramp (near-black linework down to light greys on
white). That reads correctly on a light background but disappears on a dark one,
so a luminance-inverted copy goes into drawable-dark/ and Compose Resources picks
the right one per theme automatically.
"""
import re
import shutil
import sys
from pathlib import Path

SRC = Path(sys.argv[1])          # .../Dotion Files/SVG
DST = Path(sys.argv[2])          # .../composeResources

# number -> semantic name (subjects identified from the rendered contact sheet)
NAMES = {
    "01": "illus_empty_search",
    "02": "illus_empty_cart",
    "03": "illus_empty_inbox",
    "04": "illus_error_not_found",
    "05": "illus_empty_bank",
    "06": "illus_empty_calendar",
    "07": "illus_state_under_construction",
    "08": "illus_empty_map",
    "09": "illus_empty_journal",
    "10": "illus_empty_box",
    "11": "illus_error_offline",
    "12": "illus_empty_wallet",
    "13": "illus_empty_decisions",
    "14": "illus_empty_chat",
    "15": "illus_empty_reminders",
    "16": "illus_empty_transactions",
    "17": "illus_empty_folder",
    "18": "illus_state_waiting",
    "19": "illus_state_sync",
    "20": "illus_empty_goals",
    "21": "illus_empty_photos",
    "22": "illus_empty_payment",
    "23": "illus_state_locked",
    "24": "illus_empty_schedule",
}

# Luminance inversion. Darks become light linework, the white "paper" becomes a
# dark surface, and the mid greys keep their relative separation.
DARK_MAP = {
    "#1b1b1b": "#EDEDED",
    "#221f20": "#EDEDED",
    "#231b1b": "#EDEDED",
    "#221f1f": "#EDEDED",
    "#393637": "#C9C9C9",
    "#0a0101": "#EDEDED",
    "black": "#EDEDED",
    "#a8a8a8": "#7A7A7A",
    "#999999": "#7A7A7A",
    "#e2e2e2": "#3A3A3A",
    "#d3d3d3": "#4A4A4A",
    "white": "#1C1C1E",
}

ATTR = re.compile(r'\b(fill|stroke)="([^"]*)"')


def to_dark(svg: str) -> str:
    def sub(m):
        attr, val = m.group(1), m.group(2)
        key = val.strip().lower()
        if key in ("none", ""):
            return m.group(0)
        return f'{attr}="{DARK_MAP.get(key, val)}"'

    return ATTR.sub(sub, svg)


light_dir = DST / "drawable"
dark_dir = DST / "drawable-dark"
light_dir.mkdir(parents=True, exist_ok=True)
dark_dir.mkdir(parents=True, exist_ok=True)

count = 0
unmapped = set()
for num, name in sorted(NAMES.items()):
    src = SRC / f"{num}.svg"
    if not src.exists():
        raise SystemExit(f"missing source {src}")
    svg = src.read_text()

    shutil.copyfile(src, light_dir / f"{name}.svg")
    (dark_dir / f"{name}.svg").write_text(to_dark(svg))

    for m in ATTR.finditer(svg):
        v = m.group(2).strip().lower()
        if v not in DARK_MAP and v not in ("none", ""):
            unmapped.add(v)
    count += 1

print(f"installed {count} illustrations -> drawable/ + drawable-dark/")
if unmapped:
    print(f"WARNING unmapped colors (left unchanged in dark): {sorted(unmapped)}")
else:
    print("every colour in the source had a dark mapping")
