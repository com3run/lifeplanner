#!/usr/bin/env python3
"""Generate an idempotent baseline migration from supabase/schema.sql.

The source schema is already in correct dependency order (functions -> tables ->
triggers -> indexes -> RLS -> policies, with later-appended tables carrying their
own self-contained blocks). Two things are added here:

  1. The pgvector extension. schema.sql uses vector(768) but only mentions
     CREATE EXTENSION in a comment, so a from-scratch build fails on the type.
  2. Idempotency, so the migration is a no-op against the existing production
     database rather than erroring on objects that already exist.
"""
import re
import sys

src, dst = sys.argv[1], sys.argv[2]
lines = open(src).read().split("\n")

HEADER = """-- ============================================================
-- Baseline schema for LifePlanner (generated from supabase/schema.sql)
-- ============================================================
-- This is the initial migration adopting Supabase migrations on an existing
-- project. It is written to be IDEMPOTENT so it can run safely against both:
--   * a fresh preview/branch database (creates everything), and
--   * the existing production database (no-op, everything already exists).
--
-- Regenerate with scripts/gen_migration.py if schema.sql changes.
-- ============================================================

-- schema.sql declares vector(768) columns but only mentions this in a comment.
-- A from-scratch build needs the extension to actually exist.
CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA extensions;

"""

out = []
i = 0
stats = {"table": 0, "index": 0, "policy": 0, "trigger": 0}

while i < len(lines):
    line = lines[i]

    # CREATE TABLE x ( -> CREATE TABLE IF NOT EXISTS x (
    m = re.match(r"^CREATE TABLE (?!IF NOT EXISTS)(\w+)", line)
    if m:
        out.append(re.sub(r"^CREATE TABLE ", "CREATE TABLE IF NOT EXISTS ", line, count=1))
        stats["table"] += 1
        i += 1
        continue

    # CREATE [UNIQUE] INDEX name -> ... IF NOT EXISTS name
    m = re.match(r"^CREATE (UNIQUE )?INDEX (?!IF NOT EXISTS)(\w+)", line)
    if m:
        uniq = m.group(1) or ""
        out.append(
            re.sub(
                r"^CREATE (UNIQUE )?INDEX ",
                f"CREATE {uniq}INDEX IF NOT EXISTS ",
                line,
                count=1,
            )
        )
        stats["index"] += 1
        i += 1
        continue

    # CREATE POLICY name ON table -> preceded by a DROP POLICY IF EXISTS
    m = re.match(r"^CREATE POLICY (\w+)\s+ON\s+(\w+)", line)
    if m:
        out.append(f"DROP POLICY IF EXISTS {m.group(1)} ON {m.group(2)};")
        out.append(line)
        stats["policy"] += 1
        i += 1
        continue

    # CREATE TRIGGER name ... ON table (table may be on a later line)
    m = re.match(r"^CREATE TRIGGER (\w+)", line)
    if m:
        name = m.group(1)
        # look ahead through the statement for its target table
        table, j = None, i
        while j < len(lines):
            t = re.search(r"\bON\s+(\w+)", lines[j])
            if t:
                table = t.group(1)
                break
            if ";" in lines[j]:
                break
            j += 1
        if not table:
            raise SystemExit(f"could not resolve target table for trigger {name}")
        out.append(f"DROP TRIGGER IF EXISTS {name} ON {table};")
        out.append(line)
        stats["trigger"] += 1
        i += 1
        continue

    out.append(line)
    i += 1

open(dst, "w").write(HEADER + "\n".join(out))
print(f"wrote {dst}")
print(
    "idempotent: {table} tables, {index} indexes, {policy} policies, {trigger} triggers".format(
        **stats
    )
)
