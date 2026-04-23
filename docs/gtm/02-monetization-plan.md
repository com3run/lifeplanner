# 02 — Monetization Plan (Phase 12 Execution)

**Last updated**: 2026-04-23
**Status**: Not yet built. Ships Week 9 of UK campaign (Jun 22, 2026).
**Related engineering work**: `implementation-plan.md` Phase 12.
**Related strategy**: `00-strategy.md` (Bet 3 — freemium + service upsells beats pure subscription).

---

## 1. Monetization model — two revenue streams

### Stream A — Subscription (primary, short-term)
Classic freemium SaaS model. Free tier with meaningful value; paid unlocks the full council + premium features.

### Stream B — Coach-triggered services (secondary, medium-term)
Per `coach-collaboration-and-profile-aware-goals.md` §10: each coach has a monetizable service they can surface to eligible users. These sell individually or bundle into a higher-tier subscription.

**Bet**: Stream A funds the lights; Stream B is where the real margin lives once the user trusts the product. Ship Stream A Week 9, start seeding Stream B hooks in-app during Week 11–12, launch the first paid service in Q3.

---

## 2. Pricing tiers

Three tiers. Keep it simple. No more than three.

### Free — "Meet your Guide"
- **Access**: Luna only (your Guide + orchestrator). The other 6 coaches are previewed but locked.
- **Limits**: 10 messages per day with Luna; 3 active goals; 3 habits; basic journal; Life Balance Wheel read-only.
- **Purpose**: prove the product works. Let the user experience the Luna onboarding, first goal, first habit check-in.
- **Conversion moment**: when the user explicitly tries to open a locked coach (e.g. taps Alex's portrait during a career question). Show a contextual upsell: "Alex is your Strategist. Unlock him + 5 more coaches with Premium."

### Premium — "Your Full Council" — **£6.99/month or £49/year** (30% annual discount)
- **Access**: All 7 coaches. Council mode unlocked (group chat with 2–3 coaches). Unlimited messages. Unlimited goals, habits, journal. Profile-aware goal creation (per `coach-collaboration-and-profile-aware-goals.md`). Life Balance Wheel with AI insights. Weekly/monthly AI reviews.
- **Purpose**: the mass-market paid tier. This is what the vast majority of paying users sign up for.

### Premium+ — "Your Tribe at Work" — **£12.99/month** (launched Q3, not part of this plan)
- Everything in Premium plus:
  - Proactive coaching (coaches initiate check-ins based on stress/life changes).
  - Priority GPT-4o / Claude access (vs. default Gemini Flash).
  - Access to paid services at discounted rates (Stream B bundle pricing).
  - Early access to new features.
- **Purpose**: premium segmentation; captures high-intent users willing to pay more for a richer experience.

### Regional pricing (App Store & Play Store auto-handled)

Use App Store Connect / Play Console automatic pricing tiers. Rough anchor points the auto-pricing will hit:

| Market | Premium monthly (local equivalent of £6.99) |
|---|---|
| UK | £6.99 |
| US | $7.99 |
| Canada | CAD 9.99 |
| Eurozone | €7.99 |
| Australia | AUD 11.99 |
| Mexico | MXN 129 |
| Brazil | BRL 24.99 |
| India | INR 299 |

Annual pricing mirrors the 30% discount structure.

### Why £6.99 and not £9.99?

- Calm Premium sits at £8.99; Headspace at £9.99. We're a NEW entrant in a crowded space — undercutting by ~20% lowers the first-month trial threshold.
- Replika Pro sits at £7.99 and is our closest "AI companion" competitor in mind-share; matching their anchor removes "why is this more than Replika?" objection.
- We can raise to £7.99 in Q3 after 200+ paying users prove retention; starting too low and raising is fine, starting too high and lowering kills trust.

---

## 3. Paywall placement — contextual over blocking

A user who hits a paywall because they tried to do something meaningful converts at 2–3× the rate of a user who hits a paywall because the app demanded it.

### Day 1–6: No paywall seen
User explores Luna, sets first goal, logs first habit. **Nothing is paywalled during onboarding or the first 6 days.** This is retention, not revenue. Users who churn Day 1 can't pay you.

### Day 7: Soft paywall after activation
A user who has completed onboarding, set a goal, AND either logged a habit or journal entry sees a non-blocking banner on Day 7:

> "You've been busy. Meet the other 6 coaches on your Tribe — first 7 days free, cancel anytime."

Tap → trial paywall. Skip → banner disappears; shows again every 3 days, non-aggressive.

### Contextual paywalls (always on, from Day 1)
These are the only "hard" paywalls:

- User taps a locked coach's portrait → small modal: "[Coach] is your [Title]. They're unlocked with Premium." Two buttons: "Start 7-day free trial" / "Not now".
- User tries to start a Council chat (group mode) → same pattern.
- User hits the 10-message daily cap with Luna → "You've used your 10 Luna messages today. Unlock unlimited with Premium."
- User tries to create a 4th active goal → "You've got 3 active goals. Focus is good — but if you want more, Premium removes the cap."

### Hard paywalls we refuse to ship
- No paywall during onboarding.
- No paywall before first successful chat with Luna.
- No paywall on goal creation for first goal ever.
- No "pay to continue" mid-flow.

---

## 4. Free trial mechanics

- **Length**: 7 days.
- **No credit card required to start trial**: Apple/Google pre-authorization handles billing on Day 8.
- **Day 6 push notification**: "Your trial ends tomorrow. Keep your Tribe?"
- **Day 7 in-app**: trial-end modal with "Continue Premium" / "Switch to Free" (no hostile copy like "Are you sure you want to lose your coaches?").
- **Day 8+**: converts to paid OR downgrades to free (loses access to 6 coaches, keeps all their goal/habit/journal data).

### Grace period
If payment fails (card expired etc.), 3-day grace period with full access, then downgrade. Standard App Store / Play Store billing handles this; we match it in UI.

---

## 5. Technical stack — RevenueCat-first

Use **RevenueCat** for subscription management. Free up to $10K MTR, then 1%. Reasons:
- Cross-platform subscription state (iOS + Android + web billing if we ever add it) unified in one place.
- Built-in entitlement checks — simple boolean "hasCouncilAccess" that you read from anywhere in Compose Multiplatform.
- Paywall testing support (A/B different paywall UI without redeploying).
- Handles regional pricing, trial durations, grace period, restore purchase — all the hard parts.
- Webhook → Supabase Edge Function for server-side events.

### Integration steps (engineering)

1. **App Store Connect** — create subscription group "Premium". Add monthly + annual products. Pricing tier 7 / annual tier 49.
2. **Play Console** — equivalent: subscription + base plan + offer (trial, 30%-off annual).
3. **RevenueCat dashboard** — create project "LifePlanner", add iOS and Android apps, connect products, define "Premium" entitlement.
4. **App code** — install RevenueCat SDK (Compose Multiplatform binding via Kotlin Multiplatform — their official binding works with CMP per their 2024+ releases; if any issue, fall back to platform-specific native bindings). Wire up:
   - `PurchasesConfiguration` in app init.
   - `Purchases.shared.identify(userId)` after Firebase auth.
   - `Purchases.shared.getCustomerInfo { ... }` to check entitlement.
   - `Purchases.shared.purchase(package) { ... }` on paywall CTA.
5. **Paywall UI** — build one native Compose paywall matching the design in §7. Use RevenueCat's remote paywall config for A/B testing layout variants.
6. **Webhook** — Supabase Edge Function receives RevenueCat events (`INITIAL_PURCHASE`, `RENEWAL`, `CANCELLATION`, `EXPIRATION`). Update a `subscriptions` table in Postgres for server-side logic.
7. **Meta App Events + CAPI** — fire `Subscribe` event on `INITIAL_PURCHASE`. This closes the loop for ad attribution.

### Feature gating in code

One utility:
```kotlin
suspend fun hasPremium(): Boolean =
    Purchases.sharedInstance.awaitCustomerInfo()
        .entitlements["premium"]?.isActive == true
```

Wrap every gated feature in a `PremiumGate` composable that either renders the feature or a contextual upsell modal.

---

## 6. Conversion events — what to track

| Event | When | Purpose |
|---|---|---|
| `paywall_view` | Paywall shown | Measure exposure |
| `paywall_start_trial_tap` | Trial CTA tapped | Intent signal |
| `paywall_skip_tap` | Skip CTA tapped | Objection signal |
| `trial_started` | RevenueCat `INITIAL_PURCHASE` with trial | Top of the funnel |
| `trial_day_3_active` | Day 3 of trial, session > 0 | Engagement signal |
| `trial_converted` | Day 8 → paid without cancellation | Conversion |
| `trial_cancelled` | Cancelled before Day 8 | Churn signal |
| `paid_renewed` | First paid renewal (Day 37) | Retention validation |
| `paid_cancelled` | Cancellation of paid subscription | Churn + reason prompt |

Send all to Firebase Analytics, PostHog, and Meta CAPI. Send only `trial_started` and `trial_converted` to Meta (as standard `StartTrial` and `Subscribe` events) — they're your ad-optimization signals.

---

## 7. Paywall UI spec

One paywall, three entry contexts (locked coach, Council, Day 7 banner). Content above the fold adapts to context; the purchase section is identical.

### Layout (mobile, 9:19.5)

**Top block — contextual** (40% of screen):
- Context 1 (locked coach): full-width portrait of that coach + their title + one-line why-you-need-them.
- Context 2 (Council): composite of 3 coach portraits overlapping + "A Council for your whole life".
- Context 3 (Day 7 banner): grid of 6 locked coaches + Luna at center + "You've met Luna. Meet the rest."

**Middle block — always the same** (35%):
- Headline: "Your Full Council"
- 4 value bullets (with check icons):
  - All 7 coaches, unlimited messages.
  - Council mode (chat with 2–3 coaches together).
  - Profile-aware goals that actually fit your life.
  - Weekly AI reviews + Life Balance insights.
- Social proof (once we have it, Week 11+): 1-line user quote.

**Bottom block — purchase** (25%):
- Tile 1: "Monthly — £6.99/month" with "7 days free" ribbon.
- Tile 2: "Yearly — £49/year" with "Save 30%" ribbon. Default selected.
- Big CTA button: "Start 7-day free trial" (changes to "Subscribe for £49/year" after 7-day trial is consumed).
- Fine print: "Cancel anytime in Settings. No charge during trial." + App Store / Play Store required legal links.
- Small "Maybe later" text button below the main CTA.

### Behaviour
- Closing the paywall (top-left X) = logged as `paywall_skip_tap`; user continues in Free tier.
- "Maybe later" = same as close.
- No dark patterns. No fake timers ("Offer expires in 10 min!"). No pre-selected "Apply" on a free trial that charges immediately.

---

## 8. Stream B — Coach-triggered services (Q3 seed planning)

Not launching this quarter, but **seed the hooks in-app during Week 11–12** so users see the possibility and we collect demand signal.

### Services planned (one per specialist coach)

| Coach | Service | Typical price | Trigger |
|---|---|---|---|
| Alex (Career) | Resume review + rewrite | £49 one-time OR £29 with Premium | When `career.wantsResumeService = true` or user asks for a job-hunt goal |
| Morgan (Money) | 30-min personalized budget review (AI-driven output) | £29 one-time | When user sets a savings or debt-reduction goal |
| Kai (Body) | 14-day personalized meal + movement plan | £19 one-time | When user sets a fitness or energy goal |
| Sam (Social) | Scripted conversation templates (hard talks, reconnections, networking intros) | £14 one-time OR included in Premium | When user sets a social or conflict goal |
| River (Purpose) | Guided "Values Compass" audio + workbook | £19 one-time OR included in Premium | When user's purpose slice confidence < 0.4 |
| Jamie (Family) | Family tension de-escalation workbook | £19 one-time | When user describes a family conflict in journal |
| Luna (Wellbeing) | Custom weekly "Luna report" — narrative life-audit delivered each Sunday | Included in Premium+ | Automatic for Premium+ users |

### Seeding in-app (Week 11+)
Each specialist coach, when their trigger fires, shows a soft card in chat:
> "I can build this for you when it's ready. Want me to let you know when it launches?" → tap to join waitlist.

This does three things: (a) validates demand, (b) builds a warm list for Q3 services launch, (c) signals to users "this app is going somewhere".

### Stream B target for Q3

Not a revenue target for this plan; a readiness target:
- 30%+ of Premium users click at least one "notify me" card.
- 10%+ of Premium users join 2+ service waitlists.

These numbers say "users see the services as valuable extensions, not add-ons we're pushing."

---

## 9. Pricing psychology — UK-specific notes

- **£6.99 reads as "under £7"** in UK decision-making; £7.99 reads as "£8". The 9 trick works more reliably in UK than in US.
- **"Free trial" > "Free version"** in UK conversion data — UK consumers are more familiar with trial-to-paid patterns (Netflix, Amazon Prime, Disney+ all use this). Lead with trial.
- **Annual framing**: "£49/year" reads as ~"£1 per week" mentally. Surface this framing on the annual tile: "Just £0.94 per week".
- **Regional price transparency**: UK users occasionally check US pricing for comparison. Keep UK price in line with USD equivalent to avoid "why do Brits pay more" complaints (a real issue for SaaS in UK).

---

## 10. What ships Week 9 (launch checklist)

Ready-to-launch gate for monetization:

### Product
- [ ] Subscription products created in App Store Connect (Premium monthly + Premium annual).
- [ ] Subscription products created in Play Console.
- [ ] RevenueCat project configured; entitlements mapped.
- [ ] Compose Multiplatform app integrates RevenueCat SDK.
- [ ] Paywall UI built (per §7) with 3 contexts.
- [ ] Feature gating wrapped around 6 locked coaches + Council mode + premium features.
- [ ] Trial logic tested end-to-end on sandbox (iOS) and license testing (Android).
- [ ] Meta `StartTrial` + `Subscribe` events firing via CAPI.

### Data
- [ ] `subscriptions` table in Supabase with fields from RevenueCat webhook.
- [ ] Edge Function receiving RevenueCat events and updating table.
- [ ] Firebase Analytics custom events for paywall funnel.
- [ ] PostHog dashboard: paywall view → trial start → trial conversion → renewal.

### Legal / policy
- [ ] Privacy policy updated (mention RevenueCat as subprocessor).
- [ ] Terms of Service updated (subscription terms, cancellation policy).
- [ ] App Store / Play Store subscription metadata complete (descriptions, subscription group display name).
- [ ] GDPR data-export path updated to include subscription data.

---

## 11. Metrics to judge whether it's working

Week 9–12 (first month of monetization):

| Metric | Target | Read at |
|---|---|---|
| % of D7 retained users who view paywall | 60%+ | Week 10 |
| Paywall view → trial start CVR | 25%+ | Week 10 |
| Trial start → trial → paid CVR | 30–40% | Week 10–11 |
| Net MRR end of Week 12 | £200–£500 | End of quarter |
| Day-37 first-renewal rate | 80%+ | Q3 |

If paywall view → trial start is below 15% → paywall UI or copy is the issue. If trial → paid is below 20% → onboarding or Day 1–7 product experience is the issue. Diagnose and iterate.

---

*End of monetization plan v1.*
