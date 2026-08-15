import React from "react";
import { colors, radius, spacing, font } from "../tokens";
import { IconTile } from "./IconTile";

export interface ListRowProps {
  title: string;
  subtitle?: string;
  /** Leading icon (wrapped in an IconTile). */
  icon?: React.ReactNode;
  accent?: string;
  /** Show the trailing chevron. */
  chevron?: boolean;
  /** Optional trailing element (overrides the chevron). */
  trailing?: React.ReactNode;
  onClick?: () => void;
}

/** The standard settings/menu row: icon tile, title + subtitle, chevron. */
export function ListRow({ title, subtitle, icon, accent = colors.primary, chevron = true, trailing, onClick }: ListRowProps) {
  return (
    <div
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: spacing.sm,
        padding: `${spacing.sm}px ${spacing.md}px`,
        borderRadius: radius.medium,
        background: colors.surface,
        cursor: onClick ? "pointer" : "default",
        fontFamily: font.family,
      }}
    >
      {icon != null ? <IconTile icon={icon} accent={accent} size={40} /> : null}
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: colors.textPrimary }}>{title}</div>
        {subtitle ? <div style={{ fontSize: 13, color: colors.textSecondary }}>{subtitle}</div> : null}
      </div>
      {trailing ?? (chevron ? <span style={{ color: colors.textTertiary, fontSize: 18 }}>›</span> : null)}
    </div>
  );
}
