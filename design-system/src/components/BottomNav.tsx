import React from "react";
import { colors, radius, spacing, font, elevation } from "../tokens";

export interface BottomNavItem {
  label: string;
  icon: React.ReactNode;
}

export interface BottomNavProps {
  items: BottomNavItem[];
  /** Index of the active tab. */
  value: number;
  accent?: string;
  onChange?: (index: number) => void;
}

/** The bottom tab bar: a rounded pill of tabs; the active tab shows its label. */
export function BottomNav({ items, value, accent = colors.primary, onChange }: BottomNavProps) {
  return (
    <div
      style={{
        display: "inline-flex",
        gap: spacing.xxs,
        padding: spacing.xxs,
        background: colors.surface,
        border: `1px solid ${colors.divider}`,
        borderRadius: radius.full,
        boxShadow: elevation.card,
        fontFamily: font.family,
      }}
    >
      {items.map((item, i) => {
        const on = i === value;
        return (
          <button
            key={item.label}
            onClick={() => onChange?.(i)}
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: spacing.xs,
              padding: `10px ${on ? 18 : 14}px`,
              borderRadius: radius.full,
              border: "none",
              background: on ? colors.primaryContainer : "transparent",
              color: on ? accent : colors.textTertiary,
              fontSize: 14,
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            {item.icon}
            {on ? <span>{item.label}</span> : null}
          </button>
        );
      })}
    </div>
  );
}
