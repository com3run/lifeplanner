import React from "react";
import { colors, radius, spacing, font } from "../tokens";

export interface StatTileProps {
  /** The number or short value, e.g. "12" or "3 days". */
  value: React.ReactNode;
  /** The label under it, e.g. "Habits" or "Streak". */
  label: string;
  /** Optional accent for the value (defaults to indigo primary). */
  accent?: string;
}

/**
 * A single labelled stat, as used in the "This Week" row (Habits / Focus / Journal / …).
 */
export function StatTile({ value, label, accent = colors.primary }: StatTileProps) {
  return (
    <div
      style={{
        flex: 1,
        background: colors.surfaceVariant,
        borderRadius: radius.medium,
        padding: spacing.md,
        textAlign: "center",
        fontFamily: font.family,
      }}
    >
      <div style={{ fontSize: 22, fontWeight: 700, color: accent }}>{value}</div>
      <div style={{ fontSize: 13, color: colors.textSecondary, marginTop: 2 }}>{label}</div>
    </div>
  );
}
