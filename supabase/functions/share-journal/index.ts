// share-journal (Community Journals v0)
// Publishes a journal entry into the public `shared_journals` store after moderation.
// v0 posture: everything lands as `pending` for manual review; clearly-unsafe content is rejected
// up front. No direct client INSERT to shared_journals (RLS blocks it) — this function is the gate.
import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const OPENAI_API_KEY = Deno.env.get("OPENAI_API_KEY") ?? "";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

interface ShareRequest {
  sourceEntryId?: string;
  authorName: string;
  authorAvatarUrl?: string;
  title: string;
  body: string;
  mood?: string;
  goalCategory?: string;
  goalTitle?: string;
  isAiAssisted?: boolean;
  consent: boolean;
}

function json(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors, "Content-Type": "application/json" },
  });
}

function readMinutes(text: string): number {
  const words = text.trim().split(/\s+/).length;
  return Math.max(1, Math.round(words / 200));
}

// Flag other people's PII (emails, phone numbers) so we can hold for redaction.
function looksLikePII(text: string): boolean {
  const email = /[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}/i;
  const phone = /(\+?\d[\d\s().-]{7,}\d)/;
  return email.test(text) || phone.test(text);
}

// OpenAI moderation. Returns { blocked, categories }. Fails open to "hold for review", never auto-approves.
async function moderate(text: string): Promise<{ blocked: boolean; note: string }> {
  if (!OPENAI_API_KEY) return { blocked: false, note: "moderation-unavailable" };
  try {
    const res = await fetch("https://api.openai.com/v1/moderations", {
      method: "POST",
      headers: { "Authorization": `Bearer ${OPENAI_API_KEY}`, "Content-Type": "application/json" },
      body: JSON.stringify({ model: "omni-moderation-latest", input: text }),
    });
    if (!res.ok) return { blocked: false, note: `moderation-http-${res.status}` };
    const data = await res.json();
    const result = data?.results?.[0];
    if (!result) return { blocked: false, note: "moderation-empty" };
    const cats = result.categories ?? {};
    // Hard-block categories that must never be published.
    const hardBlock = ["sexual/minors", "self-harm", "self-harm/intent", "self-harm/instructions",
      "violence/graphic", "hate/threatening", "harassment/threatening"];
    const flagged = Object.keys(cats).filter((k) => cats[k] === true);
    const blocked = flagged.some((k) => hardBlock.includes(k)) || result.flagged === true && flagged.some((k) => hardBlock.includes(k));
    return { blocked: flagged.some((k) => hardBlock.includes(k)), note: flagged.length ? `flagged:${flagged.join(",")}` : "clean" };
  } catch (e) {
    return { blocked: false, note: `moderation-error:${(e as Error).message}` };
  }
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });
  if (req.method !== "POST") return json({ error: "method-not-allowed" }, 405);

  const jwt = (req.headers.get("Authorization") ?? "").replace("Bearer ", "");
  if (!jwt) return json({ error: "unauthorized" }, 401);

  // Identify the caller.
  const userClient = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: `Bearer ${jwt}` } },
  });
  const { data: userData, error: userErr } = await userClient.auth.getUser();
  if (userErr || !userData?.user) return json({ error: "unauthorized" }, 401);
  const userId = userData.user.id;

  let payload: ShareRequest;
  try {
    payload = await req.json();
  } catch {
    return json({ error: "bad-request" }, 400);
  }
  if (!payload?.consent) return json({ error: "consent-required" }, 400);
  const title = (payload.title ?? "").trim();
  const body = (payload.body ?? "").trim();
  const authorName = (payload.authorName ?? "").trim();
  if (!title || !body || !authorName) return json({ error: "missing-fields" }, 400);

  // Moderation pre-screen.
  const mod = await moderate(`${title}\n\n${body}`);
  if (mod.blocked) {
    return json({ status: "rejected", reason: mod.note }, 200);
  }
  const pii = looksLikePII(body) ? "possible-pii" : "";
  const note = [mod.note, pii].filter(Boolean).join("; ");

  // Insert as pending (manual review in v0) with the service role, bypassing the no-insert RLS.
  const admin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY);
  const id = crypto.randomUUID();
  const { error: insErr } = await admin.from("shared_journals").insert({
    id,
    source_entry_id: payload.sourceEntryId ?? null,
    author_id: userId,
    author_name: authorName,
    author_avatar_url: payload.authorAvatarUrl ?? null,
    title,
    body,
    mood: payload.mood ?? null,
    goal_category: payload.goalCategory ?? null,
    goal_title: payload.goalTitle ?? null,
    is_ai_assisted: payload.isAiAssisted ?? false,
    read_minutes: readMinutes(body),
    status: "pending",
    moderation_note: note || "clean",
  });
  if (insErr) return json({ error: "insert-failed", detail: insErr.message }, 500);

  return json({ status: "pending", id }, 200);
});
