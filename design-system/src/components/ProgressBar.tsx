import React from "react";
import { colors, radius } from "../tokens";

export interface ProgressBarProps {
  /** 0..1. */
  progress: number;
  color?: string;
  trackColor?: string;
  height?: number;
}

/** A linear progress bar, e.g. XP-to-next-level. */
export function ProgressBar({
  progress,
  color = colors.primary,
  trackColor = colors.surfaceVariant,
  height = 8,
}: ProgressBarProps) {
  const clamped = Math.max(0, Math.min(1, progress));
  return (
    <div style={{ width: "100%", height, borderRadius: radius.full, background: trackColor, overflow: "hidden" }}>
      <div style={{ width: `${clamped * 100}%`, height: "100%", borderRadius: radius.full, background: color }} />
    </div>
  );
}
