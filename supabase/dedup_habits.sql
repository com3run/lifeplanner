-- One-time habit de-duplication (server-side, all users).
-- Run in the Supabase SQL editor (project rkdggdfabwgukspylybu).
-- Soft-delete based, so clients pull the removals via normal sync (is_deleted + bumped
-- updated_at/sync_version). Keeps ONE habit per normalized title per user (the most-completed,
-- then oldest) and soft-deletes the duplicates plus their check-ins.
--
-- Matching is on a normalized title: lowercased, punctuation stripped, whitespace collapsed.
-- So "Drink Water", "drink  water", "Drink water!" merge; genuinely different wording
-- (e.g. "Track Daily Spending" vs "Daily Spending Tracker") is left alone on purpose.

-- ── 0. PREVIEW FIRST (no writes): what would be removed ───────────────────────────
WITH ranked AS (
    SELECT
        id, user_id, title, total_completions, created_at,
        btrim(regexp_replace(regexp_replace(lower(title), '[^a-z0-9 ]', '', 'g'), '\s+', ' ', 'g')) AS norm_title,
        ROW_NUMBER() OVER (
            PARTITION BY user_id,
                btrim(regexp_replace(regexp_replace(lower(title), '[^a-z0-9 ]', '', 'g'), '\s+', ' ', 'g'))
            ORDER BY total_completions DESC, created_at ASC, id ASC
        ) AS rn
    FROM habits
    WHERE is_deleted = FALSE
)
SELECT user_id, norm_title, title AS would_soft_delete, total_completions
FROM ranked
WHERE rn > 1
ORDER BY user_id, norm_title;

-- ── 1. Soft-delete the duplicate habits' check-ins (do this BEFORE the habits) ────
WITH ranked AS (
    SELECT id,
        ROW_NUMBER() OVER (
            PARTITION BY user_id,
                btrim(regexp_replace(regexp_replace(lower(title), '[^a-z0-9 ]', '', 'g'), '\s+', ' ', 'g'))
            ORDER BY total_completions DESC, created_at ASC, id ASC
        ) AS rn
    FROM habits
    WHERE is_deleted = FALSE
),
losers AS (SELECT id FROM ranked WHERE rn > 1)
-- The BEFORE UPDATE trg_habit_check_ins_sync trigger bumps updated_at + sync_version for us.
UPDATE habit_check_ins ci
SET is_deleted = TRUE
FROM losers l
WHERE ci.habit_id = l.id AND ci.is_deleted = FALSE;

-- ── 2. Soft-delete the duplicate habits ──────────────────────────────────────────
WITH ranked AS (
    SELECT id,
        ROW_NUMBER() OVER (
            PARTITION BY user_id,
                btrim(regexp_replace(regexp_replace(lower(title), '[^a-z0-9 ]', '', 'g'), '\s+', ' ', 'g'))
            ORDER BY total_completions DESC, created_at ASC, id ASC
        ) AS rn
    FROM habits
    WHERE is_deleted = FALSE
),
losers AS (SELECT id FROM ranked WHERE rn > 1)
-- The BEFORE UPDATE trg_habits_sync trigger bumps updated_at + sync_version for us.
UPDATE habits h
SET is_deleted = TRUE
FROM losers l
WHERE h.id = l.id;
