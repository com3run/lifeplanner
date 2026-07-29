# Spec: Community Journals ("Learn from the community")

Status: **Draft for review** · Owner: Kamran · Drafted 2026-07-28

## 1. Summary

Let users **share a journal entry publicly** so it becomes part of a browsable
**community learning feed** inside the existing Learn section. Others read real,
goal-oriented reflections ("how someone actually did it") instead of only
curated tips. Authors get a byline; the app gets fresh, human, SEO-friendly
content that reinforces the app's "being seen changes the game" accountability
theme (already a Learn lesson: *Tell someone*).

This is **public user-generated content (UGC)**, so it cannot ship as a simple
toggle. It needs a moderation + safety layer and a **separate public store**,
decoupled from the strictly-private `journal_entries` table.

## 2. Goals / Non-goals

**Goals**
- A reader-friendly **community feed** of shared journals, surfaced in Learn / For You.
- A **share flow** with preview, consent, and edit-before-publish.
- **Author identity** (display name + optional avatar), no raw account exposure.
- **Moderation**: automated pre-screen + report + human review path.
- A **premium** lever (see §7 — recommend gating richer authorship, not privacy).
- Reuse existing patterns: Supabase + RLS, `ai-proxy` edge function, the
  synced-entity playbook, the Learn hub UI.

**Non-goals (v1)**
- Comments / likes / follows (fast-follow, not v1).
- Full blog CMS. v1 shares single entries, not multi-post author sites.
- Real-time feeds. Polled/paginated is fine.
- Cross-posting private entries automatically. Sharing is always an explicit act.

## 3. Key product decisions (need Kamran sign-off)

### 3.1 Default-public vs. opt-in — **flag this one**
Kamran's framing: *"public by default for all AI-created journals, premium to make it private."*

Journals are **personal reflections**; defaulting them public is a real privacy
landmine (users may not realize; entries contain names, health, relationships,
locations). Recommendation:

- **Default private. Sharing is always an explicit, per-entry opt-in** with a
  clear preview of exactly what goes public and under what name.
- Keep the monetization intent by moving the paywall to **authorship value**, not
  privacy: free users can share individual entries; **premium unlocks a public
  Author page / "notes" blog** (byline page, custom handle, richer formatting,
  view analytics — the Gates Notes angle) and higher share limits.

If Kamran still wants default-public for AI-generated entries specifically, the
minimum bar is: a **one-time, explicit consent screen** at first AI-journal
creation ("your AI journals may appear in the community feed under <name>; turn
off anytime"), a persistent visible "Public" state on every such entry, and a
one-tap "make private" that is **free** (charging to un-publish personal content
is a reputational and likely legal risk — GDPR/right-to-withdraw). Documented
here so the decision is deliberate.

**Decision needed:** opt-in-per-entry (recommended) vs. default-public-with-consent.

### 3.2 What is an "AI-created journal"?
`JournalEntry.promptUsed != null` already marks entries created through the AI
journal wizard. Use that as the eligibility hint, but sharing eligibility should
be independent of it (any entry can be shared). Add an explicit
`is_ai_assisted` boolean on the shared record for filtering/labeling.

## 4. UX surfaces

1. **Share action** — on `JournalEntryDetailScreen` and the entry overflow menu:
   "Share to community". Opens the **Share sheet**.
2. **Share sheet** — shows: rendered preview, author-name field (default =
   profile display name, editable/pseudonymous), optional linked-goal context
   ("This was about: *Run a half marathon*"), a redaction hint ("remove names /
   private details"), a checkbox to confirm consent, and **Publish**. Publishing
   sends the content through moderation (§6) before it appears.
3. **Community feed** — a new section in the **Learn hub** ("From the community")
   + a For You feed card kind. Cards show title, author, mood, goal-category tag,
   read time, excerpt. Paginated / infinite scroll.
4. **Reader** — a `CommunityJournalDetailScreen` (Gates-Notes editorial style):
   large title, author byline + avatar, date, body, the goal it served, and a
   **Report** affordance + "Save"/"Turn this into a goal" CTAs (reuse Learn
   detail CTAs).
5. **My shared** — in Profile: list of the user's shared entries with status
   (Pending review / Published / Removed) and a one-tap **Unpublish** (free).
6. **Report flow** — reasons (Personal info / Harassment / Sexual / Violence /
   Spam / Other) → writes a `content_reports` row → auto-hides above a threshold,
   enters review queue.
7. **Premium** (if §3.1 recommendation): an **Author page** entry point + upsell.

## 5. Data model (Supabase)

Keep the public copy **separate** from private `journal_entries` (which stays
owner-only RLS). Publishing writes a **snapshot** into a new public table, so
editing/deleting the private entry never silently changes or exposes anything.

```sql
-- 5.1 Public snapshot of a shared journal (moderated, denormalized)
CREATE TABLE shared_journals (
    id                TEXT PRIMARY KEY,              -- new id, not the private entry id
    source_entry_id   TEXT,                          -- private entry it came from (nullable, no FK exposure)
    author_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    author_name       TEXT NOT NULL,                 -- display/pseudonym chosen at share time
    author_avatar_url TEXT,
    title             TEXT NOT NULL,
    body              TEXT NOT NULL,                  -- moderated snapshot
    mood              TEXT,
    goal_category     TEXT,                          -- denormalized category for filtering/tags
    goal_title        TEXT,                          -- "was about: ..." context, optional
    is_ai_assisted    BOOLEAN NOT NULL DEFAULT FALSE,
    read_minutes      INT     NOT NULL DEFAULT 1,
    status            TEXT    NOT NULL DEFAULT 'pending',  -- pending|approved|rejected|removed
    moderation_note   TEXT,                          -- model/reviewer rationale
    report_count      INT     NOT NULL DEFAULT 0,
    published_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_shared_status_pub ON shared_journals(status, published_at DESC);
CREATE INDEX idx_shared_author     ON shared_journals(author_id);
CREATE INDEX idx_shared_category   ON shared_journals(status, goal_category, published_at DESC);

-- 5.2 Reports
CREATE TABLE content_reports (
    id           TEXT PRIMARY KEY,
    shared_id    TEXT NOT NULL REFERENCES shared_journals(id) ON DELETE CASCADE,
    reporter_id  UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reason       TEXT NOT NULL,
    detail       TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (shared_id, reporter_id)   -- one report per user per item
);

-- 5.3 Optional: premium author page
CREATE TABLE author_profiles (
    author_id   UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    handle      TEXT UNIQUE,          -- premium custom handle
    bio         TEXT,
    is_public   BOOLEAN NOT NULL DEFAULT FALSE
);
```

**RLS**
- `shared_journals`: **public read only of `status='approved'`** for `authenticated`
  (and optionally `anon` if a web feed is wanted); author may `select` their own
  regardless of status; `insert` goes through an **edge function only** (no direct
  client insert — forces moderation); `update`/`delete` limited to the author for
  their own rows (unpublish) + `service_role` for moderation.
- `content_reports`: `insert` by any authenticated user (with `reporter_id =
  auth.uid()`); `select` none for clients (service role only).
- Never expose `author_id`/`user_id` to readers — the client reads
  `author_name`/`author_avatar_url` only.

## 6. Moderation pipeline

Publishing is **not** a direct table write. Flow:

1. Client calls edge function **`share-journal`** with the entry payload +
   author name + consent flag.
2. Function runs **automated moderation** via `ai-proxy` (OpenAI moderation
   endpoint / a classification prompt) for: sexual, harassment, self-harm,
   violence, hate, and a **PII pass** (emails, phone numbers, full addresses,
   other people's full names) with a redaction suggestion.
3. Result:
   - **clean** → insert `status='approved'`, `published_at=now()` → appears in feed.
   - **needs review** (borderline) → insert `status='pending'` → human queue.
   - **blocked** → reject with reasons shown to the author to edit & resubmit.
4. **Reports**: `report-content` edge function increments `report_count`; at a
   threshold (e.g. ≥3 distinct reporters or 1 severe reason) auto-flip
   `status='removed'` and enqueue for review.
5. **Human review**: reuse the existing **admin panel** (`webtest/public/admin.html`
   + `functions/src/adminpanel.ts` pattern) with a moderation queue view
   (approve / remove / ban author). *Note: that admin panel is Nomadify's — for
   LifePlanner we either add an equivalent admin surface or a minimal Supabase
   dashboard SQL/queue. Decision needed.*

Self-harm/crisis content is special-cased: never publish; surface support
resources to the author instead.

## 7. Premium gating

LifePlanner's entitlement mechanism (confirm which — is it the same Superwall +
`isPlus` used elsewhere, or a separate flag?) gates the **authorship** upgrade,
not privacy:

- **Free:** share individual entries (opt-in), appear in the feed with a byline,
  basic limits (e.g. N shares/week).
- **Premium:** public **Author page** (`author_profiles.handle`), higher/no share
  limits, view counts/analytics, richer formatting, "featured" eligibility.

(If Kamran insists on the "pay to make private" model, gate it there instead —
but see §3.1 risks; do not paywall un-publishing.)

**Decision needed:** confirm LifePlanner's premium/entitlement system to wire the gate.

## 8. Client architecture (KMP)

Follows existing conventions (clean layers under `commonMain`):

- **domain/model**: `CommunityJournal` (read model), `ShareJournalRequest`,
  `ModerationStatus`, `ReportReason`.
- **domain/repository**: `CommunityJournalRepository`
  (`feed(page, category): List<CommunityJournal>`, `detail(id)`,
  `share(request): ShareResult`, `report(id, reason)`, `myShared()`,
  `unpublish(id)`). This is a **network-backed fetch**, not a synced local
  entity — the public feed is remote/paginated, not part of the offline
  soft-delete sync set. (Cache pages in memory / optional Room-less LRU.)
- **data/network**: `CommunityJournalService` (Ktor → Supabase REST/RPC or the
  edge functions), reusing the shared `HttpClient` + Supabase auth token.
- **ui/community**: `CommunityFeedSection` (in Learn hub),
  `CommunityJournalDetailScreen`, `ShareJournalSheet`, `MySharedScreen`,
  `ReportDialog`; ViewModels per screen. Wire a `Screen.CommunityJournal*` route
  + AppNav entries; add a "From the community" block to `LearnHubScreen` and a
  new `FeedKind.COMMUNITY` card in the For You feed.
- Share entry points added to `JournalEntryDetailScreen` / entry overflow.

No SQLDelight schema change is required for v1 (public content is remote-only).
If we later cache shared items offline, follow the synced-entity playbook.

## 9. Privacy, safety, legal

- **Explicit consent** captured and timestamped per share (store on
  `shared_journals`).
- **Right to withdraw**: unpublish is immediate and free; hard-delete on request.
- **Minors**: if the app has/【gets】 age data, block sharing for under-18 (or
  restrict per policy).
- **PII scrub**: moderation flags others' personal data; encourage redaction.
- **Author safety**: pseudonyms allowed; never expose real account identity.
- **Content policy + Terms**: add a short UGC policy the user accepts at first
  share; define takedown SLA.
- **Discoverability of `anon` web feed** is a separate decision (SEO upside vs.
  exposure) — default v1 to **authenticated-only** reads.

## 10. Metrics
Share rate, publish→approve rate, reads/shared item, report rate, unpublish rate,
community-feed → goal/habit creation conversion, premium upgrades attributed to
Author page.

## 11. Rollout
- **v0 (internal):** share flow + moderation + feed behind a FeatureFlag,
  internal accounts only, manual review of everything.
- **v1 (opt-in public):** auto-moderation live, reports live, minimal admin queue.
- **v1.5:** premium Author page, categories/filters, "featured".
- **v2:** light social (save, react), web-readable pages / SEO.

## 12. Open questions (need answers to build)
1. §3.1: opt-in-per-entry (recommended) vs default-public-with-consent?
2. §7: which premium/entitlement system does LifePlanner use?
3. §6: admin/moderation surface — build a LifePlanner admin panel, or start with
   a Supabase-dashboard queue?
4. Reads: authenticated-only, or public web feed (SEO) too?
5. Author identity: profile display name only, or allow free pseudonyms + premium handles?

## 13. Rough effort
- Backend (tables, RLS, `share-journal`/`report-content`/moderation via ai-proxy,
  admin queue): ~1–1.5 wk.
- Client (feed, detail, share sheet, my-shared, report, Learn/For-You wiring): ~1.5–2 wk.
- Polish, policy/consent copy, review tooling, QA: ~0.5–1 wk.
Total ≈ **3–4.5 weeks** for a safe v1. (The moderation/safety layer is the
non-negotiable core — most of the cost is there, not in the UI.)
