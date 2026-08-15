# D9 — Motivation & Emotional Design

> **TRI-56** · Design Overhaul (**TRI-47**). How the app motivates — the Duolingo/Finch craft bar,
> done **ethically**. Governed by **P3 (motivate without manipulation)**, reframed around
> value-alignment (Pillar 5) and adapted to how each person is wired (Pillar 7).

---

## 1. The stance

Borrow the *warmth and momentum* of the best consumer apps; refuse every coercive mechanic. We
motivate by **reflecting growth and affirming agency**, never by manufacturing anxiety. The feeling
to produce is *"I'm becoming someone I want to be,"* not *"I'll lose something if I stop."*

## 2. Mechanics, reframed

| Mechanic | ✅ Our version | ⛔ The dark-pattern version we reject |
|---|---|---|
| **Streaks** | Quiet encouragement; a miss is "life happens — pick it up when you're ready" (the Pillar-3 Choice Point, gently). | Streak-loss panic, "don't lose your 47-day streak!!", freeze-or-pay. |
| **Celebrations** | **Rare and earned** — fired on real, value-aligned milestones, not routine taps. Warm, brief (D10 `slow`). | Confetti on every tap; fake fanfare that cheapens real wins. |
| **Progress feedback** | Reflect **who you're becoming** — value-alignment over time (Pillar 5), not raw XP volume. | "Life Master, Level 50" that says nothing about the person. |
| **Nudges** | Gentle, skippable, *helpful*; timed to context (calendar/energy). | Guilt-trip notifications, escalating pressure, manufactured urgency. |
| **Rewards** | Tied to **value-alignment** ("3 of your last 5 actions served *Health*"). | Points for activity volume regardless of meaning. |
| **Comparison** | Only to your *past self*, opt-in. | Leaderboards / social pressure by default. |

## 3. Reframe rewards around value-alignment (Pillar 5)

XP/levels/badges stay (additive), but the **headline** the user feels is the **Becoming layer**:
identity statements + value-alignment, not the score. Celebrations and progress copy speak to
identity — "Three mornings of deep work this week — that's becoming a pattern" — per the D12 voice.

## 4. Adapt to the user's wiring (Pillar 7 — the Innate refinement)

The same event should land differently per `DecisionProfile`. No mechanic may make a valid
personality profile feel like the failure case.

| Dial (high) | Motivation flexes to… |
|---|---|
| **Punishment-sensitive** | fewer, softer prompts; misses framed kindly; never pile on. |
| **Reward-sensitive** | more visible wins/celebration (they respond to it) — but still honest. |
| **High delay-discounting (impatient)** | surface near-term payoffs and quick wins first. |
| **Low novelty** | celebrate consistency/routine, not "try something new." |
| **Risk-averse** | encourage via safe, incremental steps, not big stretch goals. |

This is *meeting people where they're wired* (D1 P5) applied to motivation specifically.

## 5. How it shows up

- **Celebration** — a brief, warm moment (the existing `CelebrationOverlay` component, used
  sparingly) on earned milestones; reduce-motion safe (D10/D12).
- **Becoming feedback** — value-alignment surfaced as `InsightCard`s (D8), with confidence (P2).
- **Choice Points** — the kind reframing of a miss (Pillar 3, already profile-aware, TRI-66).
- **Copy** — the D12 voice table is the law for all motivational strings.

## 6. The hard "no" list

Manufactured loss aversion · guilt-trip notifications · coercive competition / default leaderboards ·
variable-reward / slot-machine loops · manipulative streak pressure · shaming empty/missed states ·
any mechanic that punishes a personality trait. If a motivation idea needs one of these to work,
it's the wrong idea.

## 7. Handoff

- These rules govern any gamification/notification work and the **"Easier by default"** epic (TRI-71)
  — motivation should mostly *reflect* observed progress, not demand more activity.
- D11 onboarding sets the agency-first, non-pressuring tone from minute one.

*Next: **D6** (signature interaction), **D11** (onboarding), **D13** (handoff). **D5** (visual
visual-identity pass.*
