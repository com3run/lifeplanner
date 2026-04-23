# GTM Engineering Gaps — Progress Tracker

**Last updated**: 2026-04-23  
**Campaign window**: Q2 2026, UK launch, 12-week Meta Ads campaign  
**Related strategy**: `00-strategy.md`, `01-uk-launch-campaign.md`, `02-monetization-plan.md`

---

## How to use this file

Each item has:
- **Status** — ✅ Done | 🔄 In progress | ⬜ Todo | 🚫 Blocked
- **Skill** — KMP (Kotlin Multiplatform), Swift (iOS native), Backend (Supabase / Edge Functions), Web (landing pages), Design
- **Owner** — assign when picking up
- **Notes** — context, blockers, or links

---

## Engineering Gaps

| # | Item | Skill | Status | Owner | Notes |
|---|---|---|---|---|---|
| 1 | Onboarding flow fixes (signout key clear, debug reset, race condition) | KMP | ✅ Done | — | Committed in v2.4 |
| 2 | Jamie — 7th coach (GoalCategory.FAMILY + CoachPersona + VM + UI) | KMP | ✅ Done | — | Committed in v2.4 |
| 3 | Meta App Events — 4 custom events (session_start, goal_created, coach_chat_started, habit_checkin) | KMP | ✅ Done | — | Committed in v2.4; Android wired to AppEventsLogger, iOS stubs ready for bridge |
| 4 | iOS Facebook native bridge (Swift `FacebookAnalyticsBridge` implementor in iosApp) | Swift | ⬜ Todo | — | Kotlin bridge pattern already in place; needs Swift class in iosApp that calls `FBSDKCoreKit.AppEvents.shared.logEvent()` |
| 5 | CAPI Supabase Edge Function (server-side Meta Conversions API) | Backend | ⬜ Todo | — | New edge function at `supabase/functions/meta-capi/`; receives `trial_started` + `trial_converted` from RevenueCat webhook; forwards to `https://graph.facebook.com/v19.0/{pixel_id}/events` |
| 6 | RevenueCat SDK integration + paywall UI (Phase 12, Week 9) | KMP | ⬜ Todo | — | Full spec in `02-monetization-plan.md` §5; App Store Connect + Play Console products must be created first |
| 7 | App Store / Play Store screenshots — must show all 7 coaches + Life Balance Wheel | Design | ⬜ Todo | — | Current screenshots show only 5–6 coaches; Jamie portrait needs to be available first |
| 8 | Jamie coach portrait image — upload to Supabase Storage | Design | ⬜ Todo | — | Path: `assets/coaches/jamie.png`; Midjourney prompt in `03-coach-visual-identity.md`; once uploaded the fallback emoji circle disappears automatically |
| 9 | `builtin_coaches` Supabase row for `jamie_family` | Backend | ⬜ Todo | — | Mirrors the hardcoded `CoachPersona.ALL_COACHES` entry; needed so remote fetch overrides local fallback with latest content/system prompts |
| 10 | `system_prompts` Supabase row for `jamie_family` | Backend | ⬜ Todo | — | Jamie's system prompt for the AI chat; see other coach prompts for format; reference `coach-collaboration-and-profile-aware-goals.md` for profile-aware prompt structure |
| 11 | Match quiz web page (`lifeplanner.tribe.az/match`) | Web | ⬜ Todo | — | 8-question quiz mapping user answers to a coach recommendation; Week 5 GTM deliverable per `01-uk-launch-campaign.md`; tech stack: Next.js or static page on Vercel |
| 12 | Meta Pixel on match quiz page | Web | ⬜ Todo | — | Depends on item 11; fire `Lead` event when user completes quiz + taps "Download App" CTA |

---

## Blocked items

None currently blocked.

---

## Skill definitions

| Skill | Who does it |
|---|---|
| KMP | Kotlin Multiplatform — shared `composeApp/src/commonMain`, Android, and iOS Kotlin |
| Swift | iOS native — `iosApp/` Swift code, Xcode project |
| Backend | Supabase — SQL migrations, Edge Functions (Deno/TypeScript), storage buckets |
| Web | Next.js / Vercel — `web/` directory or new Vercel project |
| Design | Figma / Midjourney / App Store Connect / Play Console screenshots |

---

## Done in v2.4

Items 1–3 shipped in the same commit as v2.4 version bump.

- **Item 1**: `AuthViewModelActions.kt` clears `COACH_ONBOARDING_KEY` on sign-out; `ProfileScreen.kt` has hidden long-press reset on version string; `App.kt` race condition fixed.
- **Item 2**: `GoalCategory.FAMILY(7)` added; `jamie_family` in `CoachPersona.ALL_COACHES`; full 3-question onboarding flow in `CoachOnboardingViewModel.kt` + `CoachOnboardingWidgets.kt` + `CoachOnboardingStepContent.kt`; `buildSituation()` writes to `PeopleSlice.familyContext`.
- **Item 3**: `FacebookAnalytics` common/android/ios updated with 4 new methods; `Analytics.appOpened()`, `goalCreated()`, `chatMessageSent(isFirstMessage=true)`, `habitCheckedIn()` all fire corresponding FB events.
