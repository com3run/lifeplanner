# D4 — Component Library (all states)

> **TRI-51** · Design Overhaul (**TRI-47**). The reusable component set every redesigned screen
> (D7) is built from — specified across **all states** and built **only** from D3 tokens (no raw
> hex/dp). Serves **P6** (consistent, calm), **P2** (every component has a designed empty/error
> state), **P3** (no dark-pattern components).

---

## 1. The reality today

`ui/components/` holds **62 files** — but they're a mix of two very different things:

| Kind | Examples | D4 stance |
|---|---|---|
| **Design-system primitives** (reusable atoms) | `ModernCards`/`GlassCard`, `EmptyStateCard`, `BottomNavigationBar`, `ModernTopAppBar`, `QuickStatCard`, `SyncStatusIndicator`, `OfflineBanner` | **standardize** — these are the library |
| **Feature components** (compose primitives for one feature) | `GoalCard`, `HabitCard`, `Dashboard*Widgets` (8 files), `CoachPreviewCards`, `MoodCalendar`, `StreakCounter` | **out of scope** — they *consume* primitives; D7 owns them |

**Two findings that matter:**
1. **No canonical button.** Buttons are ~**345 ad-hoc** `Button`/`OutlinedButton`/`TextButton`
   call sites, each restyled inline. Biggest source of inconsistency. → **Fixed:** `AppButton`
   shipped this cycle (see §4.1).
2. **`GlassCard` hard-locks dark** (`val isDark = true` inside it), so it ignores the new theme
   setting (D3/G2). → flagged in §7; fix is one line, but it touches the app's most-used card so
   it's verified in D7.

---

## 2. The canonical primitive set

The atoms D4 standardizes (everything else composes these):

| Primitive | Today | D4 action |
|---|---|---|
| **Button** | 345 ad-hoc M3 calls | ✅ `AppButton` (4 variants, all states) — shipped |
| **Card** | `GlassCard` / `ModernCards` | adopt as canonical; fix dark-lock (§7) |
| **Bottom sheet** | `ModalBottomSheet` ad-hoc (ChoicePoint, AddGoal, …) | wrap as `AppBottomSheet` (consistent scrim/handle/padding) — D7 |
| **Text field / input** | stock M3 `OutlinedTextField` ad-hoc | `AppTextField` (label/helper/error/disabled) — D7 |
| **Chip** | `FilterChip` ad-hoc | `AppChip` (selected/unselected/disabled) — D7 |
| **List row** | bespoke per screen | `AppListRow` (leading/title/subtitle/trailing/press) — D7 |
| **Nav bar** | `BottomNavigationBar` / `NavigationRailBar` | adopt as canonical (3-tab IA from D2) |
| **Progress** | `CircularProgressIndicator` / bars ad-hoc | `AppProgress` (determinate/indeterminate) — D7 |
| **Snackbar / toast** | ad-hoc | `AppSnackbar` (info/success/error, P3 tone) — D7 |
| **Empty state** | `EmptyStateCard` / `EmptyGoalsView` | adopt `EmptyStateCard` as canonical |
| **Banner** | `OfflineBanner` / `HintBanner` | adopt; align to tokens |

**Build strategy:** `AppButton` is the reference implementation. The rest are built **incrementally
as D7 screens need them**, each against the spec below — not all up front (avoids speculative,
unused components; matches P6).

---

## 3. The universal state matrix

Every interactive/content primitive must define these states (the ones that apply to it):

| State | Meaning | Applies to | Token treatment |
|---|---|---|---|
| **default** | resting | all | base role colors |
| **pressed** | finger down | interactive | M3 ripple (no custom needed) |
| **focused** | keyboard/d-pad/a11y | interactive | M3 focus indicator; `outline` ring |
| **disabled** | not actionable | interactive | `disabledBackground` / `disabledContent` (alpha .38) |
| **loading** | async in flight | buttons, lists, screens | spinner in `contentColor`; action inert |
| **empty** | no data yet | lists, cards, screens | `EmptyStateCard` — friendly, never a dead end (P2/P3) |
| **error** | something failed | inputs, lists, screens | `error` role + a *recovery* affordance, never blame (P2) |

Rule: **empty and error are designed, first-class states — not afterthoughts.** A screen without an
empty and error state is incomplete (this is a D7 review gate, and a D12 concern).

---

## 4. Per-primitive specs

### 4.1 `AppButton` (shipped — the reference)
- **Variants:** `PRIMARY` (filled, `primary`/white) · `SECONDARY` (tonal, `primaryContainer`/
  `onPrimaryContainer`) · `TERTIARY` (ghost, transparent/`primary`) · `DESTRUCTIVE` (`error`/white).
- **States:** default · pressed (M3 ripple) · focused (M3) · disabled (`disabledBackground`/
  `disabledContent`) · **loading** (18dp spinner in content color, click inert, keeps filled look —
  no jarring gray-out).
- **Tokens:** height `ComponentSize.buttonHeight` (48 — also the a11y min target) · radius
  `CornerRadius.small` · label `typography.labelLarge` · icon `IconSize.small` · gap `Spacing.xs`.
- **API:** `AppButton(text, onClick, modifier, variant, enabled, loading, leadingIcon)`.
- **Not applicable:** empty/error (container-level states, not button states).

### 4.2 Card (`GlassCard` → canonical)
- Radius `CornerRadius.large`; surface `cardBackground`; elevation from `Elevation` (prefer `low`,
  flat-first per P6); 1dp `outlineVariant` border in light.
- **Fix required:** read the real theme instead of `isDark = true` (§7).
- States: default · pressed (if clickable) · disabled (reduced alpha). Content owns empty/error.

### 4.3 `AppTextField` (to build)
- Anatomy: label · field · helper/counter · leading/trailing icon.
- States: default · focused (`primary` outline) · filled · **error** (`error` outline + message) ·
  disabled. Height `ComponentSize.inputFieldHeight` (56); radius `CornerRadius.small`.

### 4.4 `AppChip` (to build)
- States: unselected (`surfaceVariant`/`textSecondary`) · selected (`primaryContainer`/
  `onPrimaryContainer`) · disabled. Height `ComponentSize.chipHeight` (32); pill radius.

### 4.5 `AppListRow`, `AppBottomSheet`, `AppProgress`, `AppSnackbar`
Specced briefly here; built in D7 to the same matrix (full anatomy added when built).

---

## 5. Token bindings & rules (actions D3 gaps)

1. **No raw values.** Every component reads color from `MaterialTheme.modernColors`, size from
   `LifePlannerDesign.ComponentSize`/`IconSize`, spacing from `Spacing`, radius from `CornerRadius`,
   type from the M3 scale. `AppButton` is the worked example. *(PR review gate.)*
2. **G3 — decouple type from color.** Components set color explicitly (as `AppButton` does:
   `Text(style = labelLarge, color = content)`), rather than relying on the color baked into the
   type style. Type roles should become purely typographic over time.
3. **G4 — one radius source.** Components use `LifePlannerDesign.CornerRadius` (6 steps);
   `ModernShapes` (M3, 3 steps) is derived from it, not a parallel scale.
4. **G6 — `onX` foreground roles.** Where a component needs a legible foreground on a colored
   surface, add explicit `onPrimary`/`onError`/… to `ModernColorScheme` (today `AppButton` uses
   `Color.White` on primary/error — acceptable, but a token is better). Tracked for the color layer.

---

## 6. Accessibility (the floor for every primitive)

- **Touch targets ≥ 48dp** (`buttonHeight` already meets this; small icon buttons must pad to 48).
- **Contrast ≥ 4.5:1** for text, **3:1** for large text / icons — verify each variant in light+dark.
- **State is not color-only** — disabled/error/selected also carry shape, icon, or text cues.
- **Every control has a `contentDescription`/semantics**; loading announces busy.
- Full WCAG 2.1 AA pass is **D12**; D4 sets the per-component floor.

---

## 7. Findings to fix

| # | Finding | Action (owner) |
|---|---|---|
| C1 | `GlassCard` hard-locks `isDark = true` → ignores the theme setting (D3/G2). | Read real theme (`isSystemInDarkTheme()`/controller); verify the app's most-used card in both modes. (D7) |
| C2 | ~345 ad-hoc button call sites. | Migrate to `AppButton` screen-by-screen during D7; lint/grep gate against new raw `Button(`. |
| C3 | Bottom sheets, inputs, chips styled inline per screen. | Introduce `AppBottomSheet`/`AppTextField`/`AppChip` as D7 reaches each. |

---

## 8. Handoff to D7

D7 builds the Today / Goals / You canvases (D2) from these primitives, building each remaining
primitive on first need and migrating ad-hoc usages to the canonical ones. Every screen ships its
**empty and error** states (§3). `AppButton` is the template for what "done, all states, token-pure"
looks like.

*Next: **D5 — Visual identity (iconography, illustration, warmth)** [Figma-side], or **D7 — Core
screen redesigns** if we prioritize shipping screens on the new primitives.*
