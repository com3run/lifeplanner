-- LifePlanner backend uptime monitor (Supabase-native)
--
-- Runs the `health` edge function every 5 minutes via pg_cron + pg_net.
-- The health function pings all edge functions and posts a Telegram alert
-- when any of them is unhealthy.
--
-- HOW TO APPLY: paste this whole file into the Supabase SQL Editor and run it
--   (Dashboard -> SQL Editor -> New query -> Run).
-- It is idempotent -- safe to run again to change the schedule.

-- 1. Extensions ------------------------------------------------------------
create extension if not exists pg_cron;
create extension if not exists pg_net;

-- 2. Remove any previous job with this name (idempotent re-runs) ------------
select cron.unschedule('lifeplanner-health')
where exists (select 1 from cron.job where jobname = 'lifeplanner-health');

-- 3. Schedule: hit the health function every 5 minutes ---------------------
select cron.schedule(
  'lifeplanner-health',
  '*/5 * * * *',
  $$
  select net.http_post(
    url     := 'https://rkdggdfabwgukspylybu.supabase.co/functions/v1/health',
    headers := '{"Content-Type": "application/json"}'::jsonb,
    body    := '{}'::jsonb,
    timeout_milliseconds := 20000
  );
  $$
);

-- 4. Inspect ---------------------------------------------------------------
--   select jobid, jobname, schedule, active from cron.job;
--   select * from cron.job_run_details
--     where jobid = (select jobid from cron.job where jobname = 'lifeplanner-health')
--     order by start_time desc limit 10;
--
-- To stop monitoring:
--   select cron.unschedule('lifeplanner-health');
