# D2 — Information Architecture & Navigation Redesign

> **TRI-49** · Design Overhaul (**TRI-47**). Turns D1's principles — especially **P1 (agent, not
> instrument)** and **P6 (calm by default, small surface)** — into a concrete screen map and route
> structure. Cite D1 principle numbers when justifying a choice.

---

## 0. Goal of this doc

Collapse the app's ~30+ destinations into a **small, learnable structure** a new user can hold in
their head, with an **agency-first front door**. Top-tier apps feel *small*; ours currently feels
like a drawer of features. This doc defines the target nav model, the screen map, and the route
map that D7 (screen redesigns) will build against.

---

## 1. Current-state inventory

### 1.1 Bottom navigation — 3 tabs
`BottomNavItem.items = [Home, Hub, Profile]`:

| Tab label | Route | Icon | Reality |
|---|---|---|---|
| **Life** | `home` | Flower | The home/dashboard. |
| **My Units** | `journal` | SquaresFour | A **Hub** that swaps content by an internal `hubSelectedTab` index. |
| **You** | `profile` | User | Profile + a long menu of everything else. |

### 1.2 The "My Units" Hub hides four core objects behind sub-tabs
`hubSelectedTab` (0–3) swaps the Hub between:

| Sub-tab | Content | Quick action |
|---|---|---|
| 0 | **Journal** | Write entry |
| 1 | **Goals** | Add Goal (`goal_wizard`) |
| 2 | **Habits** | New Habit (`smart_habit_generator`) |
| 3 | **Abilities** *(feature-flagged)* | Add Ability |

So **Goals and Habits — the two most important objects in a planner — have no top-level home.**
They live two interactions deep, behind a jargon label ("My Units", "Abilities") that means
nothing to a new user.

### 1.3 Full destination inventory (~40 routes)
Grouped by feature area (baseline + the Free Agents pillar UIs shipped this cycle):

| Area | Routes |
|---|---|
| Onboarding / auth | `welcome`, `onboarding`, `coach_onboarding`, `coach_intro/{id}` |
| Home / agency | `home` (+ Pillar 2 "Right now you could…" card) |
| Goals | `goals`, `goal_detail/{id}`, `edit_goal/{id}`, `goal_wizard`, `ai_goal_generation`, `templates`, `add_goal_from_template/{id}`, `dependency_graph`, `dependency_graph/{goalId}` |
| Habits | `habit_tracker`, `add_habit`, `smart_habit_generator` |
| Journal | `journal`, `journal_wizard`, `journal_entry_detail/{id}` |
| Focus | `focus_setup` |
| Decisions *(Pillar 3)* | `decision_journal`, `decision_detail/{id}`, `decision_review`, ChoicePoint sheet |
| Insight / data | `analytics`, `life_balance`, `screen_time_insight`, `health`, `retrospective`, `causal_insights` *(Pillar 4)* |
| Identity *(Pillars 5/7)* | `becoming`, `your_wiring` |
| Gamification | `achievements`, `abilities`, `ability_detail/{id}`, `create_ability` |
| Coach / chat | `ai_chat`, `ai_chat/{sessionId}`, `create_coach`, `edit_coach/{id}`, `create_group`, `edit_group/{id}`, `coach_profile/{id}` |
| System | `profile`, `reminders`, `backup_settings`, `feedback`, `search`, `story_reader` |

**~13 feature areas, ~40 routes, 3 tabs** — the surface is large and the entry points are
inconsistent (some via Hub sub-tab, some via Home, most via the Profile menu junk-drawer).

---

## 2. What's wrong today (measured against D1)

1. **No agency-first front door (violates P1).** "Life"/Home shows state, not "what can I do right
   now?". The Pillar 2 possibilities card was bolted on but the home isn't *organized* around choice.
2. **Core objects are buried (violates P6 "learnable").** Goals and Habits sit behind the "My Units"
   Hub's hidden sub-tabs. A first-time user cannot find Goals.
3. **Jargon labels.** "My Units" and "Abilities" are internal terminology, not user language.
4. **Insight is scattered (violates P2).** The "mirror" — Life Balance, Causal Insights, calibration,
   Retrospective, Health, Screen-time, Analytics — is spread across six unrelated entry points with
   no unified home. The Oura/Whoop benchmark (D1 §3.3–3.4) demands one trustworthy data home.
5. **Profile is a junk drawer.** Identity (Becoming, Your Wiring), system settings, coach, decisions,
   and gamification all dumped into one scrolling menu.
6. **The pillar features have nowhere coherent to live**, so they accreted as Profile menu items.

---

## 3. The redesigned IA

### 3.1 Nav model: **4 tabs + a context-aware quick-capture action**
Four is the sweet spot (≤5; top-tier apps rarely exceed it). The real *reduction* vs. today isn't
the tab count — it's **killing the opaque "My Units" Hub and the Profile junk-drawer**, and giving
every feature one obvious home. Each tab answers one question the user actually has:

| Tab | The question it answers | Principle / benchmark |
|---|---|---|
| **Today** | "What can I do right now?" | P1 — the agency surface |
| **Goals** | "What am I working toward, and why?" | P4 — show the why |
| **Insights** | "What's actually true about me?" | P2 — mirror, not verdict (Oura/Whoop) |
| **You** | "Who am I becoming, and my setup" | P5 + Pillar 5/7 |

A persistent **quick-capture "+"** (context-aware, evolving today's per-route action) handles the
*cross-cutting verbs* that don't deserve a tab: **write a journal entry, start a focus session, log
a decision, add a goal/habit.** Capture is a verb; the tabs are nouns.

### 3.2 What lives in each tab

```
TODAY  (home / agency surface)            ← replaces "Life"
├─ "Right now you could…" possibilities   (Pillar 2)
├─ Today's habits (inline check-in)       (habits live where they're used daily)
├─ Active / suggested focus session       (Pillar — focus)
├─ Pending Choice Points                  (Pillar 3)
└─ Day intent + quick-capture "+"

GOALS  (commitments, laddered to values)  ← promoted out of the Hub
├─ Goals list  →  Goal detail
│   ├─ Milestones
│   └─ Why-Chain: Focus → Milestone → Goal → Value   (Pillar 1)
├─ New goal: wizard / from template / AI-generate
├─ Dependency graph
└─ Habits manager (full)                  (daily check-in is on Today)

INSIGHTS  (the mirror — one honest data home)   ← NEW consolidation
├─ Life Balance (the wheel)
├─ Causal Insights + calibration          (Pillar 4)
├─ Retrospective / periodic Reviews
├─ Health
├─ Screen-time patterns
└─ Analytics

YOU  (identity + system)                  ← de-junk-drawered
├─ Becoming — values & identity           (Pillar 5)
├─ Your Wiring — DecisionProfile          (Pillar 7)
├─ Decision Journal + Review Decisions    (Pillar 3)
├─ Abilities & Achievements               (gamification)
├─ Coach / Chat
└─ Settings: reminders, backup, feedback, account
```

### 3.3 Cross-cutting & deep destinations
- **Quick-capture "+"** → journal entry, focus, log decision, add goal/habit (sheet, context-aware).
- **Search** → global, from a persistent affordance (top bar), not a tab.
- **Coach/Chat** → lives under *You*, but also reachable as a floating affordance from *Today*
  (it's the "thinking partner"; decide prominence in D7).
- **Onboarding / Welcome / Coach onboarding** → pre-auth flow, outside the tab shell.
- **Story reader** → content, launched contextually.

---

## 4. New route map

Routes are string-keyed (existing convention). Most route *strings* survive — the change is which
**tab owns** them and the death of the Hub sub-tab indirection. Flagged changes only:

| Route | Today | Goals | Insights | You | Change |
|---|:--:|:--:|:--:|:--:|---|
| `home` | ● | | | | becomes **Today**; reorganized around possibilities (P1) |
| `goals`, `goal_detail/{id}`, `goal_wizard`, `templates`, `ai_goal_generation`, `dependency_graph` | | ● | | | **promoted** out of Hub to a top-level tab |
| `habit_tracker`, `smart_habit_generator`, `add_habit` | ◐ | ● | | | daily check-in on **Today**; manager under **Goals** |
| `journal`, `journal_wizard`, `journal_entry_detail/{id}` | ◐ | | | | journal is **quick-capture**, not a Hub sub-tab |
| `focus_setup` | ● | | | | launched from Today / a goal |
| `decision_journal`, `decision_detail/{id}`, `decision_review` | | | | ● | grouped under **You → Decisions** |
| `life_balance`, `causal_insights`, `retrospective`, `health`, `screen_time_insight`, `analytics` | | | ● | | **consolidated** into the new **Insights** tab |
| `becoming`, `your_wiring` | | | | ● | identity home under **You** |
| `abilities`, `ability_detail/{id}`, `create_ability`, `achievements` | | | | ● | gamification under **You** (drop "My Units" label) |
| `ai_chat`, `ai_chat/{id}`, coach CRUD | | | | ● | coach under **You** (+ optional Today affordance) |
| `reminders`, `backup_settings`, `feedback`, `search`, `story_reader` | | | | ● | system/utility |

> **`terminology.md` follow-up:** the authoritative route list lives in
> `../lifeplanner-assets/docs/terminology.md` (a separate, non-git asset repo). It should be updated
> to this map when the routes are implemented in **D7** — tracked as a checklist item there rather
> than edited blind from here.

---

## 5. Navigation-model decisions

- **Bottom tab bar** (not a drawer/rail on phones): thumb-reachable, always visible, matches the
  benchmark set. A `NavigationRail` already exists for large screens — keep it for tablet/landscape.
- **Tabs are destinations, not a back-stack.** Switching tabs preserves each tab's own stack;
  re-tapping the active tab pops to its root. (Standard, learnable — P6.)
- **Modal vs. screen:** quick-capture and Choice Points are **bottom sheets** (lightweight, P1
  "a quick deliberate re-choice, not a chore"). Detail/editing flows are full screens.
- **Deep links** (Quick-Settings tiles, notifications, widgets) resolve to the owning tab + push the
  detail — e.g. "add goal" → Goals tab + wizard. (Fixes the dangling deep-link work, ref TRI-10.)
- **The "+" is contextual**, not a global create-anything menu: its default action matches the
  current tab (Today → capture; Goals → new goal; etc.).

---

## 6. Migration map (today → target)

| Today | Target |
|---|---|
| "Life" tab | **Today** (re-centered on agency) |
| "My Units" Hub (Journal/Goals/Habits/Abilities sub-tabs) | **dissolved** → Goals tab + Today check-ins + quick-capture + You |
| "You"/Profile junk-drawer | **You**, organized into Identity / Decisions / Growth / Coach / Settings |
| Insights scattered across 6 entry points | **Insights** tab (one home) |
| Goals/Habits 2 levels deep | **top-level** (Goals tab; habits on Today + manager) |

**Net:** 3 opaque tabs → **4 self-describing tabs**; the buried-objects and junk-drawer problems
are gone; every pillar feature has a coherent home.

---

## 7. Open questions

- **Habits: tab or not?** This proposal puts daily check-ins on **Today** and the manager under
  **Goals**, keeping 4 tabs. *Alternative:* a dedicated **Habits** tab (5 total) if usage data shows
  habits are the dominant daily action. Recommend validating with analytics before committing.
- **One headline metric?** (Carried from D1 §7.) If Insights gets an Oura-style headline (e.g. a
  "value-alignment pulse"), it lives at the top of the **Insights** tab — but only if we can show it
  without it reading as a verdict (P2). Decide in **D8**.
- **Coach prominence.** Thinking-partner positioning argues for a Today affordance; tab economy
  argues for keeping it under You. Decide in **D7** with the Today layout.

---

## 8. Handoff

- **D3 (tokens)** is unblocked and independent — start the Compose theme next.
- **D7 (core screens)** builds directly on this map: Today, Goals, Insights, You are its four
  primary canvases.
- **D11 (onboarding)** should drop the user into **Today** with the agency promise (P1) front-loaded.

*Next: **D3 — Design system foundations (color, type, spacing, tokens)** → a Compose Multiplatform
`Theme`, reconciled with the existing `tokens.json` / `figma-variables-*.json` in the assets repo.*
