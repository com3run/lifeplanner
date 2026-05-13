-- ────────────────────────────────────────────────────────────────────────────
-- personas_cache
-- Global public cache of TribeBot personas synced via webhook.
-- No user_id — one row per persona, shared by all LifePlanner users.
-- Write: service role only (via persona-sync-webhook Edge Function).
-- Read:  public (anon key) so the app can fetch without auth.
-- ────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS personas_cache (
    id                   TEXT        PRIMARY KEY,
    name                 TEXT        NOT NULL,
    title                TEXT        NOT NULL DEFAULT 'Coach',
    description          TEXT,
    style                TEXT,
    image_url            TEXT,
    category             TEXT        NOT NULL DEFAULT 'WELLBEING',
    emoji                TEXT        NOT NULL DEFAULT '✨',
    greeting             TEXT,
    specialties          JSONB       NOT NULL DEFAULT '[]',
    personality          TEXT,
    avatar_bg_color      TEXT        NOT NULL DEFAULT '#6366F1',
    avatar_accent_color  TEXT        NOT NULL DEFAULT '#818CF8',
    avatar_icon_name     TEXT        NOT NULL DEFAULT 'star',
    bio                  TEXT,
    fun_fact             TEXT,
    xp_to_unlock         INTEGER     NOT NULL DEFAULT 0,
    is_default_unlocked  BOOLEAN     NOT NULL DEFAULT TRUE,
    timezone             TEXT,
    city                 TEXT,
    country_flag         TEXT,
    is_active            BOOLEAN     NOT NULL DEFAULT TRUE,
    source               TEXT        NOT NULL DEFAULT 'tribebot',
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Public read — all LifePlanner users share the same cache
ALTER TABLE personas_cache ENABLE ROW LEVEL SECURITY;
CREATE POLICY personas_cache_public_read ON personas_cache
    FOR SELECT TO anon, authenticated USING (TRUE);

-- Write is restricted to service role (webhook function uses service role key)

-- Index for fast active-only queries
CREATE INDEX IF NOT EXISTS idx_personas_cache_active ON personas_cache (is_active);
CREATE INDEX IF NOT EXISTS idx_personas_cache_updated ON personas_cache (updated_at DESC);
