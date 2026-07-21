import React from "react";
import { colors, radius, font } from "../tokens";

export type BadgeTone = "primary" | "success" | "warning" | "error" | "neutral";

export interface BadgeProps {
  children: React.ReactNode;
  tone?: BadgeTone;
}

const TONE: Record<BadgeTone, string> = {
  primary: colors.primary,
  success: colors.success,
  warning: colors.warning,
  error: colors.error,
  neutral: colors.textSecondary,
};

/** A small pill label for status or counts, in a semantic tone. */
export function Badge({ children, tone = "primary" }: BadgeProps) {
  const c = TONE[tone];
  return (
    <span
      style={{
        display: "inline-block",
        padding: "3px 10px",
        borderRadius: radius.full,
        background: `${c}1F`,
        color: c,
        fontFamily: font.family,
        fontSize: 12,
        fontWeight: 600,
      }}
    >
      {children}
    </span>
  );
}
