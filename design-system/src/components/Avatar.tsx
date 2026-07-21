import React from "react";
import { colors, radius, font } from "../tokens";

export interface AvatarProps {
  /** Image URL. If absent, initials or an emoji are shown. */
  src?: string;
  /** Name used to derive initials when there is no image. */
  name?: string;
  /** Emoji/element shown when there is no image and no name. */
  fallback?: React.ReactNode;
  size?: number;
  accent?: string;
}

function initials(name?: string): string {
  if (!name) return "";
  return name.trim().split(/\s+/).slice(0, 2).map((w) => w[0]?.toUpperCase() ?? "").join("");
}

/** A round avatar: image, or initials/emoji on a tinted circle. */
export function Avatar({ src, name, fallback = "🙂", size = 44, accent = colors.primary }: AvatarProps) {
  const content = name ? initials(name) : fallback;
  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: radius.full,
        overflow: "hidden",
        background: `${accent}22`,
        color: accent,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: font.family,
        fontWeight: 700,
        fontSize: size * 0.38,
      }}
    >
      {src ? (
        <img src={src} alt={name ?? ""} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
      ) : (
        content
      )}
    </div>
  );
}
