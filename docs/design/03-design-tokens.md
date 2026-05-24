# D3 — Design System Foundations (Tokens → Compose Theme)

> **TRI-50** · Design Overhaul (**TRI-47**). The layer everything else is built on: color (light +
> dark), type, spacing, radius, elevation, and the full token set, mapped to a Compose Multiplatform
> `Theme`. Serves D1's **P2** (honest, semantic data colors), **P3** (warmth), and **P6** (calm,
> restrained elevation).

---

## 0. Key finding — the foundation already exists; D3 ratifies it

Unlike a greenfield D3, LifePlannerhas a **mature, working token system on both sides**:

- **Design side:** `lifeplanner-assets/design/tokens.json` (Tokens-Studio format, `global/light/dark`)
  + `figma-variables-light.json` / `figma-variables-dark.json`.
- **Code side:** a complete Compose theme under `app/shared/src/commonMain/.../ui/theme/`.

And they **already agree** — e.g. `primary #4A6FFF`, `background #F8F9FC`, `text.primary #2C3345`,
and the entire 8dp `spacing` scale are byte-identical between `tokens.json` and the Compose objects.

So D3's job is **not to invent a parallel system** (that would create exactly the token drift we're
warned against). D3 **ratifies** the existing system as the foundation, **documents** it as the
single source of truth, and **flags the few real gaps** (§7) for D4/implementation. No working theme
code is rewritten here.

---

## 1. Token architecture — the source of truth

```
DESIGN (Figma / Tokens Studio)                 CODE (Compose Multiplatform)
  tokens.json  (global / light / dark)  ──►   ui/theme/
  figma-variables-{light,dark}.json            ├─ LifePlannerColors.kt   color tokens (light + Dark)
                                               ├─ LifePlannerTheme.kt    composition + modernColors accessor
                                               ├─ LifePlannerTypography.kt  type scale (Satoshi)
                                               ├─ LifePlannerShapes.kt   M3 shape scale
                                               ├─ DesignSystem.kt        spacing/radius/elevation/size/alpha
                                               ├─ CategoryColors.kt      per-GoalCategory color
                                               └─ Gradients.kt           gradient sets
```

| Token group | tokens.json | Compose symbol | File |
|---|---|---|---|
| Color (semantic) | `light.*` / `dark.*` | `ModernColors` → `ModernColorScheme` → `ModernThemeColors.Light/Dark` | `LifePlannerColors.kt` |
| Color access | — | `MaterialTheme.modernColors` (via `LocalModernColors`) | `LifePlannerTheme.kt` |
| Typography | `global.typography*` | `LifePlannerTypography()` (M3 `Typography`) | `LifePlannerTypography.kt` |
| Spacing | `global.spacing` | `LifePlannerDesign.Spacing` / `.Padding` | `DesignSystem.kt` |
| Radius | `global.radius*` | `LifePlannerDesign.CornerRadius` + `ModernShapes` | `DesignSystem.kt` / `LifePlannerShapes.kt` |
| Elevation | `global.elevation*` | `LifePlannerDesign.Elevation` | `DesignSystem.kt` |
| Icon / component sizes | `global.*` | `LifePlannerDesign.IconSize` / `.ComponentSize` | `DesignSystem.kt` |
| Alpha | `global.*` | `LifePlannerDesign.Alpha` | `DesignSystem.kt` |
| Category / gradients | `category.*` / `gradient.*` | `CategoryColors`, `Gradients`, `ModernColorScheme.gradient*` | `CategoryColors.kt` / `Gradients.kt` |

**Rule:** `tokens.json` is the **design** source of truth; the Compose objects are the **runtime**
source of truth; they must stay in lock-step. Neither a screen nor a component may hardcode a raw
hex or dp — always reference a token (§8).

---

## 2. Color tokens (light + dark)

Semantic roles (not raw colors). Light/dark pairs from `ModernColors` / `ModernColors.Dark`:

| Role | Light | Dark | Use |
|---|---|---|---|
| `primary` | `#4A6FFF` | `#6A87FF` | primary actions, brand |
| `primaryContainer` / `onPrimaryContainer` | `#ECF0FF` / `#0C2379` | `#1E2746` / `#D7E0FF` | tonal primary surfaces |
| `secondary` | `#7A5AF8` | `#9578FF` | secondary accents |
| `accent` (tertiary) | `#F86E5A` | `#FF8A7A` | warmth, highlights (P3) |
| `success` | `#28C76F` | `#3DD98B` | positive / on-track (P2) |
| `warning` | `#FF9F43` | `#FFBF75` | caution (P2) |
| `error` | `#EA5455` | `#FF7273` | errors — *never* "you failed" (P2) |
| `background` | `#F8F9FC` | `#121826` | app canvas |
| `surface` | `#FFFFFF` | `#1B2437` | cards, sheets |
| `surfaceVariant` | `#F0F2FA` | `#252E42` | chips, subtle fills |
| `textPrimary` | `#2C3345` | `#F5F6FA` | headings, body |
| `textSecondary` | `#6E7A94` | `#B0B7C9` | supporting text |
| `textTertiary` | `#9AA6BC` | `#8792AB` | hints, captions |
| `textDisabled` | `#CBD0DD` | `#5E6A84` | disabled |
| `divider` / `outline` / `outlineVariant` | `#E8ECF4` / `#CBD0DD` / `#E8ECF4` | `#2E3850` / `#5E6A84` / `#2E3850` | separators, borders |
| `scrim` | `#80000000` | `#B3000000` | modal scrim |

Plus component-semantic roles in `ModernColorScheme`: `cardBackground`, `chipBackground`, `chipText`,
`disabledBackground`, `disabledContent`, and six gradient pairs.

**Bridging:** `LifePlannerTheme` also builds a Material3 `ColorScheme` (`createColorScheme`) from
these, so stock M3 components (`Button`, `Switch`, …) inherit the palette. App components read the
richer set via `MaterialTheme.modernColors`.

**Category colors** (`CategoryColors.kt`) — one hue per goal area, used for goal/balance accents,
serving P3 warmth and quick recognition. ⚠️ naming drift — see §7.

---

## 3. Typography — Satoshi, on the Material 3 scale

`AppFontFamily` = **Satoshi** (Regular/Medium/Bold/Black). `LifePlannerTypography()` fills the full
M3 role set:

| Role | Size / line | Weight | Default color |
|---|---|---|---|
| displayLarge / Medium / Small | 57/64 · 45/52 · 36/44 | Normal | textPrimary |
| headlineLarge / Medium / Small | 32/40 · 28/36 · 24/32 | SemiBold | textPrimary |
| titleLarge / Medium / Small | 22/28 · 16/24 · 14/20 | SemiBold→Medium | textPrimary |
| bodyLarge / Medium / Small | 16/24 · 14/20 · 12/16 | Normal | primary / secondary / tertiary |
| labelLarge / Medium / Small | 14/20 · 12/16 · 11/16 | Medium | primary / secondary / tertiary |

⚠️ Type styles **bake a color in** (e.g. `bodyMedium` → `textSecondary`). Convenient, but couples
type to color and surprises when a `bodyMedium` needs a different color. Flagged in §7.

---

## 4. Spacing, radius, elevation, sizing (`LifePlannerDesign`)

All on an **8dp grid** (P6 — calm rhythm). Matches `tokens.json › global` exactly.

- **Spacing:** `none 0 · xxs 4 · xs 8 · sm 12 · md 16 · lg 20 · xl 24 · xxl 32` (+ `listItemGap 12`, `sectionGap 24`).
- **Padding (semantic):** `screenHorizontal/Vertical 16 · cardContent 16 · cardContentLarge 20`.
- **CornerRadius:** `extraSmall 8 · small 12 · medium 16 · large 20 · extraLarge 24 · full 100`.
- **Elevation:** `none 0 · low 2 · medium 4 · high 8` — "use sparingly; flat is preferred" (P6).
- **IconSize:** `xs 16 · sm 20 · md 24 · lg 32 · xl 48` (+ `emptyState 64 · avatar 72`).
- **ComponentSize:** `button 48 · chip 32 · input 56 · progressBar 8 · fab 56 · smallFab 40 · divider 1`.
- **Alpha:** `disabled .38 · medium .6 · high .87 · overlay .5 · containerLight .1 · containerMedium .2`.

## 5. Shapes

`ModernShapes` (M3) = `small 8 · medium 12 · large 16`. The **fuller** radius scale lives in
`LifePlannerDesign.CornerRadius` (up to `extraLarge 24` + `full`). Components needing 20/24/pill use
`CornerRadius` directly. ⚠️ two radius sources — reconcile in §7.

---

## 6. Theme composition & usage

```kotlin
LifePlannerTheme(darkTheme = …) {            // composes M3 ColorScheme + Typography + Shapes,
    // content                                // and provides LocalModernColors
}
// in a component:
val c = MaterialTheme.modernColors            // rich semantic palette
Box(Modifier.padding(LifePlannerDesign.Spacing.md))
```

⚠️ `darkTheme` currently **defaults to `true`** — the app ships dark-first. Confirm intent and wire
to system/user preference (§7).

---

## 7. Reconciliation findings & gaps (the audit)

| # | Finding | Severity | Status / Recommendation |
|---|---|---|---|
| G1 | **Category naming drift** — code showed "Money/Body/People/Wellbeing/Purpose" (incl. a buggy `name.lowercase()` derive and a duplicate hardcoded map) while `terminology.md` (canonical) + the Figma tokens say Financial/Physical/Social/Emotional/Spiritual. | **High** | ✅ **Done.** Added a canonical `GoalCategory.displayName` (single source of truth) and pointed `LifeArea`, `CoachPreviewCards`, onboarding, and the goal-generator at it. Enum constant/DB names unchanged. Cross-walk below. |
| G2 | **Dark-mode is the hardcoded default** (`darkTheme = true`), not tied to system or a user setting. | High | ✅ **Done (backing).** Added `ThemeController` (persists `ThemeMode` System/Light/Dark via `Settings`, default **System**) + root wiring in `App.kt` → appearance now follows the OS. The visible toggle lands with the Settings screen (D7). |

### Canonical category cross-walk (G1)

| `GoalCategory` (enum / DB — stable) | `displayName` (canonical, terminology.md) | Figma token key | Base color |
|---|---|---|---|
| `CAREER` | Career | `career` | `#4A6FFF` |
| `MONEY` | Financial | `financial` | `#28C76F` |
| `BODY` | Physical | `physical` | `#FF9F43` |
| `PEOPLE` | Social | `social` | `#7A5AF8` |
| `WELLBEING` | Emotional | `emotional` | `#00CFE8` |
| `PURPOSE` | Spiritual | `spiritual` | `#EA5455` |
| `FAMILY` | Family | `family` | `#F57C00`* |

\* `family` base color drifts between the Figma token (`#6236FF`) and `CategoryColors.FAMILY` (`#F57C00`) — a remaining minor color reconciliation for D4. Life Balance adds an 8th area, **Personal Growth**, mapping to Career.
| G3 | **Type styles embed color** (`bodyMedium → textSecondary`, etc.). | Medium | Prefer color applied at usage (or via M3 `onSurface*`); keep type role purely typographic. (D4) |
| G4 | **Two radius sources** (`ModernShapes` 3 steps vs `CornerRadius` 6). | Low | Treat `CornerRadius` as canonical; derive `ModernShapes` from it. (D4) |
| G5 | **Dimensions are a static object**, not provided via the theme/CompositionLocal like colors. | Low | Fine for KMP; optionally expose `LocalSpacing` for parity/testability. (D4) |
| G6 | **No explicit `onPrimary`/`onSurface`** in `ModernColorScheme` (relies on M3 scheme; light hardcodes `Color.White`). | Low | Add `onX` roles to the scheme for self-containment. (D4) |
| G7 | **tokens.json ↔ Compose parity is manual** — no generator. | Medium | Add a check (script/test) or a Tokens-Studio→Kotlin step so they can't silently drift. (D13 handoff) |

None blocks D4; G1 and G2 are the two worth doing early.

---

## 8. Governance rules

1. **No raw values in features.** Never a literal hex or dp in a screen/component — reference a
   token (`MaterialTheme.modernColors.*`, `LifePlannerDesign.*`). This is the rule D4 components and
   D7 screens are reviewed against.
2. **Light + dark parity.** Every new color token defines *both* light and dark; PRs without both are
   incomplete.
3. **Semantic, not literal.** Name by role (`success`, `cardBackground`), never by hue ("green").
4. **Change tokens in lock-step.** A value changes in `tokens.json` *and* the Compose object in the
   same PR (until G7 automates it).
5. **Spacing stays on the 8dp grid.** New spacing values must be grid multiples.

---

## 9. Handoff to D4

D4 (component library) consumes this layer directly: every button/card/chip/input/sheet is built
**only** from these tokens, specified across all states (default/pressed/focused/disabled/loading/
empty/error). D4 should also action **G1–G4** as it formalizes components, and define `onX` color
roles (G6) where components need legible foreground colors.

*Next: **D4 — Component library (all states)**, built on these tokens.*
