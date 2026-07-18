# D11 — Onboarding & first-run redesign

> **TRI-58** · Design Overhaul (**TRI-47**). The first minute. Establish the agency-first promise,
> collect *just enough*, reach a meaningful first action fast. Benchmarked on Duolingo/Finch warmth
> and momentum; governed by D1 **P1/P4**, D9 (non-pressuring), D12 (warm copy).

---

## 1. Principles for first-run

1. **Promise in minute one.** The very first screen says what we are: *you steer; the app is your
   instrument* — and what we're not (no judgment, no decisions made for you).
2. **Collect little, infer the rest.** Ask only for a few **values** (the Pillar 1 seed). Everything
   else the app learns from behavior (health, calendar, usage) — the "Easier by default" thesis
   (TRI-71). No interrogation, no long forms.
3. **Reach value fast.** End on **Today**, not a settings wall. Skippable throughout.
4. **Warm, never pressuring** (D9/D12): "Pick a few — you can change these anytime," not "You must
   choose 3 to continue."

## 2. The flow (shipped — `ui/onboarding/OnboardingFlowScreen.kt`)

Three light steps with a progress indicator, a persistent **Skip**, Crossfade transitions (D10),
and the premium `GradientHero`/`AppButton`:

1. **Promise** — gradient hero ("You're the one steering") + three plain-language bullets (see what
   you could do now · every goal has a why · gets more useful the more you live). CTA "Get started."
2. **Values** — "What matters most to you?" → a chip grid of the 7 canonical categories (G1
   `displayName` + category color), multi-select, no minimum-count gate beyond "pick at least one."
   CTA "Continue."
3. **Ready** — "That's all we need" hero + a recap of the chosen values. CTA "Go to Today" → lands
   on the redesigned Today.

Reachable now via **Profile → "Onboarding (new design)"** (additive preview route; the real
first-run is untouched until we promote it).

## 3. Integration seams

- **Values → Pillar 1.** Selected values are captured locally for now; when `LifeValue` (Pillar 1)
  reaches `main`, persist them as real `LifeValue` rows (and the one-time `PurposeSlice` migration,
  TRI-25, aligns with this).
- **Situation/health/calendar.** Deliberately *not* asked here — collected passively post-onboarding
  (the TRI-71 epic). First-run stays short on purpose.
- **Auth.** Sits alongside the existing sign-in (a guest can onboard and link later).

## 4. Handoff

- Promote into the real first-run (replace `welcome`/`onboarding`) once values persistence (Pillar 1)
  lands, so step 2 writes real `LifeValue`s.
- Copy follows the D12 voice; motion follows D10; visuals upgrade with the D5 identity pass.

*Remaining design sub-issues: **D6** (signature interaction — needs
Pillar 1 for the Why-Chain), **D13** (handoff specs — the capstone).*
