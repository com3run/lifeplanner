# D8 — Insight & Data-Visualization System

> **TRI-55** · Design Overhaul (**TRI-47**). How every number in the app is presented — the Oura/Whoop
> craft bar (D1 §3.3–3.4), governed by **P2: every number is a mirror, not a verdict.**

---

## 1. Principles

1. **Mirror, not verdict.** Viz helps the user understand themselves, never grades them. No bare
   "score out of 100" with red/green judgment.
2. **Honest about uncertainty.** Every insight can show its **confidence / sample size** ("Still
   learning · 6 signals", "High confidence"). Small data is labeled, not hidden — this is the same
   sample-size honesty the `CausalInsightEngine`/`TuningInferenceEngine` already carry.
3. **Explain the why.** A number is paired with its contributors / a plain-language reason (Oura's
   "here's why your readiness is X").
4. **Calm + legible.** One visual language, restrained color, animation only to show change (D10).

## 2. Components shipped (`ui/components/DataViz.kt`)

- **`ProgressRing(progress, …)`** — animated circular progress (Canvas; fills via `Motion.emphasized`).
  For completion/scores. **Now live on the Today hero** as a white habit-completion ring
  (`doneCount/total`) on the gradient.
- **`InsightCard(headline, detail?, confidenceLabel?, icon?, accent)`** — the signature insight
  unit: plain-language headline + optional *why* + an honest confidence line. This is the building
  block for Causal Insights (Pillar 4) and the insights-first home (TRI-73).
- **`StatTile(value, label, accent)`** — a compact headline stat (the "Today" mini-stat pattern).

All token-pure (D3), animate via D10's `Motion`.

## 3. The "Today headline pulse" decision (resolves the D2 §7 / open question)

- **Now:** Today's hero carries a **habit-completion `ProgressRing`** — a concrete, honest daily
  signal (no judgment, just "3/5 done").
- **Next (when Pillar 5 reaches `main`):** a **value-alignment / "becoming" pulse** rendered as an
  `InsightCard` *with confidence* — never a bare verdict score (P2). It appears only once it's honest
  to show (the D2 §5 paced-reveal rule).
- We deliberately **avoid** a single reductive "life score" that reads as a grade.

## 4. Usage guidance

| Need | Use | Notes |
|---|---|---|
| Completion / a 0–100 score | `ProgressRing` | center label optional; color by category/semantic |
| A finding from the user's data | `InsightCard` | always pass `confidenceLabel` if the sample is small |
| A headline number (XP, streak, count) | `StatTile` | pair 2–4 in a row, not a wall of numbers |
| A trend over time | *(Sparkline — to build when a screen needs it)* | keep axis-light, show direction not precision |

## 5. Handoff

- These primitives feed **TRI-73 (insights-first home)** — empty states become `InsightCard`s from
  health/usage/calendar — and the Pillar 4 **Causal Insights** screen.
- D9 decides which insights are celebrated; D12 ensures viz isn't color-only (a11y) and that every
  chart has a text equivalent.

*Next: **D9** (motivation & emotional design), **D12** (a11y, empty/error, copy), **D6** (signature
interaction), or the **D5** Figma visual-identity pass.*
