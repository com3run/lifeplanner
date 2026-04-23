# 00 — Strategy & North Star

**Last updated**: 2026-04-23
**Status**: v1 — locked for this quarter unless month-retro data forces a revision.

---

## Mission

**LifePlanner helps people see their whole life clearly, and work on it one area at a time, with a council of AI coaches who actually know them.**

This is not a meditation app. It is not a habit tracker. It is not a chatbot friend. It is the first AI life-planner with a **Council Model** — one orchestrator (Luna) plus six specialists who share a profile and hand off to each other so the user never has to repeat themselves.

## One-line positioning

> "Your life has 7 areas. Your Tribe has 7 coaches. One for each."

Alternates (for A/B testing, not replacing the primary):
- "Other AI apps give you one assistant. Tribe gives you a whole council."
- "Career. Money. Body. Social. Emotional. Spiritual. Family. One app. Seven coaches."

## Category we are in (and are not)

| Category | In / out | Why |
|---|---|---|
| AI life-planner + council | **In** | Our unique slot. Nothing else positioned here. |
| Meditation app (Calm, Headspace) | Out | Narrower, heavier on audio content, not our game. |
| AI companion (Replika, Pi, Character.AI) | Out | Single-AI model; we are multi-coach with structured outputs (goals, habits, journal). |
| Enterprise coaching (BetterUp) | Out | Human-coach bottleneck; we are AI-native. |
| Habit tracker (Fabulous, Finch) | Adjacent | We include habits + gamification but lead with coaches. |

The positioning earns permission by leaning into what the product already has that nobody else does — **seven specialist coaches with shared memory** and a **structured life-balance model** (7 categories, 8-area wheel).

## Target audience (global)

**Primary — the "Ambitious Generalist"**:
- 25–40, urban professional.
- Juggling career + money + relationships + health + meaning. Feels scattered.
- Has tried 3+ self-improvement apps; none stuck.
- Comfortable with AI, unmet by Replika-style companions.
- English-speaking. Household income $40K+ USD equivalent.
- High phone engagement; 1–3 active subscription apps.

**Secondary (Q3 2026, not in this plan)**:
- 18–24 students preparing for independent life.
- Immigrants / expats re-building identity in a new country.
- 40+ professionals re-orienting around purpose.

## Geography for this quarter

Home market for the team: Azerbaijan (operations + beta, not paid).
Paid acquisition market this quarter: **United Kingdom — primary** (see [`01-uk-launch-campaign.md`](./01-uk-launch-campaign.md) for why).

Tier-1 expansion candidates (Q3 based on UK signal):
- Canada, Australia, Ireland (English tier-1 — easiest creative transfer).
- United States (highest paid conversion, highest CPI — layer in via retargeting + lookalikes from UK winners).
- Germany (Turkish/AZ diaspora + local premium; requires German creative).

Tier-2 volume markets (Q3, only if UK economics prove and we need top-of-funnel volume):
- Philippines, India, Brazil, Mexico.

## 3-month north-star metrics

Anchored to Q2 2026 (April 23 – July 22).

| Metric | Target | Why it matters |
|---|---|---|
| Total paid installs (UK) | 800–1,200 | Minimum volume for Meta algorithm to optimize and for cohort retention to be readable. |
| D7 retention | ≥ 25% | Baseline for lifestyle apps. Below this, ads can't save you. |
| D30 retention | ≥ 12% | Early signal that coaches actually provide continuing value. |
| Paid subscribers end of Q2 | 30–60 | Tests monetization (Phase 12 launch mid-quarter). |
| MRR end of Q2 | $200–$500 | Not profit; signal sufficient to justify 5–10× scaling. |
| Cost per install (blended) | < £5 (≈ $6.30) | UK lifestyle benchmark floor for Meta v2 2026. |

**What we are NOT optimizing for this quarter**:
- Vanity install volume from tier-2 countries. Two Philippines installs at $0.30 each are not worth one UK install at $4 to us in Q2.
- Break-even on ad spend. That comes Q3–Q4.
- Social-media follower count. Growth there is a byproduct, not a goal.

## Strategic bets

Three bets this roadmap is built on. If any one of them is wrong, the plan needs revisiting.

### Bet 1: The Council Model is our unfair advantage, not feature creep
We assume that "7 coaches + orchestrator + shared memory + life balance wheel" is perceived as a **coherent story**, not as "too many features". UK creative leans heavily on the visual: a portrait roster of seven cinematic mentors, each owning a life area.

**Falsification signal**: if quiz-based ads (show-the-roster) underperform single-coach ads (just Luna) by more than 2× CTR after 2 weeks, the Council is cognitive overload for the UK audience and we need a staged reveal strategy.

### Bet 2: UK is the right tier-1 entry market
We assume UK's combination of English-only creative + tier-1 paid conversion + manageable CPI (relative to US) makes it the best single market to test the product-hook fit.

**Falsification signal**: if after $300 spend on UK our CPI is >£7 AND D1 retention is <30% AND our top creative's CTR is <1.5%, we swap primary to Canada or Australia for month 2.

### Bet 3: Freemium + service upsells beats pure subscription
We assume users will pay for premium access to all coaches, but that meaningful revenue comes from **coach-triggered services** — resume builder (Alex), budget review (Morgan), personalized meal plan (Kai), etc. as described in `coach-collaboration-and-profile-aware-goals.md` §10.

**Falsification signal**: if 3 months into monetization, <3% of paid users engage any service upsell, we rebalance the monetization plan to subscription-only and reduce service scope.

## What has to be true for this to work

Product-level (status against `implementation-plan.md`):
- ✓ Core product ready (~95%; Phase 12 Monetization is the gap we close in this plan).
- ✓ Luna as conversational AI coach is live.
- ⚠ Multi-coach Council Model implementation (per `coach-collaboration-and-profile-aware-goals.md` Phase 0–3) ideally shipped by Week 8 — but the marketing story works with existing Luna + coach personas in chat mode while the council orchestration is being built.
- ⚠ Onboarding copy needs to foreshadow the Council even if the full orchestration lands mid-quarter.

Marketing-level:
- ✓ Meta Ads account active with spend history.
- ✓ Dedicated marketer to execute campaigns.
- Need: creative production capacity (Midjourney-based visuals, Reels editing, copy variants).
- Need: landing page at `lifeplanner.tribe.az/download` restructured per `01-uk-launch-campaign.md` §4.

## Decision log

| Date | Decision | Context |
|---|---|---|
| 2026-04-23 | UK first, not Azerbaijan | AZ is HQ-of-convenience, not the real market. Global-first positioning. |
| 2026-04-23 | English-only v1; add second language from month 2 data | Solo founder + $1,500 budget can't sustain multi-language quality. |
| 2026-04-23 | Universal premium-mentor voice, not sharp-challenge | Global audience; Kərimov-style is Azerbaijani flavor, not global differentiator. |
| 2026-04-23 | Lead with Council Model in creative | The real product moat; all 7 coaches become the hero visual asset. |

---

*When this file changes, add to the decision log so we can trace direction changes back to their trigger.*
