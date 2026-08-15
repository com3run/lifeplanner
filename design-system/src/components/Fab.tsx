import React from "react";
import { colors, radius, elevation } from "../tokens";

export interface FabProps {
  /** The icon element, usually a "+". */
  icon?: React.ReactNode;
  "aria-label": string;
  accent?: string;
  onClick?: () => void;
}

/** The floating action button, e.g. the context-aware "+" in the nav. */
export function Fab({ icon = "+", accent = colors.primary, onClick, ...rest }: FabProps) {
  return (
    <button
      aria-label={rest["aria-label"]}
      onClick={onClick}
      style={{
        width: 56,
        height: 56,
        borderRadius: radius.full,
        border: "none",
        background: accent,
        color: colors.onPrimary,
        fontSize: 26,
        fontWeight: 400,
        lineHeight: 1,
        boxShadow: elevation.raised,
        cursor: "pointer",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      {icon}
    </button>
  );
}
