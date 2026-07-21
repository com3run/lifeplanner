import React from "react";
import { colors, radius } from "../tokens";

export interface IconTileProps {
  /** Icon element (emoji or SVG). */
  icon: React.ReactNode;
  /** Tile tint / icon color. */
  accent?: string;
  size?: number;
}

/** A rounded square icon tile, the leading element on list rows and cards. */
export function IconTile({ icon, accent = colors.primary, size = 44 }: IconTileProps) {
  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: radius.medium,
        background: `${accent}22`,
        color: accent,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: size * 0.45,
      }}
    >
      {icon}
    </div>
  );
}
