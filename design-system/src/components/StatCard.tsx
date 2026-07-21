import React from "react";
import { colors, radius, spacing, font } from "../tokens";
import { IconTile } from "./IconTile";

export interface StatCardProps {
  value: React.ReactNode;
  label: string;
  /** Optional leading icon. */
  icon?: React.ReactNode;
  accent?: string;
}

/** A stat with an optional icon, a large value, and a label. Richer than StatTile. */
export function StatCard({ value, label, icon, accent = colors.primary }: StatCardProps) {
  return (
    <div
      style={{
        background: colors.surface,
        border: `1px solid ${colors.divider}`,
        borderRadius: radius.large,
        padding: spacing.md,
        fontFamily: font.family,
        display: "flex",
        alignItems: "center",
        gap: spacing.sm,
      }}
    >
      {icon != null ? <IconTile icon={icon} accent={accent} size={40} /> : null}
      <div>
        <div style={{ fontSize: 22, fontWeight: 700, color: colors.textPrimary }}>{value}</div>
        <div style={{ fontSize: 13, color: colors.textSecondary }}>{label}</div>
      </div>
    </div>
  );
}
