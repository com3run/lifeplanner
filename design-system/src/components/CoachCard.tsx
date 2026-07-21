import React from "react";
import { colors, radius, spacing, font } from "../tokens";

export interface CoachCardProps {
  /** Coach name, e.g. "Luna". */
  name: string;
  /** Role/subtitle, e.g. "Life Coach". */
  role: string;
  /** Emoji or avatar element shown in the leading tile. */
  avatar?: React.ReactNode;
  /** Accent color for the avatar tile (defaults to indigo). */
  accent?: string;
  onClick?: () => void;
}

/**
 * A coach / menu row: icon tile, name + role, chevron. The shape used across the You screen and
 * coach lists.
 */
export function CoachCard({ name, role, avatar = "🌟", accent = colors.primary, onClick }: CoachCardProps) {
  return (
    <div
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: spacing.sm,
        background: colors.surface,
        border: `1px solid ${colors.divider}`,
        borderRadius: radius.large,
        padding: spacing.md,
        cursor: onClick ? "pointer" : "default",
        fontFamily: font.family,
      }}
    >
      <div
        style={{
          width: 44,
          height: 44,
          borderRadius: radius.medium,
          background: `${accent}22`,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontSize: 20,
        }}
      >
        {avatar}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 16, fontWeight: 600, color: colors.textPrimary }}>{name}</div>
        <div style={{ fontSize: 13, color: colors.textSecondary }}>{role}</div>
      </div>
      <div style={{ color: colors.textTertiary, fontSize: 18 }}>›</div>
    </div>
  );
}
