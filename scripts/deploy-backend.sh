#!/usr/bin/env bash
#
# Deploy LifePlanner Supabase backend (edge functions).
#
# One-time setup (interactive, do this yourself in a terminal):
#   supabase login
#
# Then just run:
#   ./scripts/deploy-backend.sh                 # deploy all functions
#   ./scripts/deploy-backend.sh ai-proxy        # deploy a single function
#
set -euo pipefail

PROJECT_REF="rkdggdfabwgukspylybu"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# All deployable edge functions.
ALL_FUNCTIONS=(ai-proxy auth-redirect mcp-server persona-sync-webhook resend-verification health store-watch)

# --- auth check -------------------------------------------------------------
if ! supabase projects list >/dev/null 2>&1; then
  echo "ERROR: Supabase CLI is not authenticated."
  echo "Run this once in your terminal, then re-run this script:"
  echo "    supabase login"
  echo "(or export SUPABASE_ACCESS_TOKEN=... for CI)"
  exit 1
fi

# --- link (idempotent) ------------------------------------------------------
if [[ ! -f supabase/.temp/project-ref ]] || [[ "$(cat supabase/.temp/project-ref)" != "$PROJECT_REF" ]]; then
  echo ">> Linking to project $PROJECT_REF ..."
  supabase link --project-ref "$PROJECT_REF"
fi

# --- deploy -----------------------------------------------------------------
if [[ $# -gt 0 ]]; then
  TARGETS=("$@")
else
  TARGETS=("${ALL_FUNCTIONS[@]}")
fi

echo ">> Deploying functions: ${TARGETS[*]}"
for fn in "${TARGETS[@]}"; do
  echo "---- deploy $fn ----"
  supabase functions deploy "$fn" --project-ref "$PROJECT_REF"
done

echo ">> Done. Deployed: ${TARGETS[*]}"
