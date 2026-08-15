// report-content (Community Journals v0)
// Files a report against a shared journal and auto-hides it once enough distinct users report it.
import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

const AUTO_HIDE_THRESHOLD = 3; // distinct reporters before auto-removal pending review
const SEVERE_REASONS = ["sexual", "violence", "self_harm", "harassment"];

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};
function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { ...cors, "Content-Type": "application/json" } });
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "method-not-allowed" }, 405);

  const jwt = (req.headers.get("Authorization") ?? "").replace("Bearer ", "");
  if (!jwt) return json({ error: "unauthorized" }, 401);

  const userClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: `Bearer ${jwt}` } },
  });
  const { data: userData, error: userErr } = await userClient.auth.getUser();
  if (userErr || !userData?.user) return json({ error: "unauthorized" }, 401);
  const reporterId = userData.user.id;

  let payload: { sharedId: string; reason: string; detail?: string };
  try {
    payload = await req.json();
  } catch {
    return json({ error: "bad-request" }, 400);
  }
  const sharedId = (payload?.sharedId ?? "").trim();
  const reason = (payload?.reason ?? "").trim();
  if (!sharedId || !reason) return json({ error: "missing-fields" }, 400);

  const admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY);

  // Insert the report (idempotent on (shared_id, reporter_id) via the unique constraint).
  const { error: insErr } = await admin.from("content_reports").insert({
    id: crypto.randomUUID(),
    shared_id: sharedId,
    reporter_id: reporterId,
    reason,
    detail: payload.detail ?? null,
  });
  // Duplicate report is fine — treat as success.
  if (insErr && !insErr.message.includes("duplicate")) {
    return json({ error: "insert-failed", detail: insErr.message }, 500);
  }

  // Recount distinct reporters and auto-hide if warranted.
  const { count } = await admin
    .from("content_reports")
    .select("*", { count: "exact", head: true })
    .eq("shared_id", sharedId);
  const reports = count ?? 0;
  const shouldHide = reports >= AUTO_HIDE_THRESHOLD || SEVERE_REASONS.includes(reason);

  const update: Record<string, unknown> = { report_count: reports };
  if (shouldHide) update.status = "removed";
  await admin.from("shared_journals").update(update).eq("id", sharedId);

  return json({ ok: true, reports, hidden: shouldHide }, 200);
});
