# D14 - v3 Gap Analysis and Safe Rollout Plan

> Written 2026-07-18. Answers two questions from the product owner:
> 1. "The UI almost changed nothing understandable." Why?
> 2. "I want to test version 3 without breaking things like I did in version 2." How?
>
> Every claim below is cited to `file:line` and was verified against the working tree at
> branch `com3run/tri-20-possibility-mode`.

---

## 1. Why v3 looks like v2

The redesign changed **which screens the tabs point at**. It never changed **what the app looks
like**. Three independent causes, in order of impact.

### 1.1 D3 deliberately kept the v2 visual system

`03-design-tokens.md:11-27` states the decision explicitly: *"LifePlanner has a mature, working
token system... D3's job is not to invent a parallel system... No working theme code is
rewritten here."* Primary stays `#4A6FFF`, background stays `#F8F9FC`, type stays Satoshi, spacing
stays the same 8dp ladder.

D3 was executed faithfully and therefore produced, by design, zero pixels of change.

Git confirms it. `ui/theme/LifePlannerColors.kt`, `LifePlannerTypography.kt`, `DesignSystem.kt`
and `LifePlannerShapes.kt` were last touched in `7d1c14b` (2026-05-21), which is the module split
refactor, three days **before** the design epic's first commit. The only files added to
`ui/theme/` across the entire epic are `Motion.kt` (59 lines), `ThemeController.kt` (32) and
`ThemeMode.kt` (4). That is 95 lines, none of them color, type, spacing or radius.

Same palette, same font, same grid, same corner radii. The app looking unchanged is the correct
and predicted outcome of the plan as written.

### 1.2 The two docs that would have changed the look were never written

`docs/design/` jumps from `04` to `07`. D5 and D6 are the only two docs in the set that do not
exist, and they are referenced by name in eight of the others:

| Doc | What it owned | Status per `13-handoff.md` |
|---|---|---|
| **D5 Visual Identity** | iconography, illustration, brand warmth, the whole look-and-feel layer | `13:81` "needs Figma; today we use Phosphor icons + gradients" |
| **D6 Signature Interaction** | the one moment a user screenshots and a competitor cannot copy | `13:83` "needs Pillar 1 on `main`" |

The epic shipped D1 to D4 and D7 to D13, which is architecture, plumbing and screen re-layout,
while skipping both documents whose job was to make the product **look and feel** different.
`13-handoff.md:99` predicted this outcome in writing: *"Promotion + D5/D6 are the remaining path
to a shipped redesign."*

### 1.3 The redesigned screens exist but were never promoted

`13-handoff.md:73-78` is candid that `Today`, `Goals`, `You` and `OnboardingFlow` were parked
behind preview routes pending validation. Since then only Today and Goals were promoted. The
other two are still preview-only:

- **`ui/you/YouScreen.kt`** already implements the grouped Identity / Insights / Decisions /
  Growth / Coach / Appearance / Settings model (`YouScreen.kt:104-149`) with a working theme
  toggle. It is reachable only through `ProfileScreen.kt:213-214`, a row literally labelled
  **"You (new design)"** under a section header **"Preview."**
- The live third tab is still `ui/profile/ProfileScreen.kt:171-227`, a flat scroll of about 15
  menu rows. This is precisely the "~12 unrelated items in one scroll, no grouping" that
  `02-information-architecture.md:§2` names as overwhelm cause number two.
- **`ui/onboarding/OnboardingFlowScreen.kt`** (198 lines) is likewise preview-only. The real
  first run is still `CoachOnboardingScreen.kt` (622 lines, untouched design) via `App.kt:353`.
  **Every new user's entire first impression of v3 is v2.**

A third of the app's surface, plus 100% of the first-run experience, is still the old design.

---

## 2. The safety problem: the kill switches do not work

This is the most important finding for "test v3 without breaking things," and it is worth
reading carefully.

`core/FeatureFlags.kt:23-35` declares five flags that read as per-pillar kill switches:

```kotlin
const val REDESIGN_HOME_FORYOU = false
const val PILLAR_WIRING        = false
const val PILLAR_CAUSAL        = false
const val PILLAR_BECOMING      = false
const val PILLAR_POSSIBILITY   = false
```

All five are `false`, and the file header (`FeatureFlags.kt:5-6`) documents the contract as *"Set
a flag to `false` to hide it completely."*

**None of these five flags is read anywhere in the codebase.** A repo-wide grep for
`FeatureFlags.` returns only 11 consumers, and every one of them is `ABILITIES_ENABLED`,
`HINTS_ENABLED`, or `USE_LEGACY_HOME_TAB`. The five pillar flags gate nothing.

The consequence is the opposite of what the code implies:

- The pillar features are **live for every user right now**, despite every flag reading `false`.
  Verified reachable: Possibility Mode via `GoalDetailScreen.kt:169-175`; Your Wiring via
  `ProfileScreen.kt:225`; Causal Insights via `ProfileScreen.kt:222`; Becoming via
  `ProfileScreen.kt:223`; plus For You feed cards in `HomeFeedBuilder.kt:94,111,127,138`.
- If a pillar misbehaves in testing, **flipping its flag to `false` will do nothing.** The only
  recovery is a code change and a rebuild.

This is the exact failure shape described as "breaking things like in version 2": there is no
working rollback lever, so any problem becomes a hotfix instead of a toggle. Fixing this is
Phase 0 below and should land before any further v3 testing.

---

## 3. Gap table

| Doc | Verdict | Evidence |
|---|---|---|
| D1 Principles | N/A (reference doc) | - |
| **D2 Information Architecture** | **PARTIAL, 2 of 3 tabs** | Today and Goals promoted (`BottomNavItem.kt:33,53`); third tab still old Profile (`:69`). No gear icon. Paced reveal not started. Context "+" mapping inverted (see 4.2). |
| **D3 Design Tokens** | **IMPLEMENTED AS SPEC'D** | And that is the problem, see 1.1. Adoption is also thin: 44 files use `modernColors` vs 164 still on `colorScheme`, 93 files carry 738 raw `Color(0x...)` hex literals, 13 `AppButton` call sites vs 221 ad-hoc buttons. |
| **D4 Component Library** | **PARTIAL, 2 of 11 primitives** | Built: `AppButton`, plus `DataViz` and `StateView`. Missing: `AppTextField`, `AppChip`, `AppBottomSheet`, `AppListRow`, `AppProgress`, `AppSnackbar`. `StateView` has 4 call sites against D12's "every screen ships all three states." |
| **D7 Today Screen** | **PARTIAL / SUPERSEDED** | `TodayScreen.kt` is spec-compliant and token-pure but is no longer the home tab. Commit `809dd1e` re-pointed Home to `ForYouScreen.kt`, a different concept (ranked filterable feed) that **has no spec doc**. |
| **D8 Data Viz** | **PARTIAL** | `DataViz.kt` exists, `ProgressRing` used on both heroes. `InsightCard` confidence line (the signature honest-uncertainty idea, `08:§1.2`) absent from home. No Sparkline. 9 of 15 consumers are unmigrated bespoke charts. |
| **D9 Emotional Design** | **NOT STARTED as UI** | Policy doc, no code column at `13:19`. `CelebrationOverlay` predates the epic. |
| **D10 Motion** | **PARTIAL, minimal** | `Motion.kt` exists; only 1 of 7 catalogued motions shipped (press-scale). Screen transitions still hand-rolled `tween(380)`/`tween(280)` in `App.kt:512-527`, violating `10:§2.3`. Reduce-motion not wired. |
| **D11 Onboarding** | **NOT PROMOTED** | See 1.3. New users see v2 onboarding. |
| **D12 A11y / States / Copy** | **NOT STARTED** | No WCAG audit, no reduce-motion, no semantics pass. |
| **D5 Visual Identity** | **DOES NOT EXIST** | The single biggest cause of "looks the same." |
| **D6 Signature Interaction** | **DOES NOT EXIST** | Blocked on Pillar 1 per `13:83`. |

---

## 4. Rollout plan

Ordered so that the safety net lands first, then the cheap visible wins, then the real visual
work. Each phase has an explicit verification gate. Do not start a phase until the previous
gate passes.

### Phase 0: make rollback possible (do this before any v3 testing)

Nothing else in this plan is safe to test until there is a working off switch.

1. Wire the five pillar flags to their actual entry points. Minimum viable set:
   - `PILLAR_POSSIBILITY` guards `GoalDetailScreen.kt:167-175` and the POSSIBILITY branch in
     `HomeFeedBuilder.kt:94`.
   - `PILLAR_WIRING` guards `ProfileScreen.kt:225`.
   - `PILLAR_CAUSAL` guards `ProfileScreen.kt:222` and `HomeFeedBuilder.kt:111`.
   - `PILLAR_BECOMING` guards `ProfileScreen.kt:223` and `HomeFeedBuilder.kt:127,138`.
2. Flip all five to `true` in the same commit, so behavior is unchanged but the levers are real.
3. Guard the nav registrations too (`App.kt:575-586`), so a disabled pillar cannot be reached by
   a stale deep link.

**Gate:** flip each flag to `false` one at a time, build, and confirm the entry point disappears
and the app does not crash on a deep link to that route.

**Risk:** low. Additive guards only.

### Phase 1: the free visible wins

These are small, reversible, and address "nothing changed" directly.

1. **Promote `YouScreen` to the third tab.** `BottomNavItem.kt:69`, change
   `Screen.Profile.route` to `Screen.YouRedesign.route`. One line. A third of the app's surface
   changes appearance. Keep the old Profile route registered so nothing else breaks.
2. **Fix the two hardcoded `val isDark = true`** at `ModernCards.kt:49` and
   `BottomNavigationBar.kt:72`. Both are flagged as D4 finding C1 and both are still present.
   `GlassCard` is used in 38 files and the nav bar is on every screen, so light mode currently
   cannot actually light-mode the app even though the toggle exists at `YouScreen.kt:146`.
   This is roughly a two-line fix that unlocks an entire second visual identity.
3. **Fix the context "+" mapping.** `App.kt:459-497` keys on `Screen.Home.route`,
   `Screen.Journal.route`, `Screen.Profile.route`. The live tab routes are `for_you`,
   `goals_redesign`, `profile`, so the button currently renders on You only and is absent from
   Today and Goals. `02:§7` mandates the exact opposite.

**Gate:** run the app, confirm the third tab is the grouped You screen, toggle light mode and
confirm cards and nav bar actually change, confirm "+" appears on Today and Goals.

**Risk:** low, and each item is independently revertible.

### Phase 2: D5 visual identity (the actual fix)

**Nothing before this point changes the palette, type or shape language.** Until D5 lands, every
other phase is rearranging identical looking boxes, which is exactly the complaint.

This needs a product decision before any code: whether v3 gets a new visual identity at all, or
whether D3's "keep the mature system" call stands. If it stands, then "the UI looks the same" is
not a bug and the plan should stop pretending otherwise.

If a new identity is wanted, D5 has to be written first (it needs Figma per `13:81`). Scope:
color, iconography, illustration, brand warmth, carrying P3 "warmth without childishness."

**Gate:** side by side screenshots of v2 and v3 on the same screen, where the difference is
obvious to someone who has never seen the app.

**Risk:** high blast radius by definition. Do it behind a theme-level flag so it can be reverted
wholesale.

### Phase 3: promotion and token migration

> **Revised 2026-07-18 after investigation. Both items in the original Phase 3 were wrong.**
> Recorded here rather than deleted, because the reasoning matters.

**3.1 Onboarding promotion is not a swap, and would regress the product.**

The original plan said to point `App.kt:353` at `OnboardingFlowScreen` instead of
`CoachOnboardingScreen`. That is unsafe. The two flows are not equivalent:

| | `CoachOnboardingScreen` (live) | `OnboardingFlowScreen` (D11) |
|---|---|---|
| Size | 622 lines, 15 phases (`OnboardingPhase`) | 198 lines, 3 steps |
| Collects | name, priority, wellbeing, 4 specialist questions, mind dump | a few `LifeValue`s |
| Produces | coach/persona selection, seeded first goals + habits | `LifeValue` rows |
| Owns | `COACH_ONBOARDING_KEY`, which gates the start destination | nothing |

Swapping would drop coach selection, name capture, and the initial goal/habit seeding, landing
every new user on an empty For You feed. D11 §4 says to replace the `welcome`/`onboarding`
routes; it never mentions `CoachOnboarding`, because the real first-run became the coach flow
after D11 was written. **The spec and the product diverged, and the spec is the stale side.**

Three options, needs a product decision:
- **(a) Chain them.** `OnboardingFlowScreen` (promise + values) first, then the coach flow. Keeps
  everything, gives new users the v3 first impression. Costs a longer first run, which cuts
  against D11 §1.3 "reach value fast."
- **(b) Merge.** Fold the D11 promise + values steps into `CoachOnboardingViewModel` as its first
  two phases. Best result, most work.
- **(c) Leave it.** New users keep seeing the v2 first-run.

**3.2 The token migration is craft, not perceptible change. Deprioritized.**

The original plan called for migrating 8 screens off `colorScheme` and raw hex, on the assumption
this was high visual impact. Measured and driven on device, it is not:

- `MaterialTheme.colorScheme` **is** correctly wired to the theme
  (`LifePlannerTheme.kt:79` `createColorScheme(darkTheme)`), so the 164 files using it already
  respond to Light/Dark. They are not broken, just not on the richer `modernColors` token set.
- The raw hex literals in the high-traffic screens are **semantic accents**, not hardcoded
  text-on-background pairs: green for "granted"/"available", amber for morning, indigo gradients.
  A green success dot is green in both themes.
- Verified on device in Light mode after the Phase 1 fix: Life Balance and Causal Insights render
  correctly, readable, no contrast failures.

So this work is a large diff with real regression risk and close to zero visible change. It is
consistency work worth doing eventually, but it does **not** answer "the UI looks the same" and
should not be mistaken for progress on that. Same reasoning applies to the six D4 primitives and
the 221 ad-hoc buttons.

**What is actually left that changes what the user sees: D5 (Phase 2), and the onboarding
decision above.** There is no third option that avoids both.

**Smaller genuine items found along the way:**
- `ui/causal/CausalInsightsScreen.kt` empty state is bare body text, no `StateView`, no icon or
  title (D12 wants all three states on every screen).
- The Home tab label says "Today" (`BottomNavItem.kt:34`) while the screen header says "For You"
  (`ForYouScreen.kt:87`).
- Legacy `ProfileScreen` is now reachable only from the legacy Home, which is itself off unless
  `USE_LEGACY_HOME_TAB` is flipped. Its "Preview" rows pointing at the You and Onboarding
  redesigns are now stale. Harmless, but it is dead surface to clean up.

### Phase 4: D6 signature interaction

The screenshot moment. Blocked on Pillar 1 reaching `main` per `13:83`. This is the difference
between "redesigned" and "memorable," but it is correctly last.

---

## 5. Open items not covered by this plan

- **Crystal Ball** (`AdherenceForecastEngine.kt`, `PreMortemPlan.kt`, schema v35, wizard forecast
  steps) is uncommitted in the working tree and not reachable from the app. It is unrelated to
  the v3 UI question and is deliberately untouched by this plan. It needs its own decision:
  finish and wire it, or shelve it.
- `_to_delete/` at repo root holds three zero-byte `.bak` files. Harmless, but it is untracked
  clutter.
- The Home tab label says "Today" (`BottomNavItem.kt:34`) while the screen's top bar says "For
  You" (`ForYouScreen.kt:87`). Minor, but it is a visible inconsistency.
- `ForYouScreen` is the shipped home surface and has no design doc. Either write one or
  reconcile it with D7.
