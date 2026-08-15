// Supabase Edge Function: health
//
// Pings every LifePlanner edge function, returns a JSON status summary, and
// posts a Telegram alert whenever one or more functions look unhealthy.
//
// A function is considered ALIVE if it answers with any HTTP status < 500 that
// is not 404. Protected functions returning 401/400 are healthy on purpose --
// the point of this check is "is the function deployed and responding", not
// "does an unauthenticated call succeed".
//
// Secrets (set via `supabase secrets set`):
//   TELEGRAM_BOT_TOKEN   - bot token from @BotFather
//   TELEGRAM_CHAT_ID     - chat id to send alerts to
//   HEALTH_KEY           - (optional) if set, callers must pass ?key=<value>
//
// Endpoints:
//   GET|POST /health            -> run all checks, alert on failure
//   GET|POST /health?test=1     -> send a one-off Telegram test message

const PROJECT = "rkdggdfabwgukspylybu";
const BASE = `https://${PROJECT}.supabase.co/functions/v1`;

// Functions to monitor (health itself is intentionally excluded).
const FUNCTIONS = [
  "auth-redirect",
  "resend-verification",
  "ai-proxy",
  "mcp-server",
  "persona-sync-webhook",
];

const TG_TOKEN = Deno.env.get("TELEGRAM_BOT_TOKEN");
const TG_CHAT = Deno.env.get("TELEGRAM_CHAT_ID");
const HEALTH_KEY = Deno.env.get("HEALTH_KEY");

async function sendTelegram(text: string): Promise<boolean> {
  if (!TG_TOKEN || !TG_CHAT) return false;
  try {
    const res = await fetch(
      `https://api.telegram.org/bot${TG_TOKEN}/sendMessage`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          chat_id: TG_CHAT,
          text,
          parse_mode: "HTML",
          disable_web_page_preview: true,
        }),
      },
    );
    return res.ok;
  } catch (_e) {
    return false;
  }
}

const TIMEOUT_MS = 15_000;
const RETRY_DELAY_MS = 3_000;

async function attempt(name: string) {
  const started = Date.now();
  try {
    const res = await fetch(`${BASE}/${name}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: "{}",
      signal: AbortSignal.timeout(TIMEOUT_MS),
    });
    return {
      name,
      status: res.status,
      ms: Date.now() - started,
      healthy: res.status < 500 && res.status !== 404,
    };
  } catch (e) {
    return {
      name,
      status: 0,
      ms: Date.now() - started,
      healthy: false,
      error: String(e),
    };
  }
}

// A single blip is not an outage. Cold starts on the edge runtime can blow past
// the timeout occasionally, and alerting on one failed request just trains you
// to ignore the alerts. Only report a function down when two consecutive
// attempts fail.
async function probe(name: string) {
  const first = await attempt(name);
  if (first.healthy) return { ...first, attempts: 1 };

  await new Promise((r) => setTimeout(r, RETRY_DELAY_MS));
  const second = await attempt(name);

  return second.healthy
    ? { ...second, attempts: 2, recoveredAfterRetry: true }
    : { ...second, attempts: 2, firstError: first.error ?? first.status };
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body, null, 2), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

Deno.serve(async (req) => {
  const url = new URL(req.url);

  // Optional shared-secret gate.
  if (HEALTH_KEY && url.searchParams.get("key") !== HEALTH_KEY) {
    return json({ error: "unauthorized" }, 401);
  }

  // Manual Telegram wiring test: /health?test=1
  if (url.searchParams.get("test") === "1") {
    const configured = Boolean(TG_TOKEN && TG_CHAT);
    const sent = await sendTelegram(
      "✅ LifePlanner health monitor: Telegram alerts are wired up correctly.",
    );
    return json({ telegram_configured: configured, test_sent: sent });
  }

  // Force a sample outage alert (to preview what a real alert looks like):
  // /health?force=1
  if (url.searchParams.get("force") === "1") {
    const sent = await sendTelegram(
      `🚨 <b>LifePlanner backend alert</b>\n` +
        `1 function(s) unhealthy:\n` +
        `• <b>example-function</b> → 503\n\n` +
        `<i>(This is a forced test alert — nothing is actually down.)</i>`,
    );
    return json({ forced_alert_sent: sent });
  }

  const results = await Promise.all(FUNCTIONS.map(probe));
  const down = results.filter((r) => !r.healthy);
  const overall = down.length === 0 ? "ok" : "degraded";

  if (down.length > 0) {
    const lines = down.map(
      (r) =>
        `• <b>${r.name}</b> → ${r.status || "no response"}` +
        (r.error ? ` (${r.error})` : ""),
    );
    await sendTelegram(
      `🚨 <b>LifePlanner backend alert</b>\n` +
        `${down.length} function(s) unhealthy ` +
        `(failed 2 consecutive attempts):\n${lines.join("\n")}`,
    );
  }

  return json({ overall, checked: results.length, down: down.length, results });
});
