import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "jsr:@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
const WEBHOOK_SECRET = Deno.env.get("PERSONA_WEBHOOK_SECRET") ?? "";

interface PersonaPayload {
  id: string;
  name: string;
  title?: string;
  description?: string;
  style?: string;
  image_url?: string;
  category?: string;
  emoji?: string;
  greeting?: string;
  specialties?: string[];
  personality?: string;
  avatar_bg_color?: string;
  avatar_accent_color?: string;
  avatar_icon_name?: string;
  bio?: string;
  fun_fact?: string;
  xp_to_unlock?: number;
  is_default_unlocked?: boolean;
  timezone?: string;
  city?: string;
  country_flag?: string;
  is_active?: boolean;
  is_public?: boolean;
}

interface WebhookBody {
  action: "upsert" | "delete";
  persona?: PersonaPayload;
  personas?: PersonaPayload[];
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response(null, {
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "x-webhook-secret, content-type",
      },
    });
  }

  // Validate shared secret
  const secret = req.headers.get("x-webhook-secret");
  if (!WEBHOOK_SECRET || secret !== WEBHOOK_SECRET) {
    return new Response(JSON.stringify({ error: "Unauthorized" }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  }

  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
      headers: { "Content-Type": "application/json" },
    });
  }

  let body: WebhookBody;
  try {
    body = await req.json();
  } catch {
    return new Response(JSON.stringify({ error: "Invalid JSON" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  }

  const supabase = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY);
  const now = new Date().toISOString();

  const payloads: PersonaPayload[] = body.personas ?? (body.persona ? [body.persona] : []);
  if (payloads.length === 0) {
    return new Response(JSON.stringify({ error: "No persona data provided" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  }

  if (body.action === "delete") {
    const ids = payloads.map((p) => p.id);
    const { error } = await supabase
      .from("personas_cache")
      .update({ is_active: false, updated_at: now })
      .in("id", ids);
    if (error) {
      console.error("Delete error:", error);
      return new Response(JSON.stringify({ error: error.message }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }
    return new Response(JSON.stringify({ ok: true, deactivated: ids.length }), {
      headers: { "Content-Type": "application/json" },
    });
  }

  // upsert
  const rows = payloads.map((p) => ({
    id: p.id,
    name: p.name,
    title: p.title ?? "Coach",
    description: p.description ?? null,
    style: p.style ?? null,
    image_url: p.image_url ?? null,
    category: p.category ?? "WELLBEING",
    emoji: p.emoji ?? "✨",
    greeting: p.greeting ?? `Hi! I'm ${p.name}. How can I help you today?`,
    specialties: p.specialties ?? [],
    personality: p.personality ?? p.description ?? null,
    avatar_bg_color: p.avatar_bg_color ?? "#6366F1",
    avatar_accent_color: p.avatar_accent_color ?? "#818CF8",
    avatar_icon_name: p.avatar_icon_name ?? "star",
    bio: p.bio ?? p.description ?? null,
    fun_fact: p.fun_fact ?? null,
    xp_to_unlock: p.xp_to_unlock ?? 0,
    is_default_unlocked: p.is_default_unlocked ?? true,
    timezone: p.timezone ?? null,
    city: p.city ?? null,
    country_flag: p.country_flag ?? null,
    is_active: p.is_active !== false && p.is_public !== false,
    source: "tribebot",
    updated_at: now,
  }));

  const { error } = await supabase.from("personas_cache").upsert(rows, {
    onConflict: "id",
  });

  if (error) {
    console.error("Upsert error:", error);
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }

  return new Response(
    JSON.stringify({ ok: true, upserted: rows.length }),
    { headers: { "Content-Type": "application/json" } },
  );
});
