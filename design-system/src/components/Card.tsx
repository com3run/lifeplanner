import React from "react";
import { colors, radius, spacing, elevation } from "../tokens";

export interface CardProps {
  children: React.ReactNode;
  /** Inner padding (px). Defaults to the 16px card content padding. */
  padding?: number;
  style?: React.CSSProperties;
}

/**
 * The standard surface card: white fill, hairline border, whisper-soft shadow. Depth comes from
 * the surface contrast, not a heavy drop shadow. Mirrors the app's GlassCard.
 */
export function Card({ children, padding = spacing.md, style }: CardProps) {
  return (
    <div
      style={{
        background: colors.surface,
        border: `1px solid ${colors.divider}`,
        borderRadius: radius.large,
        boxShadow: elevation.card,
        padding,
        ...style,
      }}
    >
      {children}
    </div>
  );
}
