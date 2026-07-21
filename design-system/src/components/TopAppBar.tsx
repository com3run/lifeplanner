import React from "react";
import { colors, spacing, font } from "../tokens";

export interface TopAppBarProps {
  title: string;
  /** Optional leading element (usually a back button). */
  leading?: React.ReactNode;
  /** Optional trailing element (usually an action). */
  trailing?: React.ReactNode;
}

/** A screen top bar: leading slot, centered title, trailing slot. */
export function TopAppBar({ title, leading, trailing }: TopAppBarProps) {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: spacing.sm,
        height: 56,
        padding: `0 ${spacing.md}px`,
        background: colors.background,
        fontFamily: font.family,
      }}
    >
      <div style={{ width: 40, display: "flex", justifyContent: "flex-start" }}>{leading}</div>
      <div style={{ flex: 1, textAlign: "center", fontSize: 17, fontWeight: 700, color: colors.textPrimary }}>{title}</div>
      <div style={{ width: 40, display: "flex", justifyContent: "flex-end" }}>{trailing}</div>
    </div>
  );
}
