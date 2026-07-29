-- Community Journals (v0) — public shared-journal store + reports.
-- Apply in the Supabase SQL editor (project rkdggdfabwgukspylybu). Idempotent-ish (IF NOT EXISTS).
-- See docs/SPEC-community-journals.md. Decoupled from the private, owner-only journal_entries table.

-- ── 1. Public snapshot of a shared journal (moderated, denormalized) ─────────────
CREATE TABLE IF NOT EXISTS shared_journals (
    id                TEXT PRIMARY KEY,
    source_entry_id   TEXT,                                   -- private entry it came from (not exposed to readers)
    author_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    author_name       TEXT NOT NULL,                          -- display/pseudonym chosen at share time
    author_avatar_url TEXT,
    title             TEXT NOT NULL,
    body              TEXT NOT NULL,                          -- moderated snapshot (not live-linked to the private entry)
    mood              TEXT,
    goal_category     TEXT,
    goal_title        TEXT,
    is_ai_assisted    BOOLEAN NOT NULL DEFAULT FALSE,
    read_minutes      INT     NOT NULL DEFAULT 1,
    status            TEXT    NOT NULL DEFAULT 'pending'
                       CHECK (status IN ('pending','approved','rejected','removed')),
    moderation_note   TEXT,
    report_count      INT     NOT NULL DEFAULT 0,
    consent_at        TIMESTAMPTZ NOT NULL DEFAULT now(),     -- explicit per-share consent timestamp
    published_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_shared_status_pub ON shared_journals(status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_shared_author     ON shared_journals(author_id);
CREATE INDEX IF NOT EXISTS idx_shared_category   ON shared_journals(status, goal_category, published_at DESC);

-- ── 2. Reports ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS content_reports (
    id           TEXT PRIMARY KEY,
    shared_id    TEXT NOT NULL REFERENCES shared_journals(id) ON DELETE CASCADE,
    reporter_id  UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reason       TEXT NOT NULL,
    detail       TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (shared_id, reporter_id)                           -- one report per user per item
);

-- ── 3. RLS ───────────────────────────────────────────────────────────────────────
ALTER TABLE shared_journals ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_reports ENABLE ROW LEVEL SECURITY;

-- Readers see only APPROVED items; authors additionally see their own (any status).
DROP POLICY IF EXISTS shared_journals_read ON shared_journals;
CREATE POLICY shared_journals_read ON shared_journals
    FOR SELECT TO authenticated
    USING (status = 'approved' OR author_id = (select auth.uid()));

-- No direct client INSERT: publishing goes through the share-journal edge function
-- (service_role), which forces moderation. Authors may unpublish/delete their own row.
DROP POLICY IF EXISTS shared_journals_update_own ON shared_journals;
CREATE POLICY shared_journals_update_own ON shared_journals
    FOR UPDATE TO authenticated
    USING (author_id = (select auth.uid())) WITH CHECK (author_id = (select auth.uid()));
DROP POLICY IF EXISTS shared_journals_delete_own ON shared_journals;
CREATE POLICY shared_journals_delete_own ON shared_journals
    FOR DELETE TO authenticated
    USING (author_id = (select auth.uid()));

-- Reports: any authenticated user may file one for themselves; clients cannot read reports.
DROP POLICY IF EXISTS content_reports_insert ON content_reports;
CREATE POLICY content_reports_insert ON content_reports
    FOR INSERT TO authenticated
    WITH CHECK (reporter_id = (select auth.uid()));

-- ── 4. updated_at trigger (reuse existing helper if present) ─────────────────────
DROP TRIGGER IF EXISTS trg_shared_journals_touch ON shared_journals;
CREATE TRIGGER trg_shared_journals_touch BEFORE UPDATE ON shared_journals
    FOR EACH ROW EXECUTE FUNCTION update_sync_metadata();
