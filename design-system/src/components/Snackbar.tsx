import React from "react";
import { colors, radius, spacing, font, elevation } from "../tokens";

export interface SnackbarProps {
  message: string;
  /** Optional action label (e.g. "Undo"). */
  actionLabel?: string;
  onAction?: () => void;
}

/** A transient bottom snackbar with an optional action. */
export function Snackbar({ message, actionLabel, onAction }: SnackbarProps) {
  return (
    <div
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: spacing.md,
        background: colors.textPrimary,
        color: "#FFFFFF",
        borderRadius: radius.medium,
        padding: `${spacing.sm}px ${spacing.md}px`,
        boxShadow: elevation.raised,
        fontFamily: font.family,
        fontSize: 14,
      }}
    >
      <span style={{ flex: 1 }}>{message}</span>
      {actionLabel ? (
        <button
          onClick={onAction}
          style={{ border: "none", background: "transparent", color: colors.primary, fontWeight: 700, cursor: "pointer", fontSize: 14 }}
        >
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}
