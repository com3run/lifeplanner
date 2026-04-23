# Coach Collaboration & Profile-Aware Goal Creation

**Status:** Draft for discussion
**Owner:** Kamran
**Last updated:** 2026-04-19

---

## TL;DR

Turn LifePlanner's coach personas from isolated chat-bots into a **collaborative council** that progressively learns the user's real-life situation across all six goal categories, stores it in a shared memory (`UserSituation`), and uses that memory to produce **dramatically more personalized goals** than any competitor.

Two tightly linked systems:

1. **Onboarding & Memory** — Luna orchestrates specialist coaches (Alex, Morgan, Kai, Sam, River) to fill in slices of the user's profile through natural conversation. Age-adaptive questions, gap detection, no repetition.
2. **Profile-Aware Goal Creation** — When the user creates a goal, the responsible coach reads the profile, skips questions the app already knows, drafts a SMART goal calibrated to the user's life, and invites adjacent coaches to weigh in.

The flywheel: **Data collected → Goal gets better → User trusts app → User shares more → Goal gets even better.**

---

## 1. Why this matters

Today our goal creation is one-size-fits-all. Two users with the same category request get nearly identical goals, regardless of age, life stage, stress level, or history.

A 27-year-old junior designer stuck in a banking job and a 45-year-old senior manager with two kids should **not** get the same "career" goal. They should barely recognize each other's outputs.

This design is the bridge from "AI generates a goal" to "AI generates **the right goal for you**."

It also unlocks a strategic data asset: every conversation enriches a structured profile we can use to ship smarter features (personalized benchmarks, predictive nudges) and monetizable services (resume builder, budget coaching, meal plans, meditation tracks).

---

## 2. The six categories (recap)

Straight from `domain/enum/GoalCategory.kt` and `domain/model/LifeBalance.kt`:

| # | Category | Owning coach | Emoji |
|---|---|---|---|
| 1 | Career | Alex | 💼 |
| 2 | Money | Morgan | 💰 |
| 3 | Body | Kai | 💪 |
| 4 | People | Sam | 🤝 |
| 5 | Wellbeing | Luna (also orchestrator) | ✨ |
| 6 | Purpose | River | 🧘 |

Luna is special — she owns the Wellbeing slice **and** acts as the conductor for the whole council.

---

## 3. The Council Model

### Analogy

A good family doctor doesn't ask you your full history every visit. They have your chart. They call in a specialist when needed, and the specialist already knows the basics before they walk in the room.

That's the Council: Luna is the GP, specialists have a shared chart, and the user never has to repeat themselves.

### Roles

- **Luna (Orchestrator)** — opens every session, decides which specialist speaks next, performs sanity checks, never hands the mic to more than 2 adjacent coaches per goal.
- **Specialists (Alex, Morgan, Kai, Sam, River)** — own one slice each. They read before they speak. They fill gaps. They draft category-specific outputs.
- **The Council** — a virtual meeting where multiple coaches weigh in on a single user moment (e.g. a big goal, a life change, a struggle).

### Relay race, shared notebook

Each conversational turn follows this pattern:

1. Coach reads `UserSituation` from the shared memory.
2. Coach acts (asks or drafts).
3. Coach writes back updated slots + confidence.
4. Coach hands control to Luna or to another specialist.

Control is explicit. Nothing is implicit. This is what makes the system debuggable and testable.

---

## 4. Shared Memory — `UserSituation`

A single object per user, Supabase-backed (reuse existing `UserRepositoryImpl` infra). Each coach owns one slice and reads all others.

```kotlin
data class UserSituation(
    val meta: MetaSlice,          // Luna owns
    val career: CareerSlice,      // Alex owns
    val money: MoneySlice,        // Morgan owns
    val body: BodySlice,          // Kai owns
    val people: PeopleSlice,      // Sam owns
    val purpose: PurposeSlice,    // River owns
    val completeness: Map<GoalCategory, Float>,
    val lastUpdatedBy: String,
    val updatedAt: Instant
)

data class MetaSlice(
    val name: String? = null,
    val age: Int? = null,
    val lifeStage: LifeStage? = null,       // STUDENT, EARLY_CAREER, FAMILY, MID_CAREER, SENIOR, RETIRED
    val topPriority: GoalCategory? = null,
    val overallMood: Int? = null,           // 1-10
    val stressLevel: Int? = null,           // 1-10
    val sleepQuality: Int? = null,          // 1-10
    val confidence: Float = 0f
)

data class CareerSlice(
    val status: EmploymentStatus? = null,   // STUDENT | EMPLOYED | UNEMPLOYED | FREELANCE | ENTREPRENEUR | RETIRED
    val role: String? = null,
    val industry: String? = null,
    val yearsExperience: Int? = null,
    val topSkills: List<String> = emptyList(),
    val hasResume: Boolean = false,
    val resumeUrl: String? = null,
    val wantsResumeService: Boolean? = null,
    val careerGoal: String? = null,
    val confidence: Float = 0f
)

data class MoneySlice(
    val incomeBand: IncomeBand? = null,     // banded, not exact
    val currency: String? = null,
    val savingsHabit: SavingsHabit? = null, // NONE | SPORADIC | CONSISTENT | AGGRESSIVE
    val hasDebt: Boolean? = null,
    val financialGoal: String? = null,
    val riskAppetite: RiskAppetite? = null, // LOW | MEDIUM | HIGH
    val confidence: Float = 0f
)

data class BodySlice(
    val activityLevel: ActivityLevel? = null,
    val sleepHours: Float? = null,
    val dietPattern: DietPattern? = null,
    val energyRating: Int? = null,          // 1-10
    val flags: List<HealthFlag> = emptyList(),
    val confidence: Float = 0f
)

data class PeopleSlice(
    val relationshipStatus: RelationshipStatus? = null,
    val closeCircleSize: CircleSize? = null,
    val familyContext: String? = null,      // free text summary
    val socialEnergy: SocialEnergy? = null, // INTROVERT | AMBIVERT | EXTROVERT
    val confidence: Float = 0f
)

data class PurposeSlice(
    val topValues: List<String> = emptyList(),      // up to 3
    val mindfulnessPractice: Boolean? = null,
    val meaningSources: List<String> = emptyList(),
    val longTermVision: String? = null,
    val confidence: Float = 0f
)
```

### Confidence rules

- `< 0.4` — coach must fill before drafting a goal.
- `0.4–0.7` — coach may re-ask if the user opens the door.
- `> 0.7` — considered "known"; **never re-ask**.
- `> 0.9` — used as strong signal in cross-coach reasoning.

### Write discipline

Only the owning coach can mutate their slice. Cross-category insights go into a separate `CoachNote` stream, not into another coach's slice. This keeps the memory clean and auditable.

---

## 5. Onboarding Journey — filling the chart

### Phase 1: Luna bootstrap (3 questions max)

1. Name + rough age.
2. Top priority today: Career / Money / Body / People / Wellbeing / Purpose.
3. Overall stress and sleep (1–10).

After this, Luna knows *who* the user is and *where* to hand off.

### Phase 2: First specialist (5 questions max)

Luna hands to the specialist of the user's top priority. That specialist fills their slice to `confidence ≥ 0.8` and hands back.

### Phase 3: Rotating fill

Luna picks the specialist with the **lowest current slice confidence** and invites the user: *"Mind if Morgan joins in for a quick money check-in? 5 questions tops."*

The user can always defer: *"Not now"* → coach queued for later.

### Age-adaptive question branching

The same slot is asked differently per age:

| Age band | Career — "employment status" prompt |
|---|---|
| 13–17 | "Which grade are you in? Any part-time work or side hustles?" |
| 18–22 | "Are you studying, working, or both right now?" |
| 23–35 | "Are you employed full-time, freelancing, job-hunting, or running your own thing?" |
| 36–55 | "Tell me about your current role and how long you've been in it." |
| 55+ | "Are you still working, semi-retired, or fully retired? Any passion projects?" |

Same enum slot fills. Different conversational surface. This is what makes it feel human.

### Upsell hooks

When Alex detects `status ∈ {UNEMPLOYED, FREELANCE, EMPLOYED-considering-switch}`, he surfaces: *"Do you have an up-to-date resume, or would you like me to help you build one?"*

Stored as `career.wantsResumeService = true`. This is the seed for a paid resume-builder service. The same pattern applies to Morgan (budget coaching), Kai (meal plans), River (meditation tracks).

---

## 6. Profile-Aware Goal Creation

This is where the data actually bends the output.

### Flow

```
User taps "Create Goal" → picks category
  ↓
Coach reads UserSituation
  ↓
Detect missing slots for goal quality
  ↓
Ask 0–3 gap-filling questions (skip if all known)
  ↓
Coach drafts SMART goal using FULL profile
  ↓
Luna sanity-checks vs. stress / sleep / life balance
  ↓
(Optional) Council weigh-in: 1–2 adjacent coaches
  ↓
Deliver personalized goal + coach notes
```

### Missing-slot detection

```kotlin
fun Alex.missingSlotsForGoalDraft(situation: UserSituation): List<String> {
    val c = situation.career
    return buildList {
        if (c.status == null) add("employment_status")
        if (c.status == EMPLOYED && c.yearsExperience == null) add("years_experience")
        if (c.careerGoal == null) add("career_ambition")
    }
}
```

If the list is empty, the coach **skips straight to drafting**. That's the "the app already knows me" moment.

### Council weigh-in rules

To avoid spamming the user with 6 opinions, adjacent coaches join only when a trigger fires. Max 2 per goal.

| Trigger | Invites |
|---|---|
| Ambitious goal AND `body.sleepHours < 6` OR `body.energyRating < 5` | Kai |
| Goal has financial implications (career move, relocation, equipment) | Morgan |
| Goal requires networking, public speaking, or outreach | Sam |
| Goal conflicts with stated values or current stress level | River |
| Goal crosses multiple categories (e.g. career + family time) | Luna expands scope |

### Worked example — same category, different users

**Leyla, 27, junior designer, hates her job, 3 yrs exp, sleep 5/10, introvert.**
Asks: *"I want a career goal."*

Alex drafts:
> "Transition from banking to product design at a tech company within 8 months — rebuild resume this week, complete 2 portfolio case studies by June, apply to 3 target companies/week from July."

Luna's note: "Your sleep is rough — start at 3 applications/week, not 10. Sustainable > aggressive."
Sam's note: "You're introverted — I'll draft a LinkedIn outreach script that feels less pushy."

**Murad, 45, senior manager, 2 kids, 8 yrs experience, high stress, extrovert.**
Asks: *"I want a career goal."*

Alex drafts:
> "Move from Senior Manager to Director in 12 months — focus on executive presence, lead one cross-functional initiative this quarter, mentor 2 juniors to demonstrate leadership scalability."

Luna's note: "Stress is high and you're a parent — mentor sessions during work hours only."
Morgan's note: "A Director move is ~30% more comp in your market. Want me to open a parallel money goal?"

Same ask. Wildly different outputs. That's the value.

---

## 7. System Prompt Blueprints

Add a `GOAL CREATION MODE` block to each specialist coach's system prompt:

```
GOAL CREATION MODE

When the user opens goal creation in your category:
1. Read UserSituation first. Do NOT ask questions whose answers are
   stored with confidence > 0.7.
2. Identify missing slots. Ask up to 3 gap-filling questions max.
3. Draft a SMART goal using the FULL profile, not just your slice:
   - Calibrate ambition to current stress/sleep (meta + body slices)
   - Match tone/style to personality (people slice)
   - Fit timeline to life stage (meta.age + meta.lifeStage)
4. Flag adjacent coach(es) who should weigh in.

OUTPUT (hidden JSON):
{
  "questions_for_user": [],
  "goal_draft": { ... },
  "memory_update": { "career": { ... } },
  "request_council_input": ["luna_general", "kai_fitness"]
}
```

Luna's prompt gets a matching `ORCHESTRATOR MODE` block with handoff rules, cross-check logic, and sanity-check templates.

Full prompt templates for each of the six coaches live in **Appendix A** (to be drafted alongside code work).

---

## 8. Implementation phases

### Phase 0 — Foundations (1 week)
- Add data classes: `UserSituation`, all slices, supporting enums.
- Add `UserSituationRepository` with Supabase table + sync.
- Extend `CoachRepositoryImpl` to read/write slice updates.

### Phase 1 — Orchestrator (1–2 weeks)
- Build `CoachOrchestrator` service: handoff logic, missing-slot detection, confidence updates.
- Update system prompts for all six coaches with ORCHESTRATOR / GOAL CREATION MODE blocks.
- Unit tests: handoff determinism, no-repeat-questions, confidence math.

### Phase 2 — Onboarding journey (1 week)
- New onboarding flow: Luna → top-priority specialist → rotating fill.
- Age-adaptive question templates per slot.
- Progress indicator: "Your profile is 40% complete."

### Phase 3 — Profile-aware goal creation (1–2 weeks)
- Extend `GoalCreationWizardScreen` with a "skip if known" step.
- Render coach notes card in the wizard preview.
- Council weigh-in UI (compact, dismissible).

### Phase 4 — Services & monetization hooks (parallel track)
- Resume builder (first service).
- Flag-driven upsell cards in each coach.
- Analytics: what percentage of users say yes to each service.

---

## 9. Privacy & trust

Data sensitivity goes up sharply compared to today's app. Required before ship:

- **Consent screen** at first profile-fill. Plain-language: what we collect, why, who can see it.
- **Slice-level visibility toggle** — user can hide Money or People from the council.
- **Export & delete** — user can download their `UserSituation` JSON or wipe it.
- **No PII in logs** — coach memory writes never hit any log tier above DEBUG.
- **Aggregate-only analytics** — product team sees counts and distributions, never individual slices.

Building trust *into the first session* is what unlocks the deeper data later.

---

## 10. Business implications

Each filled slice isn't just UX — it's a product moat.

- **Richer features next release:** personalized benchmarks ("You sleep less than 70% of 25–30 yr olds — here's what helps"), predictive nudges ("Your stress usually spikes on Tuesdays — let's pre-plan").
- **Monetizable services:** resume building (Alex), budget planning (Morgan), meal plans (Kai), meditation tracks (River). Each triggers off a known slice flag.
- **Data moat:** No generic LLM wrapper can compete, because they don't have your user's 6-slice profile. Every message makes the moat deeper.

Ship the privacy promise cleanly, and this becomes a genuine differentiator.

---

## 11. Open questions

- Do we let users name their own coach variants, or lock to the built-in six?
- Should the council be visible to the user (coaches "speaking up" by name) or invisible (Luna synthesizes)? My instinct: visible — it's more charming and builds attachment.
- How do we handle **profile drift** — a user whose situation changes (job loss, new baby, relocation)? Proposal: each slice has a 6-month staleness flag that prompts a refresh chat.
- Resume service: do we build it in-house (more work, more margin) or partner with an existing resume tool (faster, lower margin)?
- What's the onboarding completion floor before goal creation is enabled? Proposal: Meta slice `confidence ≥ 0.6` minimum; category slice of chosen goal `≥ 0.4`.

---

## 12. Next steps

1. Review this doc with the team. Lock the data schema.
2. Build Phase 0 (data classes + repo).
3. Prototype `CoachOrchestrator` with Leyla + Murad synthetic personas in integration tests.
4. Rewrite system prompts for all six coaches and run evals.
5. Wire the new flow into `GoalCreationWizardScreen` behind a feature flag.

---

## Appendix A — Coach prompt templates (to draft)

Placeholders for:
- Luna (orchestrator + wellbeing specialist)
- Alex (career)
- Morgan (money)
- Kai (body)
- Sam (people)
- River (purpose)

Each template covers: ownership, question strategy, goal creation mode, output schema, tone rules, upsell triggers.

## Appendix B — Synthetic personas for testing

- **Leyla** — 27, Baku, junior designer in banking, introvert, poor sleep.
- **Murad** — 45, Baku, senior manager, 2 kids, high stress, extrovert.
- **Aylin** — 19, student, side hustle on TikTok, high energy, low savings.
- **Jahid** — 62, retired engineer, values legacy and family, low social activity.
- **Nigar** — 34, freelance developer, solo, high savings, mid stress.

Each persona has a pre-filled `UserSituation` that can seed integration tests and demos.
