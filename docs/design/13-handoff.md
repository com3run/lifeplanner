# D13 — Design Handoff & Compose Specs

> **TRI-60** · Design Overhaul (**TRI-47**) — the capstone. One entry point for building on the
> redesign: what exists, where it lives in code, how to use it, and what's still open. Unusually for
> a "handoff," most of this system is **already implemented** (commonMain Compose), so this maps
> docs → code rather than throwing specs over a wall.

---

## 1. The system at a glance

| Layer | Doc | Code |
|---|---|---|
| Principles / north star | `01-principles-and-benchmarks.md` | — |
| IA & navigation (3 tabs) | `02-information-architecture.md` | `BottomNavItem.kt` (target) |
| **Tokens** (color, type, space, radius, elevation) | `03-design-tokens.md` | `ui/theme/` — `LifePlannerColors`, `LifePlannerTypography`, `DesignSystem`, `LifePlannerShapes`, `LifePlannerTheme` |
| **Components** | `04-component-library.md` | `ui/components/` — `AppButton`, `PremiumComponents`, `DataViz`, `StateView` |
| Data-viz | `08-data-viz.md` | `ui/components/DataViz.kt` |
| Motion | `10-motion.md` | `ui/theme/Motion.kt` |
| Motivation | `09-motivation-emotional-design.md` | — (governs gamification) |
| A11y / states / copy | `12-accessibility-states-copy.md` | `ui/components/StateView.kt` |
| Onboarding | `11-onboarding.md` | `ui/onboarding/OnboardingFlowScreen.kt` |
| Screens | (D7) `07-today-screen.md` | `ui/today/`, `ui/goals/`, `ui/you/` |

## 2. Foundations — quick reference

- **Color:** `MaterialTheme.modernColors.*` (semantic roles, light + dark; bridged to M3 `ColorScheme`). Theme: `LifePlannerTheme(darkTheme)`; preference via `ThemeController` (System/Light/Dark, G2).
- **Type:** the M3 scale on **Satoshi** (`LifePlannerTypography`).
- **Dimensions:** `LifePlannerDesign.{Spacing, Padding, CornerRadius, Elevation, IconSize, ComponentSize, Alpha}` — 8dp grid.
- **Rule:** never a raw hex or dp in a screen/component — always a token (the review gate).

## 3. Component package (props · states)

| Component | File | Key props | States |
|---|---|---|---|
| `AppButton` | `AppButton.kt` | text, onClick, variant (PRIMARY/SECONDARY/TERTIARY/DESTRUCTIVE), enabled, loading, leadingIcon | default · pressed · focused · disabled · loading |
| `GradientHero` | `PremiumComponents.kt` | title, subtitle?, eyebrow?, gradient, trailing? | — (banner) |
| `IconChip` | `PremiumComponents.kt` | icon, tint, boxSize | — |
| `ProgressRing` | `DataViz.kt` | progress (0–1), diameter, strokeWidth, color, trackColor, content? | animated |
| `InsightCard` | `DataViz.kt` | headline, detail?, confidenceLabel?, icon?, accent | — (honest confidence) |
| `StatTile` | `DataViz.kt` | value, label, accent | — |
| `StateView` | `StateView.kt` | title, message, icon?, loading, actionLabel?, onAction? | empty · error · loading |
| `bouncyClickable` | `Motion.kt` | enabled, onClick (Modifier) | press-scale |

Existing primitives also adopted: `GlassCard`/`ModernCards`, `BottomNavigationBar`/`NavigationRailBar`, `ModernTopAppBar`, `EmptyStateCard`.

## 4. Motion — quick reference

`Motion.Duration` (fast 120 / medium 240 / slow 400), `Motion.standard` / `emphasized` easing,
`Motion.pressScale`. Use `Modifier.bouncyClickable {}` on cards/rows. Reserve animation for feedback
and meaningful transitions (P6); honor reduce-motion (D12).

## 5. Screens built (preview routes today)

`Today` · `Goals` · `You` · `OnboardingFlow` are live behind **preview routes** (Profile → "New"),
additive and zero-risk to the current shell. Each: gradient hero, token-pure cards, `AppButton`,
press feedback, empty/error states.

**Promotion (the remaining wiring):** replace the bottom-tab content (Life→Today, Hub→Goals,
Profile→You) per D2's 3-tab model, fold the old widgets in, then delete the preview routes. Hold
until validated / the pillar stack merges (so Today ships with real Possibilities + Choice Points).

## 6. How to build a new screen (the recipe)

1. `Scaffold(containerColor = modernColors.background)` + a `TopAppBar` (token colors).
2. Open with a `GradientHero` (or a clear title) — most-relevant-first (D2).
3. Group content in token-pure cards; rows use `IconChip` + `bouncyClickable`.
4. Numbers via `ProgressRing` / `InsightCard` / `StatTile` — always show confidence for inferred data (P2).
5. Ship **empty + error + loading** via `StateView`.
6. Actions via `AppButton`.
7. Copy in the D12 voice (warm, agency-affirming, no guilt).
8. **Review gates:** token-pure (no raw hex/dp) · light **and** dark verified · empty/error states present · ≥48dp targets · semantics on every control.

## 7. Cross-platform

Everything above is **commonMain** Compose Multiplatform — Android and iOS share one source of truth;
no per-platform UI code. Tokens map to one `Theme`; iOS gets the redesign for free.

## 8. What's still open (seams & follow-ups)

- **D5 — visual identity** (custom icons, illustration, brand warmth): needs Figma; today we use
  Phosphor icons + gradients.
- **D6 — signature interaction** (Why-Chain reveal): needs **Pillar 1** on `main`.
- **Pillar integration seams:** Today's "Right now you could…" → Pillar 2 `PossibilityEngine`;
  Choice Points → Pillar 3; insights-first → Pillar 4 `CausalInsightEngine` + the "Easier by
  default" epic (TRI-71); onboarding values → Pillar 1 `LifeValue`.
- **Carried fixes:** `GlassCard` dark-lock (D4 C1), migrate ~345 ad-hoc buttons to `AppButton`
  (C2), decouple type-from-color (G3), single radius source (G4), `onX` color roles (G6),
  tokens.json↔Compose drift check (G7).
- **`terminology.md`** route list → update to D2's map when screens are promoted.
- **Full WCAG audit** pass once promoted (D12).

## 9. Definition of done

- **A screen:** built on tokens + components, all states, light+dark, a11y gates, D12 copy.
- **The epic:** D1–D13 complete *and* the redesign promoted into the live shell with the pillar
  integrations wired — i.e. when "preview routes" become the real Today/Goals/You.

*The design system, the four core screens, and the full spec set are in place. Promotion + D5/D6
are the remaining path to a shipped redesign.*
