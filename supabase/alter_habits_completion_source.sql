-- Schema v38 companion: habits.completion_source
--
-- Which in-app session completes a habit: MANUAL (default), FOCUS, or BREATHING.
-- The client sends this column on every habit push, so it must exist remotely before a build
-- carrying schema v38 ships, or habit sync will fail on an unknown column.
--
-- This repo tracks no supabase/migrations/, so `supabase db push` refuses on history mismatch.
-- Paste this into the Supabase dashboard SQL editor (project rkdggdfabwgukspylybu).
-- Idempotent: safe to run more than once.

ALTER TABLE habits ADD COLUMN IF NOT EXISTS completion_source TEXT;
