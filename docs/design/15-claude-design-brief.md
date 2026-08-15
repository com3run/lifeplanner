# D15 - LifePlanner design brief for Claude Design

> **Paste this into a Claude Design project before asking it for screens.**
>
> Claude Design builds with generic components by default. It has no knowledge of LifePlanner, so
> without this brief its output looks nothing like the app. This file is the design language in
> the form a design agent can act on.
>
> **Every value here is generated from or verified against the code.** Colors come from
> `ui/theme/VisualIdentity.kt` via `scripts/generate-design-tokens.py`; scales come from
> `ui/theme/DesignSystem.kt`; component names were each checked to exist. When the code changes,
> re-run the script and update this file rather than editing values by hand.

---

## 1. What the product is

LifePlanner is a **life-planning agent for adults**: goals, habits, and a coach that helps you
choose deliberately. It is not a task manager and not a habit-streak game.

Three things follow from that, and they should be visible in any screen you design:

- **The user steers.** The app shows options and reasons; it never commands. Copy is "you could",
  not "you must".
- **Honesty about data.** When the app is uncertain, it says so. A number with a confidence band
  beats a false-precise number.
- **A missed day is "life happens", never guilt.** No streak panic, no red shaming.

## 2. Identity: v2 (indigo)

A clean, modern indigo/violet system on soft near-white surfaces. Reads friendly, bright, and
capable. Primary is a vivid indigo; a violet secondary and coral accent add warmth. This is the
palette the app currently ships (`ACTIVE_VISUAL_IDENTITY = CLASSIC`), so designs made from it drop
straight onto the real screens.

### Light

| Role | Hex | Use |
|---|---|---|
| `background` | `#F8F9FC` | app canvas (soft near-white) |
| `surface` | `#FFFFFF` | cards, sheets |
| `surfaceVariant` | `#F0F2FA` | chips, subtle fills |
| `primary` | `#4A6FFF` | primary actions, brand (indigo) |
| `primaryContainer` | `#ECF0FF` | tonal primary surfaces |
| `secondary` | `#7A5AF8` | secondary accents (violet) |
| `accent` | `#F86E5A` | warmth, highlights (coral) |
| `textPrimary` | `#2C3345` | headings, body |
| `textSecondary` | `#6E7A94` | supporting text |
| `textTertiary` | `#9AA6BC` | hints, captions |
| `success` | `#28C76F` | on track (green) |
| `warning` | `#FF9F43` | caution (amber) |
| `error` | `#EA5455` | errors, never "you failed" |
| `divider` | `#E8ECF4` | separators |

### Dark

| Role | Hex |
|---|---|
| `background` | `#121826` |
| `surface` | `#1B2437` |
| `surfaceVariant` | `#252E42` |
| `primary` | `#6A87FF` |
| `secondary` | `#9578FF` |
| `accent` | `#FF8A7A` |
| `textPrimary` | `#F5F6FA` |
| `textSecondary` | `#B0B7C9` |
| `success` / `warning` / `error` | `#3DD98B` / `#FFBF75` / `#FF7273` |

### Hero gradient

A single fixed indigo→violet gradient behind the page hero. Always white text on it.

| From → To |
|---|
| `#667EEA` → `#764BA2` |

## 3. Type

**Satoshi** (Regular / Medium / Bold / Black) on the Material 3 scale.

| Role | Size / line | Weight |
|---|---|---|
| `headlineLarge` / `Medium` / `Small` | 32/40 · 28/36 · 24/32 | SemiBold |
| `titleLarge` / `Medium` / `Small` | 22/28 · 16/24 · 14/20 | SemiBold → Medium |
| `bodyLarge` / `Medium` / `Small` | 16/24 · 14/20 · 12/16 | Regular |
| `labelLarge` / `Medium` / `Small` | 14/20 · 12/16 · 11/16 | Medium |

Eyebrow labels above hero titles are `labelSmall`, uppercase, letter-spaced (e.g. "YOUR SPACE",
"DO NEXT", "FEELING STUCK?").

## 4. Space, radius, elevation

Everything on an **8dp grid**.

- **Spacing:** `xxs 4 · xs 8 · sm 12 · md 16 · lg 20 · xl 24 · xxl 32`
- **Screen padding:** 16 horizontal. **Card content padding:** 16.
- **Radius:** `extraSmall 8 · small 12 · medium 16 · large 20 · extraLarge 24 · full 100` (pill).
  Cards use `large` (20); heroes use `extraLarge` (24).
- **Elevation:** `none 0 · low 2 · medium 4 · high 8`. Use sparingly. **Flat is preferred** — depth
  comes from the warm surface contrast, not drop shadows.
- **Icons:** Phosphor, `xs 16 · sm 20 · md 24 · lg 32 · xl 48`.

## 5. Component vocabulary

Build from these. Each exists in the app, so a design made of them maps onto real code.

| Component | What it is |
|---|---|
| `GradientHero` | Page header: eyebrow + title + subtitle on the time-of-day gradient, optional trailing slot (usually a `ProgressRing`). Every main screen leads with one. |
| `GlassCard` | The standard card. Surface fill + hairline border, no heavy shadow. |
| `AppButton` | Primary / secondary / text variants, 48dp tall, pill radius, optional leading icon. |
| `StateView` | Empty / error / loading states: icon + title + message + optional action. Every screen should ship all three. |
| `ProgressRing` | Circular progress, used in heroes and on goal detail. |
| `IconChip` | Small rounded icon tile, used as a card's leading element. |
| `InsightCard` | A data insight **with its confidence and sample size shown**. |
| `StatTile` | A single labelled number. |

## 6. Screen archetypes

- **For You (home).** Gradient hero with greeting + level ring, then filter chips
  (All / Right now / Reflect on today / Learn), then a ranked feed of cards. Each card is an
  eyebrow + title + one supporting line + chevron. Sections: "Right now", "Reflect on today",
  "Learn".
- **Goals.** Hero + list of goal cards with progress. Empty state uses `StateView`.
- **You.** Hero + **grouped** sections (Identity / Insights / Decisions / Growth / Coach /
  Appearance / Settings), each a card of labelled rows with an icon tile and a chevron. Never a
  flat undifferentiated settings list.

Navigation is **three tabs**: Today · Goals · You, plus one context-aware "+" (capture on Today,
new goal on Goals, hidden on You).

## 7. Rules

**Do**
- Lead with the indigo→violet gradient hero; keep the rest calm and flat.
- Show the reason behind a number ("Based on 14 days, medium confidence").
- Let indigo `primary` be the main hue; use the violet secondary and coral accent sparingly.
- Write plainly and kindly. Short sentences.

**Do not**
- Add streak-anxiety mechanics, leaderboards, guilt framing, or manufactured urgency.
- Stack heavy drop shadows or use more than one strong accent per screen.
- Invent a number the app could not actually know.
