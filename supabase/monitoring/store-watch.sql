-- LifePlanner store version watcher (Supabase-native)
--
-- Creates the state table for the `store-watch` edge function and schedules it
-- every 30 minutes. The function alerts Telegram when the published App Store
-- or Play Store version changes.
--
-- HOW TO APPLY: paste this whole file into the Supabase SQL Editor and run it
--   (Dashboard -> SQL Editor -> New query -> Run).
-- It is idempotent -- safe to re-run.

-- 1. Extensions ------------------------------------------------------------
create extension if not exists pg_cron;
create extension if not exists pg_net;

-- 2. State table -----------------------------------------------------------
create table if not exists public.store_versions (
  platform   text primary key,          -- 'ios' | 'android'
  version    text not null,
  checked_at timestamptz not null default now()
);

-- Locked down: only the service_role (used by the edge function) touches this.
alter table public.store_versions enable row level security;

-- 3. Remove any previous job with this name (idempotent re-runs) ------------
select cron.unschedule('lifeplanner-store-watch')
where exists (select 1 from cron.job where jobname = 'lifeplanner-store-watch');

-- 4. Schedule: check the stores every 30 minutes ---------------------------
select cron.schedule(
  'lifeplanner-store-watch',
  '*/30 * * * *',
  $$
  select net.http_post(
    url     := 'https://rkdggdfabwgukspylybu.supabase.co/functions/v1/store-watch',
    headers := '{"Content-Type": "application/json"}'::jsonb,
    body    := '{}'::jsonb,
    timeout_milliseconds := 25000
  );
  $$
);

-- 5. Inspect ---------------------------------------------------------------
--   select * from public.store_versions;
--   select jobid, jobname, schedule, active from cron.job;
--
-- To stop watching:
--   select cron.unschedule('lifeplanner-store-watch');
