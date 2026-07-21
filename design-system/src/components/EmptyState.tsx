import React from "react";
import { colors, spacing, font } from "../tokens";

export interface EmptyStateProps {
  /** Large icon/emoji at the top. */
  icon?: React.ReactNode;
  title: string;
  message?: string;
  /** Optional call-to-action element (usually a Button). */
  action?: React.ReactNode;
}

/** The empty-state block: icon, title, message, optional CTA. Warm, never a dead end. */
export function EmptyState({ icon = "🌱", title, message, action }: EmptyStateProps) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        textAlign: "center",
        padding: `${spacing.xxl}px ${spacing.xl}px`,
        fontFamily: font.family,
      }}
    >
      <div style={{ fontSize: 44, marginBottom: spacing.sm }}>{icon}</div>
      <div style={{ fontSize: 17, fontWeight: 600, color: colors.textPrimary }}>{title}</div>
      {message ? (
        <div style={{ fontSize: 14, color: colors.textSecondary, marginTop: spacing.xxs, maxWidth: 320 }}>{message}</div>
      ) : null}
      {action ? <div style={{ marginTop: spacing.md }}>{action}</div> : null}
    </div>
  );
}
