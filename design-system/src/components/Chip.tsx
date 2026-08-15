import React from "react";
import { colors, radius, font } from "../tokens";

export interface ChipProps {
  children: React.ReactNode;
  /** Selected chips fill with the accent tint. */
  selected?: boolean;
  accent?: string;
  onClick?: () => void;
}

/** A selectable pill chip, e.g. the For You filters or category picks. */
export function Chip({ children, selected = false, accent = colors.primary, onClick }: ChipProps) {
  return (
    <button
      onClick={onClick}
      style={{
        padding: "8px 16px",
        borderRadius: radius.full,
        border: `1px solid ${selected ? accent : colors.divider}`,
        background: selected ? accent : colors.surface,
        color: selected ? colors.onPrimary : colors.textSecondary,
        fontFamily: font.family,
        fontSize: 14,
        fontWeight: 500,
        cursor: "pointer",
      }}
    >
      {children}
    </button>
  );
}
