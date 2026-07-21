import React from "react";
import { colors, radius, spacing, font } from "../tokens";

export interface SegmentedControlProps {
  /** Segment labels. */
  options: string[];
  /** Index of the selected segment. */
  value: number;
  accent?: string;
  onChange?: (index: number) => void;
}

/** A segmented control, e.g. the System / Light / Dark appearance picker. */
export function SegmentedControl({ options, value, accent = colors.primary, onChange }: SegmentedControlProps) {
  return (
    <div
      style={{
        display: "inline-flex",
        gap: spacing.xxs,
        padding: spacing.xxs,
        background: colors.surfaceVariant,
        borderRadius: radius.medium,
        fontFamily: font.family,
      }}
    >
      {options.map((opt, i) => {
        const on = i === value;
        return (
          <button
            key={opt}
            onClick={() => onChange?.(i)}
            style={{
              padding: "8px 18px",
              borderRadius: radius.small,
              border: "none",
              background: on ? accent : "transparent",
              color: on ? colors.onPrimary : colors.textSecondary,
              fontSize: 14,
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            {opt}
          </button>
        );
      })}
    </div>
  );
}
