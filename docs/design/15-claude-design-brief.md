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

## 2. Identity: Warm Ink

Warm paper and ink neutrals with a brass primary. Reads calm, premium, and grown-up. The reference
points are Oura (trustworthy data, restraint) and Finch (warmth), explicitly **without** Finch's
cuteness: this is a serious tool for adults.

Avoid the generic SaaS indigo/violet look. That was v2 and it is what we moved away from.

### Light

| Role | Hex | Use |
|---|---|---|
| `background` | `#FAF7F2` | app canvas (warm paper, never pure white) |
| `surface` | `#FFFFFF` | cards, sheets |
| `surfaceVariant` | `#F1EBE1` | chips, subtle fills |
| `primary` | `#A65A2E` | primary actions, brand |
| `primaryContainer` | `#F7E7D8` | tonal primary surfaces |
| `secondary` | `#3F5A50` | secondary accents (muted teal-green) |
| `accent` | `#C98A3F` | warmth, highlights |
| `textPrimary` | `#211C16` | headings, body (warm ink, never pure black) |
| `textSecondary` | `#6A6055` | supporting text |
| `textTertiary` | `#988D80` | hints, captions |
| `success` | `#4A7C52` | on track |
| `warning` | `#B5802C` | caution |
| `error` | `#A9453B` | errors, never "you failed" |
| `divider` | `#EAE2D6` | separators |

### Dark

| Role | Hex |
|---|---|
| `background` | `#16130F` |
| `surface` | `#1E1A15` |
| `surfaceVariant` | `#2A251E` |
| `primary` | `#D98B57` |
| `secondary` | `#7FA394` |
| `accent` | `#E0A85F` |
| `textPrimary` | `#F2EBE1` |
| `textSecondary` | `#B5AA9B` |
| `success` / `warning` / `error` | `#79B183` / `#D8A44E` / `#D9776B` |

### Hero gradient, by time of day

The one surface that shifts with the hour. Everything else stays fixed so contrast never drifts.
Use it behind the page hero only, always with white text.

| Phase | Hours | From → To |
|---|---|---|
| Dawn | 05–08 | `#8F4A32` → `#C4794A` |
| Day | 09–16 | `#8A4A25` → `#BC7F37` |
| Dusk | 17–21 | `#6B3524` → `#B0603A` |
| Night | 22–04 | `#241A14` → `#5E3320` |

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
- Lead with the gradient hero; keep the rest calm and flat.
- Show the reason behind a number ("Based on 14 days, medium confidence").
- Use warm neutrals; let brass be the only strong hue on the page.
- Write plainly and kindly. Short sentences.

**Do not**
- Use indigo/violet SaaS accents, pure white backgrounds, or pure black text.
- Add streak-anxiety mechanics, leaderboards, guilt framing, or manufactured urgency.
- Stack heavy drop shadows or use more than one strong accent per screen.
- Invent a number the app could not actually know.
