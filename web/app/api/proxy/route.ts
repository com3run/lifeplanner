import { NextRequest, NextResponse } from "next/server";

export async function POST(req: NextRequest) {
  let payload: {
    path: string;
    method?: string;
    headers?: Record<string, string>;
    body?: unknown;
    authType: "user-jwt" | "service-role" | "none";
    jwt?: string;
  };

  try {
    payload = await req.json();
  } catch {
    return NextResponse.json({ error: "Invalid JSON payload" }, { status: 400 });
  }

  const { path, method = "GET", headers: reqHeaders = {}, body, authType, jwt } = payload;

  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
  if (!supabaseUrl) {
    return NextResponse.json({ error: "NEXT_PUBLIC_SUPABASE_URL not configured" }, { status: 500 });
  }

  const targetUrl = path.startsWith("http") ? path : `${supabaseUrl}${path}`;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    "apikey": process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ?? "",
    ...reqHeaders,
  };

  if (authType === "service-role") {
    const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
    if (!serviceKey) {
      return NextResponse.json(
        { error: "SUPABASE_SERVICE_ROLE_KEY is not set in environment variables" },
        { status: 400 },
      );
    }
    headers["Authorization"] = `Bearer ${serviceKey}`;
  } else if (authType === "user-jwt") {
    if (!jwt) {
      return NextResponse.json(
        { error: "JWT token is required for user-jwt auth. Paste it in the Auth tab." },
        { status: 400 },
      );
    }
    headers["Authorization"] = `Bearer ${jwt}`;
  }

  const isBodyMethod = ["POST", "PUT", "PATCH"].includes(method.toUpperCase());

  const fetchOptions: RequestInit = {
    method: method.toUpperCase(),
    headers,
    ...(isBodyMethod && body !== undefined && body !== null
      ? { body: typeof body === "string" ? body : JSON.stringify(body) }
      : {}),
  };

  const startTime = Date.now();

  let upstreamRes: Response;
  try {
    upstreamRes = await fetch(targetUrl, fetchOptions);
  } catch (err) {
    return NextResponse.json(
      { error: `Failed to reach ${targetUrl}: ${err instanceof Error ? err.message : String(err)}` },
      { status: 502 },
    );
  }

  const elapsed = Date.now() - startTime;
  const contentType = upstreamRes.headers.get("content-type") ?? "";

  if (contentType.includes("text/event-stream")) {
    const responseHeaders: Record<string, string> = {
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache",
      "X-Accel-Buffering": "no",
      "X-Response-Status": upstreamRes.status.toString(),
      "X-Response-Time": elapsed.toString(),
    };
    return new Response(upstreamRes.body, { headers: responseHeaders });
  }

  const responseBody = await upstreamRes.text();
  const responseHeaders: Record<string, string> = {};
  upstreamRes.headers.forEach((value, key) => {
    responseHeaders[key] = value;
  });

  return NextResponse.json({
    status: upstreamRes.status,
    statusText: upstreamRes.statusText,
    headers: responseHeaders,
    body: responseBody,
    time: elapsed,
    size: responseBody.length,
  });
}
