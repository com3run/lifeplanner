import React from "react";
import { heroGradient, radius, spacing, font } from "../tokens";

export interface GradientHeroProps {
  /** Small uppercase label above the title, e.g. "YOUR SPACE" or a date. */
  eyebrow?: string;
  title: string;
  subtitle?: string;
  /** Optional element pinned to the right, usually a ProgressRing. */
  trailing?: React.ReactNode;
}

/**
 * The page hero. Every main screen (Today, Goals, You) leads with one: an eyebrow, a big title,
 * a subtitle, all in white on the signature indigo->violet gradient, with an optional trailing slot.
 */
export function GradientHero({ eyebrow, title, subtitle, trailing }: GradientHeroProps) {
  return (
    <div
      style={{
        background: heroGradient,
        borderRadius: radius.extraLarge,
        padding: spacing.xl,
        color: "#FFFFFF",
        fontFamily: font.family,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: spacing.md,
      }}
    >
      <div>
        {eyebrow ? (
          <div
            style={{
              fontSize: 12,
              fontWeight: 600,
              letterSpacing: 0.8,
              textTransform: "uppercase",
              opacity: 0.85,
              marginBottom: spacing.xs,
            }}
          >
            {eyebrow}
          </div>
        ) : null}
        <div style={{ fontSize: 30, fontWeight: 700, lineHeight: 1.15 }}>{title}</div>
        {subtitle ? (
          <div style={{ fontSize: 15, opacity: 0.9, marginTop: spacing.xs }}>{subtitle}</div>
        ) : null}
      </div>
      {trailing}
    </div>
  );
}
