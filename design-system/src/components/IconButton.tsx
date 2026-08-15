import React from "react";
import { colors, radius } from "../tokens";

export interface IconButtonProps {
  /** The icon element (emoji or SVG). */
  icon: React.ReactNode;
  "aria-label": string;
  /** Circular tinted background behind the icon. */
  tinted?: boolean;
  accent?: string;
  size?: number;
  onClick?: () => void;
}

/** A circular icon-only button, e.g. the focus-timer shortcut on a habit row. */
export function IconButton({
  icon,
  tinted = true,
  accent = colors.primary,
  size = 40,
  onClick,
  ...rest
}: IconButtonProps) {
  return (
    <button
      aria-label={rest["aria-label"]}
      onClick={onClick}
      style={{
        width: size,
        height: size,
        borderRadius: radius.full,
        border: "none",
        background: tinted ? `${accent}22` : "transparent",
        color: accent,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        cursor: "pointer",
        fontSize: size * 0.45,
      }}
    >
      {icon}
    </button>
  );
}
