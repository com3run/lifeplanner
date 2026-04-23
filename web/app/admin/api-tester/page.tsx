"use client";

import { useState, useEffect } from "react";

type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";
type AuthType = "user-jwt" | "service-role" | "none";

interface RequestDef {
  id: string;
  name: string;
  description?: string;
  method: HttpMethod;
  path: string;
  auth: AuthType;
  headers?: Record<string, string>;
  body?: unknown;
}

interface Collection {
  id: string;
  info: { name: string; description: string };
  requests: RequestDef[];
}

interface ResponseState {
  status: number;
  statusText: string;
  body: string;
  time: number;
  size: number;
  streaming?: boolean;
}

const COLLECTION_FILES = [
  { id: "ai-proxy", file: "/collections/ai-proxy.json" },
  { id: "mcp-server", file: "/collections/mcp-server.json" },
  { id: "resend-verification", file: "/collections/resend-verification.json" },
];

const METHOD_COLOR: Record<string, string> = {
  GET: "text-emerald-400",
  POST: "text-blue-400",
  PUT: "text-yellow-400",
  DELETE: "text-red-400",
  PATCH: "text-purple-400",
};

function statusColor(status: number) {
  if (status === 0) return "text-gray-500";
  if (status < 300) return "text-emerald-400";
  if (status < 500) return "text-yellow-400";
  return "text-red-400";
}

function tryFormatJson(text: string): string {
  try {
    return JSON.stringify(JSON.parse(text), null, 2);
  } catch {
    return text;
  }
}

function bytesLabel(n: number) {
  if (n < 1024) return `${n}B`;
  return `${(n / 1024).toFixed(1)}KB`;
}

export default function ApiTesterPage() {
  const [collections, setCollections] = useState<Collection[]>([]);
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  const [selected, setSelected] = useState<RequestDef | null>(null);
  const [editedBody, setEditedBody] = useState("");
  const [jwt, setJwt] = useState("");
  const [authType, setAuthType] = useState<AuthType>("user-jwt");
  const [tab, setTab] = useState<"body" | "auth">("body");
  const [response, setResponse] = useState<ResponseState | null>(null);
  const [streamText, setStreamText] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all(
      COLLECTION_FILES.map(({ id, file }) =>
        fetch(file)
          .then((r) => r.json())
          .then((data) => ({ id, ...data } as Collection))
          .catch(() => null),
      ),
    ).then((results) => {
      const valid = results.filter(Boolean) as Collection[];
      setCollections(valid);
      setExpanded(new Set(valid.map((c) => c.id)));
    });
  }, []);

  function selectRequest(req: RequestDef) {
    setSelected(req);
    setEditedBody(req.body != null ? JSON.stringify(req.body, null, 2) : "");
    setAuthType(req.auth);
    setTab(req.body != null ? "body" : "auth");
    setResponse(null);
    setStreamText("");
    setError(null);
  }

  function toggleCollection(id: string) {
    setExpanded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  async function sendRequest() {
    if (!selected || loading) return;
    setLoading(true);
    setResponse(null);
    setStreamText("");
    setError(null);

    let parsedBody: unknown;
    try {
      parsedBody = editedBody.trim() ? JSON.parse(editedBody) : undefined;
    } catch {
      setError("Request body is not valid JSON.");
      setLoading(false);
      return;
    }

    const isStreaming =
      parsedBody != null &&
      typeof parsedBody === "object" &&
      (parsedBody as Record<string, unknown>).stream === true;

    try {
      const res = await fetch("/api/proxy", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          path: selected.path,
          method: selected.method,
          headers: selected.headers,
          body: parsedBody,
          authType,
          jwt,
        }),
      });

      const sseContentType = res.headers.get("content-type")?.includes("text/event-stream");

      if (sseContentType || isStreaming) {
        const status = parseInt(res.headers.get("x-response-status") ?? "200");
        const startTime = parseInt(res.headers.get("x-response-time") ?? "0");
        setResponse({ status, statusText: "Streaming…", body: "", time: startTime, size: 0, streaming: true });

        const reader = res.body!.getReader();
        const decoder = new TextDecoder();
        let fullText = "";
        let rawSSE = "";

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          const chunk = decoder.decode(value, { stream: true });
          rawSSE += chunk;
          for (const line of chunk.split("\n")) {
            if (!line.startsWith("data: ")) continue;
            const data = line.slice(6).trim();
            if (data === "[DONE]") continue;
            try {
              const parsed = JSON.parse(data);
              if (typeof parsed === "string") fullText += parsed;
              else if (typeof parsed.text === "string") fullText += parsed.text;
            } catch {
              // non-JSON SSE data — ignore
            }
          }
          setStreamText(fullText || rawSSE);
        }

        setResponse((prev) =>
          prev
            ? { ...prev, statusText: "OK", body: rawSSE, size: rawSSE.length, streaming: false }
            : null,
        );
      } else {
        const data = await res.json();
        if (data.error) {
          setError(data.error);
        } else {
          setResponse(data as ResponseState);
        }
      }
    } catch (err) {
      setError(`Network error: ${err instanceof Error ? err.message : String(err)}`);
    } finally {
      setLoading(false);
    }
  }

  const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL ?? "";

  return (
    <div className="flex h-full bg-gray-950 text-gray-100 text-sm overflow-hidden">
      {/* Sidebar */}
      <aside className="w-60 flex-shrink-0 border-r border-gray-800 flex flex-col overflow-hidden">
        <div className="px-4 py-3 border-b border-gray-800 flex-shrink-0">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-widest">Collections</p>
          <p className="text-xs text-gray-600 mt-0.5 truncate font-mono">{supabaseUrl.replace("https://", "")}</p>
        </div>
        <nav className="flex-1 overflow-y-auto py-2">
          {collections.map((col) => (
            <div key={col.id}>
              <button
                onClick={() => toggleCollection(col.id)}
                className="w-full flex items-center gap-2 px-4 py-2 hover:bg-gray-900 text-left group"
              >
                <span className="text-gray-600 text-xs w-3">{expanded.has(col.id) ? "▾" : "▸"}</span>
                <span className="text-xs font-semibold text-gray-300 group-hover:text-white truncate">
                  {col.info.name}
                </span>
              </button>
              {expanded.has(col.id) &&
                col.requests.map((req) => (
                  <button
                    key={req.id}
                    onClick={() => selectRequest(req)}
                    className={`w-full flex items-center gap-2 pl-8 pr-3 py-1.5 hover:bg-gray-800 text-left border-r-2 transition-colors ${
                      selected?.id === req.id
                        ? "bg-gray-800/80 border-indigo-500"
                        : "border-transparent"
                    }`}
                  >
                    <span
                      className={`text-xs font-mono font-bold w-9 shrink-0 ${METHOD_COLOR[req.method] ?? "text-gray-400"}`}
                    >
                      {req.method}
                    </span>
                    <span className="text-xs text-gray-400 truncate leading-relaxed">{req.name}</span>
                  </button>
                ))}
            </div>
          ))}
        </nav>
        <div className="px-4 py-3 border-t border-gray-800 flex-shrink-0">
          <p className="text-xs text-gray-600 leading-relaxed">
            Edit <span className="font-mono text-gray-500">public/collections/*.json</span> to add requests.
          </p>
        </div>
      </aside>

      {/* Main pane */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {selected ? (
          <>
            {/* URL bar */}
            <div className="flex items-center gap-3 px-5 py-3 border-b border-gray-800 bg-gray-900/60 flex-shrink-0">
              <span
                className={`text-xs font-mono font-bold w-12 shrink-0 ${METHOD_COLOR[selected.method] ?? "text-gray-400"}`}
              >
                {selected.method}
              </span>
              <div className="flex-1 min-w-0 bg-gray-800 border border-gray-700 rounded-lg px-3 py-1.5 font-mono text-xs text-gray-300 truncate">
                <span className="text-gray-500">{supabaseUrl}</span>
                {selected.path}
              </div>
              <button
                onClick={sendRequest}
                disabled={loading}
                className="flex-shrink-0 px-4 py-1.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white text-xs font-semibold rounded-lg transition-colors"
              >
                {loading ? "Sending…" : "Send"}
              </button>
            </div>

            {/* Tab bar */}
            <div className="flex items-center border-b border-gray-800 bg-gray-900/40 flex-shrink-0">
              {(["body", "auth"] as const).map((t) => (
                <button
                  key={t}
                  onClick={() => setTab(t)}
                  className={`px-5 py-2.5 text-xs font-semibold uppercase tracking-wide border-b-2 transition-colors ${
                    tab === t
                      ? "border-indigo-500 text-indigo-400"
                      : "border-transparent text-gray-500 hover:text-gray-300"
                  }`}
                >
                  {t === "body" ? "Body" : "Auth"}
                </button>
              ))}
              {selected.description && (
                <span className="ml-auto pr-5 text-xs text-gray-600 italic truncate max-w-xs">
                  {selected.description}
                </span>
              )}
            </div>

            {/* Request panel */}
            <div className="flex-shrink-0 border-b border-gray-800" style={{ height: "38%" }}>
              {tab === "body" ? (
                <textarea
                  value={editedBody}
                  onChange={(e) => setEditedBody(e.target.value)}
                  className="w-full h-full font-mono text-xs bg-gray-950 text-gray-300 resize-none p-4 focus:outline-none leading-relaxed"
                  placeholder={selected.method === "GET" ? "No body for GET requests." : "Request body (JSON)…"}
                  spellCheck={false}
                  readOnly={selected.method === "GET"}
                />
              ) : (
                <div className="p-5 space-y-5 overflow-y-auto h-full">
                  <div>
                    <label className="text-xs text-gray-500 uppercase tracking-wide block mb-2">
                      Auth Type
                    </label>
                    <div className="flex gap-2">
                      {(["user-jwt", "service-role", "none"] as AuthType[]).map((a) => (
                        <button
                          key={a}
                          onClick={() => setAuthType(a)}
                          className={`px-3 py-1.5 text-xs rounded-md border transition-colors font-mono ${
                            authType === a
                              ? "border-indigo-500 bg-indigo-900/30 text-indigo-300"
                              : "border-gray-700 text-gray-500 hover:border-gray-600 hover:text-gray-300"
                          }`}
                        >
                          {a}
                        </button>
                      ))}
                    </div>
                  </div>

                  {authType === "user-jwt" && (
                    <div>
                      <label className="text-xs text-gray-500 uppercase tracking-wide block mb-2">
                        JWT Access Token
                      </label>
                      <textarea
                        value={jwt}
                        onChange={(e) => setJwt(e.target.value)}
                        className="w-full h-20 font-mono text-xs bg-gray-800 border border-gray-700 rounded-lg p-3 text-gray-300 focus:outline-none focus:border-indigo-500 resize-none"
                        placeholder="Paste your Supabase access_token here…"
                      />
                      <p className="text-xs text-gray-600 mt-1.5">
                        Get it from the mobile app console or Supabase Auth dashboard.
                      </p>
                    </div>
                  )}

                  {authType === "service-role" && (
                    <div className="rounded-lg bg-amber-950/30 border border-amber-800/40 px-4 py-3">
                      <p className="text-xs font-semibold text-amber-400 mb-1">Service Role Key</p>
                      <p className="text-xs text-amber-700 leading-relaxed">
                        Read server-side from{" "}
                        <span className="font-mono text-amber-600">SUPABASE_SERVICE_ROLE_KEY</span>. Bypasses
                        RLS — all users&apos; data is visible. Set it in{" "}
                        <span className="font-mono">.env.local</span> to use.
                      </p>
                    </div>
                  )}

                  {authType === "none" && (
                    <p className="text-xs text-gray-600">No authorization header will be sent.</p>
                  )}
                </div>
              )}
            </div>

            {/* Response panel */}
            <div className="flex-1 flex flex-col overflow-hidden">
              <div className="flex items-center gap-4 px-5 py-2.5 border-b border-gray-800 bg-gray-900/40 flex-shrink-0">
                <span className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Response</span>
                {response && (
                  <>
                    <span className={`text-sm font-bold font-mono ${statusColor(response.status)}`}>
                      {response.status} {response.statusText}
                    </span>
                    {response.time > 0 && (
                      <span className="text-xs text-gray-600">{response.time}ms</span>
                    )}
                    {response.size > 0 && (
                      <span className="text-xs text-gray-600">{bytesLabel(response.size)}</span>
                    )}
                    {response.body && (
                      <button
                        onClick={() => navigator.clipboard.writeText(tryFormatJson(response.body))}
                        className="ml-auto text-xs text-gray-600 hover:text-gray-300 transition-colors"
                      >
                        Copy
                      </button>
                    )}
                  </>
                )}
              </div>

              <div className="flex-1 overflow-auto p-4">
                {error && (
                  <div className="rounded-lg bg-red-950/30 border border-red-800/40 px-4 py-3 mb-4">
                    <p className="text-xs font-semibold text-red-400 mb-1">Error</p>
                    <p className="text-xs text-red-600 font-mono">{error}</p>
                  </div>
                )}

                {loading && !streamText && (
                  <p className="text-xs text-gray-600 animate-pulse">Waiting for response…</p>
                )}

                {streamText && (
                  <pre className="text-xs font-mono text-gray-300 whitespace-pre-wrap leading-relaxed">
                    {streamText}
                    {loading && <span className="animate-pulse text-gray-600">█</span>}
                  </pre>
                )}

                {!loading && !streamText && response?.body && (
                  <pre className="text-xs font-mono text-gray-300 whitespace-pre-wrap leading-relaxed">
                    {tryFormatJson(response.body)}
                  </pre>
                )}

                {!loading && !streamText && !response && !error && (
                  <p className="text-xs text-gray-700">Hit Send to see the response here.</p>
                )}
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex flex-col items-center justify-center text-center gap-2">
            <p className="text-sm font-semibold text-gray-500">Select a request</p>
            <p className="text-xs text-gray-700">Choose a collection item from the sidebar to get started.</p>
          </div>
        )}
      </div>
    </div>
  );
}
