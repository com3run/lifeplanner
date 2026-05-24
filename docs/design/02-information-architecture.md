# D2 — Information Architecture & Navigation Redesign

> **TRI-49** · Design Overhaul (**TRI-47**). Operationalizes D1's **P6 (calm by default, small
> surface)** and **P1 (agent, not instrument)** into a concrete nav model, screen map, and route map.
>
> **Mandate (from the product owner):** the app must feel *small and unintimidating* — a user should
> **never feel lost or overwhelmed between features**. Depth is earned and offered, never dumped.
> This doc treats anti-overwhelm as the primary constraint, not an afterthought.

---

## 1. What the best calm apps actually do (research)

The instinct to *reduce*, not add, is backed by both platform guidance and the apps we benchmark
against (D1 §3):

- **Material Design — 3–5 bottom-nav destinations, and odd counts (3 or 5) read as calmer.** Past 5,
  tap targets crowd and the long tail belongs elsewhere (a profile/menu), not in the bar.[^material]
- **Oura just went the *other* way on tab count — from 5 tabs to 3** (Today / Vitals / My Health) in
  its redesign, explicitly to cut overwhelm. Its **Today tab is built like a "Top Stories" page** —
  the most timely, relevant thing first — and the **scores sit at the top of Today**, not in their
  own tabs. Depth lives one tap down.[^oura]
- **Whoop** keeps its **Home** as the daily anchor (the three dials live there) and uses a single
  **"+" action button** for quick verbs (start workout, journal).[^whoop]
- **Duolingo** keeps **Settings behind the profile icon** — never a tab, never a flat list in your
  face.[^duo]
- **Nielsen Norman Group — progressive disclosure**: show the few most-important options first;
  defer advanced/rare features to a second layer revealed on request. It measurably improves
  *learnability, efficiency, and error rate*, and "people understand a system better when you help
  them prioritize."[^nng]

**Four rules we adopt from this:**
1. **3 tabs.** Odd, calm, Material-endorsed, and the direction the gold-standard data app moved *to*.
2. **The daily front door is a "top stories" surface** — most relevant thing first, not a dashboard.
3. **Settings and the long tail live behind the profile**, not in the nav bar.
4. **Progressive disclosure is the law** — and we add a twist below: *reveal a feature when its data
   is ready to be honest* (P6 × P2).

---

## 2. Current state & why it overwhelms

**Today: 3 tabs** — `[Home "Life", Hub "My Units", Profile "You"]` — but the middle tab is a **Hub**
that swaps Journal / Goals / Habits / Abilities behind a hidden `hubSelectedTab` index, and
everything else (focus, life balance, retrospective, causal insights, health, screen-time,
reminders, coach, decisions, becoming, wiring, …) is scattered across Home, the Hub, and a long
Profile menu.

The tab *count* is already fine. The **overwhelm comes from three things**, all fixable:
1. **Buried core objects** — Goals and Habits, the heart of a planner, sit two taps deep behind the
   jargon label "My Units." A new user can't find them. *(violates P6 learnability)*
2. **A junk-drawer Profile** — ~12 unrelated items in one scroll, no grouping. *(P6)*
3. **Everything visible at once, from day one** — a brand-new user sees dependency graphs, abilities,
   causal insights, and decision journals before they have a single goal. *(the overwhelm mandate)*

So the redesign keeps **3 tabs** but (a) gives every object an obvious home, (b) replaces the
junk-drawer with a structured profile + gear-settings, and (c) **paces feature reveal over time**.

---

## 3. The model: **3 tabs + profile-gear settings + one context "+" + progressive disclosure**

```
┌─────────────────────────────────────────────────────────┐
│                      (content)                            │
│                                                           │
│                                        ┌──────────┐       │
│                                        │   "+"    │  ◀ context-aware quick-capture
│                                        └──────────┘       │
├───────────────┬───────────────┬───────────────┬──────────┤
│    Today      │     Goals     │      You       │           ◀ 3 bottom tabs
│   (agency)    │ (commitments) │ (self + setup) │
└───────────────┴───────────────┴───────────────┴──────────┘
```

| Tab | Question it answers | JTBD frequency | Principle |
|---|---|---|---|
| **Today** | "What can I do right now?" | every session | P1 agency, P2 (a key signal up top) |
| **Goals** | "What am I working toward — and why?" | frequent | P4 show-the-why |
| **You** | "Who am I becoming, and where's my stuff?" | reflective / occasional | P5, Pillars 5/7, P6 |

Why not a 4th "Insights" tab (my v1 proposal)? Because **Insights isn't a daily action** — and Oura
proved you can surface the headline signal *on Today* and keep the depth one tap down. A permanent
Insights tab would spend scarce thumb-space on something used weekly, and add a destination the
overwhelm mandate tells us to avoid. So **the key signal rides on Today; the full Insights surface
lives inside You.**

---

## 4. Each tab — surface vs. revealed (progressive disclosure within the screen)

Every tab opens to a **calm summary with one clear focus**; depth is a tap away, never on the
surface.

### Today — the agency surface ("top stories" for your life)
**Surface (always):**
- **"Right now you could…"** — 1–3 ranked possibilities (Pillar 2), each with its fit reason.
- **Today's habits** — inline check-in for *today only* (the daily verb; full manager is in Goals).
- **One headline signal** — a single "becoming / alignment" pulse or today's balance glance
  (Oura-style, top of Today) — *only once it's honest to show* (see §5).
- **Pending Choice Point**, if any (Pillar 3) — surfaced as a gentle card, framed per the user's
  wiring (Pillar 7).

**Revealed on tap:** a possibility → its goal/focus; the headline → full Insights; a habit → detail.

### Goals — commitments laddered to values
**Surface:** the goals list (active first), each showing progress + its value tag; one primary
"new goal" action.
**Revealed on tap:** goal detail → milestones, the **Why-Chain** (Pillar 1), linked habits/focus;
**Habits manager** (full CRUD) as a section; *Dependency graph* and *Templates* as advanced entries
(see §5 — not shown to brand-new users).

### You — self + setup (organized, not a drawer)
Grouped sections, each a calm card that drills in — **never a flat 12-item list**:
- **Identity** — Becoming (Pillar 5), Your Wiring (Pillar 7).
- **Insights / Patterns** — Life Balance, Causal Insights + calibration (Pillar 4), Retrospective,
  Health, Screen-time. *(the consolidated "mirror" home)*
- **Decisions** — Decision Journal + Review Decisions (Pillar 3).
- **Coach** — chat + personas.
- **Growth** — Abilities & Achievements.
- **⚙︎ Settings (gear, top-right)** — Reminders, Backup, Account, Notifications, Feedback, About.
  *(the Duolingo pattern: the whole config long-tail collapses behind one icon.)*

---

## 5. The anti-overwhelm engine: **pace the reveal; show a feature when it's ready to be honest**

This is the crucial part — *how we guide the user*. Features don't all exist on day one. They
**arrive as the user grows and as their data becomes truthful.** This unifies P6 (calm) with P2
(a number is a mirror, not a verdict): a feature that has nothing honest to say yet is **hidden, not
shown empty or guessing.**

| Stage | Trigger | What's visible | What's still hidden |
|---|---|---|---|
| **First run** | onboarding done | Today (1 possibility), create first Goal, add 1 habit, the "why" of one value (P4) | everything advanced |
| **Getting going** | ≥1 goal + a few check-ins (~days) | Why-Chain, Habits manager, Life Balance starts filling, Becoming begins | causal insights, wiring, decisions, abilities, dependency graph |
| **Enough signal** | data crosses honesty thresholds | **Your Wiring** appears once the `DecisionProfile` is reliable (**≥10 behavioural signals** — the `TuningInferenceEngine` says "still learning" before that); **Causal Insights** appears at **≥7 days** of data (the `CausalInsightEngine`'s `minSampleSize`); Retrospective after the first full period | power-user tools |
| **Power user** | sustained, deliberate use | Dependency graph, Abilities, Coach groups, advanced export/backup | — |

Mechanics:
- **Reveal, don't gate.** Hidden ≠ locked-behind-paywall. Things *appear* when relevant; a user can
  always opt into "show everything" in Settings. (We never punish curiosity — P1.)
- **Data-honesty reveal is the signature move.** "Your Wiring" and "Causal Insights" literally
  surface the moment the underlying engine has enough samples to be truthful — so the user's first
  encounter with a feature is *already trustworthy*, never an empty or hand-wavy state. This is
  uniquely ours: the engines (Pillars 4 & 7) already carry sample-size/confidence, so the UI can pace
  itself off real readiness.
- **Onboarding establishes the *why* in minute one** (P4) and drops the user on **Today** with exactly
  one obvious next action — not a tour of 13 features. (Hands off to **D11**.)
- **Contextual hints over upfront tutorials** (NN/g): introduce a feature where/when it becomes
  relevant, not in a front-loaded carousel.

---

## 6. Settings & the long tail

Everything configuration/utility collapses behind the **⚙︎ gear in You's top bar** (Duolingo
pattern), grouped: **Account · Notifications & Reminders · Data & Backup · Appearance · Help &
Feedback · About**. None of these is ever a tab or a top-level Profile row. This single move empties
most of today's Profile junk-drawer.

---

## 7. Quick-capture "+" — one action, context-aware (not a menu)

A persistent "+" whose **default action matches the current tab**, so it's a single confident tap,
never a chooser that re-introduces decision load:
- **Today →** capture (journal / quick note)
- **Goals →** new goal
- **You →** (hidden; no capture verb here)
- Long-press (optional, power users) → the small set of other verbs (start focus, log a decision).

Verbs (journal, focus, log-decision) are **actions**, not destinations — they never deserve a tab.
(Whoop's "+" validates this.)

---

## 8. Route map (tab ownership)

Route strings mostly survive; the change is *ownership* + killing the Hub indirection. (● primary
home, ◐ also surfaced, ▷ revealed progressively per §5.)

| Routes | Today | Goals | You | Notes |
|---|:--:|:--:|:--:|---|
| `home` | ● | | | becomes **Today** (agency "top stories") |
| `goals`, `goal_detail/{id}`, `goal_wizard`, `ai_goal_generation`, `add_goal_from_template/{id}` | | ● | | promoted out of the Hub |
| `templates`, `dependency_graph`, `dependency_graph/{goalId}` | | ▷ | | advanced — revealed for power users |
| `habit_tracker`, `add_habit`, `smart_habit_generator` | ◐ | ● | | daily check-in on Today; manager in Goals |
| `journal`, `journal_wizard`, `journal_entry_detail/{id}` | ◐ | | | journal = quick-capture, not a Hub tab |
| `focus_setup` | ● | | | launched from Today / a goal |
| `life_balance`, `retrospective`, `health`, `screen_time_insight`, `analytics` | | | ● | **Insights** section in You |
| `causal_insights` | | | ▷ | You → Insights; revealed at ≥7 days data |
| `your_wiring` | | | ▷ | You → Identity; revealed at ≥10 signals |
| `becoming` | ◐ | | ● | You → Identity; a pulse may surface on Today |
| `decision_journal`, `decision_detail/{id}`, `decision_review` | | | ● | You → Decisions |
| `abilities`, `ability_detail/{id}`, `create_ability`, `achievements` | | | ▷ | You → Growth (power user) |
| `ai_chat`, `ai_chat/{id}`, coach CRUD, `coach_profile/{id}` | | | ● | You → Coach (optional Today affordance) |
| `reminders`, `backup_settings`, `feedback`, `story_reader` | | | ● | behind **⚙︎ Settings** |
| `search` | ◐ | ◐ | ◐ | global, top-bar affordance, not a tab |

> **`terminology.md` follow-up:** the authoritative route list (`../lifeplanner-assets/docs/
> terminology.md`, a separate non-git repo) gets updated to this map when routes are implemented in
> **D7** — a checklist item there, not edited blind from here.

---

## 9. Navigation-model decisions

- **Bottom tab bar** on phones (thumb-reachable, always visible); the existing `NavigationRail` stays
  for tablet/landscape. 3 destinations, labels + icons, ~56–80dp, subtle elevation (Material).
- **Tabs preserve independent back-stacks**; re-tapping the active tab pops to its root.
- **Bottom sheets** for lightweight, in-the-moment things (quick-capture, Choice Points) so they feel
  like "a quick deliberate re-choice, not a chore" (Pillar 3). Full screens for detail/editing.
- **Deep links** (Quick-Settings tiles, notifications, widgets) resolve to the owning tab + push the
  detail (fixes the dangling deep-link work, ref TRI-10).
- **One headline, never a verdict** (P2): if Today shows a pulse, it's framed as information with a
  "why," never a grade — and it's absent until honest (§5).

---

## 10. Migration map (today → target)

| Today | Target |
|---|---|
| "Life" tab | **Today** — re-centered on agency, "top stories" layout |
| "My Units" Hub (Journal/Goals/Habits/Abilities sub-tabs) | **dissolved** → Goals (top-level) + Today check-ins + quick-capture + You |
| Goals/Habits two taps deep | **top-level** in Goals; daily habits on Today |
| Profile junk-drawer (~12 flat items) | **You**, grouped: Identity / Insights / Decisions / Coach / Growth + **⚙︎ Settings** |
| Insights scattered across 6 entry points | **You → Insights** (one home) + a pulse on Today |
| Every feature visible from day one | **paced reveal** by stage + data-honesty (§5) |

**Net:** still 3 tabs, but self-describing; nothing buried; the long tail collapses behind a gear;
and a new user meets the app a few features at a time. Calmer *and* more capable.

---

## 11. Open questions

- **The Today pulse:** do we ship a single headline signal (value-alignment / becoming) at the top of
  Today, or keep Today purely action-focused and leave all numbers to Insights? Leaning *yes, one
  pulse, once honest* — finalize in **D8**.
- **Coach prominence:** thinking-partner positioning argues for a Today affordance; calm argues for
  keeping it in You. Decide with the Today layout in **D7**.
- **"Show everything" escape hatch:** confirm the Settings toggle that opts power users out of paced
  reveal (some users hate being paced). Recommend including it.

---

## 12. Handoff

- **D3 (tokens)** — unblocked, independent; build the Compose theme next (reconcile with the existing
  `tokens.json` / `figma-variables-*.json`).
- **D7 (core screens)** — Today, Goals, You are the three canvases; build the surface/revealed tiers
  from §4.
- **D9/D11 (motivation, onboarding)** — own the §5 reveal schedule and the minute-one "why."
- **D8 (data-viz)** — decides the Today pulse.

---

[^material]: Material Design — Bottom navigation guidelines (3–5 destinations; odd counts preferred). https://m3.material.io/components/navigation-bar/guidelines · https://m2.material.io/components/bottom-navigation
[^oura]: Oura — "Introducing the New Oura App Design" (5 tabs → Today / Vitals / My Health; Today as a "Top Stories" page; scores at the top of Today). https://ouraring.com/blog/new-oura-app-experience/
[^whoop]: WHOOP — App Navigation Bar / The All-New Home Screen (Home holds the dials; "+" quick action). https://support.whoop.com/hc/en-us/articles/360056034814-WHOOP-App-Navigation-Bar
[^duo]: Duolingo — Settings accessed via the profile icon. https://pageflows.com/post/android/settings/duolingo/
[^nng]: Nielsen Norman Group — "Progressive Disclosure" (defer advanced features; improves learnability, efficiency, error rate). https://www.nngroup.com/articles/progressive-disclosure/
