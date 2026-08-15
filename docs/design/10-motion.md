# D10 — Motion & micro-interactions

> **TRI-57** · Design Overhaul (**TRI-47**). The motion language that makes the app feel fast, alive,
> and intentional — the difference between a 7/10 and a 10/10. Governed by D1 **P6 (calm by default,
> alive on purpose)**: motion is reserved for feedback and for transitions that aid understanding,
> never decoration.

---

## 1. Motion tokens (`ui/theme/Motion.kt`)

One shared feel, tokenized so nothing hand-rolls durations:

| Token | Value | Use |
|---|---|---|
| `Duration.fast` | 120 ms | micro-feedback (press, toggle) |
| `Duration.medium` | 240 ms | transitions, content moves, crossfades |
| `Duration.slow` | 400 ms | hero entrances, celebrations |
| `standard` easing | `FastOutSlowInEasing` | most transitions (decelerate) |
| `emphasized` easing | `cubic(0.2, 0, 0, 1)` | entrances / important moves (confident settle) |
| `pressScale` | 0.97 | how far a pressed surface scales |

## 2. Principles

1. **Feedback first.** Every tappable surface acknowledges touch within `fast`.
2. **Meaningful, not decorative.** Animate to show *where something came from / went to* or to
   confirm an action — not for spectacle (P6).
3. **One feel.** All motion uses the tokens above; no ad-hoc `tween(317)`.
4. **Fast in, gentle out.** Entrances use `emphasized`; exits are quick and quiet.
5. **Respect reduced-motion.** Honor the OS "reduce motion" setting — fall back to crossfades/instant
   (wired in D12 alongside the a11y pass).

## 3. Primitives shipped

- **`Modifier.bouncyClickable { }`** — the tactile card/row press: the surface scales to `pressScale`
  while pressed (no ripple), giving a physical, modern feel. **Applied** to the Today habit rows,
  Goals cards, and You rows this cycle — tap any and it presses in.

## 4. Catalog — intended motions (spec for the rest of D7/D11)

| Moment | Motion | Token |
|---|---|---|
| Card / row press | scale to 0.97 (shipped: `bouncyClickable`) | fast |
| Screen → detail | shared-axis / slide-up (the app's NavHost already slides) | medium |
| List first paint | staggered fade + 8dp rise | medium, `emphasized` |
| Theme switch (light↔dark) | crossfade the surface, don't hard-cut | medium |
| Habit check / goal complete | check draws in + a brief, *earned* celebration (ties to D9, P3) | slow |
| Progress bar fill | animate width from old→new value | medium |
| Possibility appears | gentle fade/rise as it enters "Right now you could…" | medium |

Celebrations are deliberately rare and tied to real wins (P3 — motivate without manipulation); they
must never fire on routine taps.

## 5. Accessibility

Motion is an enhancement, never required to understand state. With OS reduce-motion on, scale/slide
collapse to instant or a soft crossfade; nothing depends on animation to be usable (full pass: D12).

## 6. Handoff

- `Motion` tokens + `bouncyClickable` are ready for every screen; D7 detail screens and D11 onboarding
  draw transitions from §4.
- D9 owns the celebration moments (which wins, how loud); D12 wires reduce-motion.

*Next: **D8** (data-viz / the Today headline pulse), **D9** (motivation & emotional design), **D12**
(a11y, empty/error, copy). **D5** (visual identity) shipped: `ui/theme/VisualIdentity.kt`.*
