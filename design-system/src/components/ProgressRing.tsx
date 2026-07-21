import React from "react";
import { colors, font } from "../tokens";

export interface ProgressRingProps {
  /** 0..1. */
  progress: number;
  /** Outer diameter in px. */
  diameter?: number;
  strokeWidth?: number;
  /** Ring color. Defaults to the indigo primary; pass white when on the hero. */
  color?: string;
  trackColor?: string;
  /** Center content, e.g. a percentage or level. */
  children?: React.ReactNode;
}

/**
 * Circular progress ring. Used inside the hero (level/streak) and on goal detail.
 */
export function ProgressRing({
  progress,
  diameter = 64,
  strokeWidth = 7,
  color = colors.primary,
  trackColor = colors.divider,
  children,
}: ProgressRingProps) {
  const clamped = Math.max(0, Math.min(1, progress));
  const r = (diameter - strokeWidth) / 2;
  const c = 2 * Math.PI * r;
  const center = diameter / 2;

  return (
    <div style={{ position: "relative", width: diameter, height: diameter }}>
      <svg width={diameter} height={diameter} style={{ transform: "rotate(-90deg)" }}>
        <circle cx={center} cy={center} r={r} fill="none" stroke={trackColor} strokeWidth={strokeWidth} />
        <circle
          cx={center}
          cy={center}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={c}
          strokeDashoffset={c * (1 - clamped)}
        />
      </svg>
      <div
        style={{
          position: "absolute",
          inset: 0,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          fontFamily: font.family,
          fontWeight: 700,
          fontSize: 14,
          color,
        }}
      >
        {children}
      </div>
    </div>
  );
}
