// Supabase Edge Function: store-watch
//
// Polls the App Store and Play Store for the live version of LifePlanner and
// posts a Telegram alert whenever the published version changes.
//
// State is kept in public.store_versions (see supabase/monitoring/store-watch.sql).
// On the very first run for a platform it records a baseline and does NOT alert.
//
// Secrets:
//   TELEGRAM_BOT_TOKEN / TELEGRAM_CHAT_ID  - set via `supabase secrets set`
//   SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY - injected automatically by Supabase
//
// Endpoints:
//   GET|POST /store-watch          -> check both stores, alert on change
//   GET|POST /store-watch?peek=1   -> report current versions, never alert or write

const BUNDLE_ID = "az.tribe.lifeplanner";
const APP_STORE_ID = "6745726864";

const TG_TOKEN = Deno.env.get("TELEGRAM_BOT_TOKEN");
const TG_CHAT = Deno.env.get("TELEGRAM_CHAT_ID");
const SB_URL = Deno.env.get("SUPABASE_URL");
const SB_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

type Platform = "ios" | "android";

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body, null, 2), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

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

// --- store lookups ---------------------------------------------------------

// App Store: official iTunes lookup API, returns clean JSON.
async function fetchAppStoreVersion(): Promise<string | null> {
  try {
    const res = await fetch(
      `https://itunes.apple.com/lookup?bundleId=${BUNDLE_ID}&t=${Date.now()}`,
      { signal: AbortSignal.timeout(15_000) },
    );
    if (!res.ok) return null;
    const data = await res.json();
    return data?.results?.[0]?.version ?? null;
  } catch (_e) {
    return null;
  }
}

// Play Store: no public API, so scrape the listing. Best-effort by design --
// if Google changes their markup this returns null and we simply skip the
// android check rather than firing a bogus alert.
async function fetchPlayStoreVersion(): Promise<string | null> {
  try {
    const res = await fetch(
      `https://play.google.com/store/apps/details?id=${BUNDLE_ID}&hl=en&gl=US`,
      {
        headers: { "User-Agent": "Mozilla/5.0 (compatible; LifePlannerBot/1.0)" },
        signal: AbortSignal.timeout(15_000),
      },
    );
    if (!res.ok) return null;
    const html = await res.text();
    // Prefer the anchored form: Play embeds the version under key "141".
    // Fall back to the bare pattern only if that key moves.
    for (
      const re of [
        /"141":\s*\[\[\["(\d+\.\d+(?:\.\d+)*)"\]\]/,
        /\[\[\["(\d+\.\d+(?:\.\d+)*)"\]\]/,
      ]
    ) {
      const m = html.match(re);
      if (m?.[1]) return m[1];
    }
    return null;
  } catch (_e) {
    return null;
  }
}

// --- state -----------------------------------------------------------------

async function readStored(platform: Platform): Promise<string | null> {
  if (!SB_URL || !SB_KEY) return null;
  const res = await fetch(
    `${SB_URL}/rest/v1/store_versions?platform=eq.${platform}&select=version`,
    { headers: { apikey: SB_KEY, Authorization: `Bearer ${SB_KEY}` } },
  );
  if (!res.ok) return null;
  const rows = await res.json();
  return rows?.[0]?.version ?? null;
}

async function writeStored(platform: Platform, version: string): Promise<void> {
  if (!SB_URL || !SB_KEY) return;
  await fetch(`${SB_URL}/rest/v1/store_versions`, {
    method: "POST",
    headers: {
      apikey: SB_KEY,
      Authorization: `Bearer ${SB_KEY}`,
      "Content-Type": "application/json",
      Prefer: "resolution=merge-duplicates",
    },
    body: JSON.stringify({
      platform,
      version,
      checked_at: new Date().toISOString(),
    }),
  });
}

// --- main ------------------------------------------------------------------

Deno.serve(async (req) => {
  const url = new URL(req.url);
  const peek = url.searchParams.get("peek") === "1";

  const [iosVersion, androidVersion] = await Promise.all([
    fetchAppStoreVersion(),
    fetchPlayStoreVersion(),
  ]);

  const live: Record<Platform, string | null> = {
    ios: iosVersion,
    android: androidVersion,
  };

  if (peek) {
    return json({
      mode: "peek",
      live,
      stored: {
        ios: await readStored("ios"),
        android: await readStored("android"),
      },
    });
  }

  const labels: Record<Platform, string> = {
    ios: "App Store",
    android: "Play Store",
  };

  const changes: unknown[] = [];

  for (const platform of ["ios", "android"] as Platform[]) {
    const current = live[platform];
    if (!current) continue; // lookup failed or app not published: skip quietly

    const previous = await readStored(platform);

    if (previous === null) {
      // First sighting: record a baseline, stay quiet.
      await writeStored(platform, current);
      changes.push({ platform, action: "baseline", version: current });
      continue;
    }

    if (previous !== current) {
      await writeStored(platform, current);
      changes.push({ platform, action: "changed", from: previous, to: current });
      await sendTelegram(
        `🚀 <b>LifePlanner is live on the ${labels[platform]}</b>\n` +
          `<b>v${current}</b> (was v${previous})`,
      );
    }
  }

  return json({ live, changes, checked: new Date().toISOString() });
});
