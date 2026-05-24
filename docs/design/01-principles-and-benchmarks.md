  # D1 — Design Principles & Competitive Benchmark Study

> **TRI-48** · anchor document for the Design Overhaul (**TRI-47**).
> Every other design sub-issue (D2–D13) should trace back to a principle here. If a screen,
> component, or interaction can't be justified by one of these principles, it's wrong.

---

## 0. How to use this doc

This is the **north star + rulebook** for rebuilding LifePlanner at a 10/10 craft bar. It does
three things:

1. States the **design vision** and **six principles** that govern every later decision.
2. Tears down four best-in-class apps — **Duolingo, Finch, Oura, Whoop** — for what to *borrow*
   and what to *deliberately reject*.
3. Names **where LifePlanner must be unique** — the moat that no competitor can copy without
   copying our philosophy.

It is deliberately opinionated. Downstream docs (IA, tokens, components, screens) cite these
principle numbers (P1–P6) as their justification.

---

## 1. North star

> **LifePlanner is an instrument for human agency — it helps you see your real options, choose
> deliberately, and become who you're trying to be. It never decides for you, and it never makes
> you feel like the failure case.**

We are not a productivity tracker, a habit nag, or a gamified to-do list. We are a *thinking
partner for a free agent*. The craft bar of Duolingo/Finch/Oura/Whoop is the floor; the
**Free Agents + Innate philosophy** (the seven pillars) is the ceiling and the differentiator.

The feeling we're designing for: **calm, capable, and seen** — the opposite of anxious,
nagged, and judged.

---

## 2. The six agency-first principles

### P1 — The user is the agent; the app is the instrument
Surface choices and their consequences; never silently decide or auto-optimize a life.
- **In practice:** "Right now you could…" offers options, never an order. Defaults are gentle
  suggestions, always overridable. The app proposes; the user disposes.
- **Rejects:** auto-scheduling that treats the user as a resource to be optimized; flows with
  no visible alternative.

### P2 — Every number is a mirror, not a verdict
Show data honestly — with its uncertainty — to help the user understand themselves, never to
rank or grade them.
- **In practice:** scores always show *why* (contributors) and *how sure* (sample size /
  confidence). Low numbers are framed as information, never as failure. "Describe, don't grade"
  (this is already the law for the Pillar 7 `DecisionProfile`).
- **Rejects:** a single daily verdict score with no explanation; red "you failed" states;
  precision the data doesn't support.

### P3 — Motivate without manipulation
Borrow the warmth and momentum of the best consumer apps; refuse every dark pattern.
- **In practice:** celebrate real progress; gentle, skippable re-engagement; rewards tied to
  *value-alignment*, not raw activity volume. A missed day is "life happens," never guilt.
- **Rejects:** manufactured loss aversion (streak anxiety weaponized), guilt-trip notifications,
  coercive competition, infinite-scroll/variable-reward traps.

### P4 — Always show the why
Nothing floats free. Every task, habit, and goal can be traced up to a value or reason the user
actually holds.
- **In practice:** the Why-Chain (`FocusSession → Milestone → Goal → LifeValue`) is one tap from
  anywhere; orphaned goals get a gentle "what's this for?" nudge, never a block.
- **Rejects:** bare task lists with no context; vanity metrics disconnected from meaning.

### P5 — Meet people where they're wired
There is no "average user." The experience adapts to the individual's `DecisionProfile`.
- **In practice:** an impatient (high-delay-discounting) user sees shorter tasks and nearer
  payoffs first; a punishment-sensitive user gets gentler, less frequent prompts. The same
  event produces different framing for different people.
- **Rejects:** one universal streak system, one XP curve, one tone for everyone; any mechanic
  that makes a valid personality profile feel like the failure case.

### P6 — Calm by default, alive on purpose
Reduce surface area; spend motion and delight only where they carry meaning. **The user should
never feel lost between features** — depth is available, not imposed.
- **In practice:** small, learnable structure (top-tier apps feel *small*); generous whitespace;
  animation reserved for transitions that aid understanding and for genuine celebratory moments.
  **Progressive disclosure is the default**: surface the few things that matter now, reveal the
  rest on request or as the user matures — and only reveal a data feature once it has something
  *honest* to say (this is where P6 meets P2). See D2 for the operational model.
- **Rejects:** dense dashboards, decorative motion everywhere, novelty for its own sake, dumping
  every feature on a new user at once.

---

## 3. Competitive benchmark teardowns

For each: **what they nail**, the **mechanic**, what to **borrow**, what to **avoid**.

### 3.1 Duolingo — the motivation & retention engine
**Nails:** turning a dry activity into a daily habit through relentless, well-crafted motivation.
- **Mechanics:** streaks + streak freeze, daily XP goals, bite-sized lessons with instant
  feedback, leagues/leaderboards, the Duo mascot's personality, escalating reminders,
  big celebratory moments on completion.
- **✅ Borrow (P3, P6):** bite-sized units of progress; immediate, positive feedback; a clear
  daily intent; genuinely delightful celebration moments; a character with warmth and personality.
- **⛔ Avoid (P1, P3, P5):** weaponized loss aversion (streak panic), guilt-trip notifications,
  league-style coercive competition, one-size pressure. These are the textbook dark patterns our
  P3 exists to reject. We take Duolingo's *craft*, not its *coercion*.

### 3.2 Finch — warmth & emotional safety
**Nails:** making self-improvement feel gentle, safe, and cared-for; the warmest app in the space.
- **Mechanics:** you nurture a pet bird by doing self-care; the pet grows *from your care*;
  consistently non-judgmental, soft tone; low-pressure reflection; "it's okay to rest" framing.
- **✅ Borrow (P3, P5):** warmth and character *without* infantilizing an adult; non-judgmental
  copy; gentle re-engagement after a lapse; the powerful loop where **your growth is reflected in
  something you care about** — which maps directly onto our **Becoming** layer (Pillar 5) and the
  emotional-safety needs of punishment-sensitive users (Pillar 7).
- **⛔ Avoid (P1):** tipping into cutesy/childish for what is fundamentally a serious agency tool;
  tying self-worth to a pet's wellbeing (a subtle guilt vector). Keep the warmth; drop the
  dependency.

### 3.3 Oura — insight & trustworthy data presentation
**Nails:** making personal data feel **calm, premium, and trustworthy** — you believe the numbers.
- **Mechanics:** one legible headline score (Readiness/Sleep), a **contributors breakdown** that
  explains *why* the score is what it is, restrained and beautiful data viz, no hype.
- **✅ Borrow (P2):** a single legible headline metric backed by **transparent contributors**;
  honest, calm visualization; an explainable "here's why"; a trustworthy, non-hype voice. This is
  the bar for our **Causal Insights** and **calibration** surfaces (Pillar 4).
- **⛔ Avoid (P2):** score reductionism that lands as a daily verdict; gating core insight behind a
  subscription in a way that feels extractive; letting a low score read as judgment.

### 3.4 Whoop — turning data into a decision
**Nails:** closing the loop from measurement → recommendation → behavior, over time.
- **Mechanics:** strain vs. recovery model, a daily recommendation derived from the data, strong
  trend-over-time views, weekly/monthly performance assessments, a "your body is telling you…"
  narrative.
- **✅ Borrow (P1, P2):** data that drives a **concrete, optional recommendation** (maps onto
  Pillar 2 Possibilities + Pillar 4 Causal Model); trends over snapshots; calibration and honesty
  about what is and isn't measured.
- **⛔ Avoid (P1, P3):** the optimization-treadmill pressure (always push to do *more*); opaque
  proprietary scores (trust requires explainability — see P2); the framing that rest must be
  "earned." We recommend; we never command.

---

## 4. Borrow / Avoid — consolidated

| Borrow (the craft) | Avoid (the coercion) |
|---|---|
| Bite-sized progress + instant positive feedback (Duo) | Streak anxiety / weaponized loss aversion (Duo) |
| Delightful, meaningful celebration moments (Duo) | Guilt-trip / escalating notifications (Duo) |
| Warmth, character, non-judgmental tone (Finch) | Coercive competition & leagues (Duo) |
| Growth reflected in something you care about (Finch) | Infantilizing cuteness for an adult tool (Finch) |
| Gentle re-engagement after a lapse (Finch) | Self-worth tied to an app-pet's wellbeing (Finch) |
| One legible headline metric + contributors (Oura) | Score-as-daily-verdict / judgment (Oura) |
| Honest, calm, premium data viz (Oura) | Extractive paywalling of core insight (Oura) |
| Data → concrete, optional recommendation (Whoop) | Optimization-treadmill "do more" pressure (Whoop) |
| Trends over snapshots; calibration honesty (Whoop) | Opaque proprietary scores (Whoop) |

---

## 5. Where LifePlanner must be unique (the moat)

Craft can be copied — anyone can ship a streak or a nice chart. **Our moat is the Free Agents +
Innate philosophy made tangible.** These are the things a competitor cannot clone without
adopting our entire worldview:

- **The agency surface** — "Right now you could…" reads a situation as a field of *possibilities*,
  not a backlog of obligations. *(Pillar 2)*
- **The Why-Chain** — every action visibly laddered to a value the user holds. *(Pillar 1)*
- **Decisions as first-class objects** — the "good decision vs. good luck" distinction; you grade
  your *reasoning*, not just outcomes. *(Pillar 3)*
- **Your own causal model** — the app helps you learn what actually drives *your* progress, with
  calibration. *(Pillar 4)*
- **Becoming over scoring** — identity and value-alignment as the retention engine, sitting above
  XP/levels rather than replacing them. *(Pillar 5)*
- **Your Wiring** — the app tunes itself to *you* and tells you, honestly and without judgment, how
  you're wired. *(Pillar 7)*

**Design implication:** the **signature interaction** (D6) must be drawn from this list — most
likely the Why-Chain reveal or the "Right now you could…" surface — because that's the moment a
user screenshots and that a competitor literally cannot copy.

---

## 6. Implications for the rest of the epic

How each principle should show up downstream:

| Sub-issue | Carries principles |
|---|---|
| **D2** IA & navigation | P6 (calm, small surface), P1 (choice-first home) |
| **D3** Tokens / theme | P6 (calm palette, restraint), P2 (semantic colors for honest data states) |
| **D4** Component library | P2 (every state incl. empty/error designed), P3 (no dark-pattern components) |
| **D5** Visual identity | P3 (warmth without childishness — the Finch lesson, dialed for adults) |
| **D6** Signature interaction | §5 moat — Why-Chain reveal or "Right now you could…" |
| **D7** Core screens | all six; Home is the proving ground for P1 |
| **D8** Insight & data-viz | P2 (mirror not verdict — the Oura/Whoop bar) |
| **D9** Motivation & emotional design | P3 (motivate without manipulation), P5 (per-wiring), Pillar 5 |
| **D10** Motion | P6 (alive on purpose, not everywhere) |
| **D11** Onboarding | P4 (establish the *why* in minute one), P1 (agency-first promise) |
| **D12** A11y, empty/error, copy | P2, P3 (warm, agency-affirming voice) |
| **D13** Handoff | translates P1–P6 into Compose specs |

---

## 7. Open questions (to resolve as the epic proceeds)

- **Mascot/character?** Finch's warmth is largely carried by its pet. Do we want a character, or
  do we carry warmth through copy, color, and motion alone? (Risk: a mascot can tip into childish
  and undercut the "serious agency tool" positioning — see D5.)
- **Headline metric?** Oura/Whoop each have one. Should LifePlanner have a single daily headline
  (e.g., a value-alignment or "becoming" pulse), or deliberately avoid one to dodge the
  verdict trap (P2)? Decide in D8.
- **How visible is the DecisionProfile (P5) in the everyday UI** vs. living mostly in "Your Wiring"?
  Decide alongside D7.

---

*Anchor established. Next: **D2 — Information Architecture & navigation redesign**, which turns
P1 and P6 into a concrete screen map and route structure.*
