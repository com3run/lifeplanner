import React from "react";
import { heroGradient, radius, spacing, font } from "../tokens";

export interface GradientButtonProps {
  children: React.ReactNode;
  leadingIcon?: React.ReactNode;
  onClick?: () => void;
  fullWidth?: boolean;
}

/** A high-emphasis button filled with the indigo→violet brand gradient, white text. */
export function GradientButton({ children, leadingIcon, onClick, fullWidth = false }: GradientButtonProps) {
  return (
    <button
      onClick={onClick}
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
        height: 48,
        padding: `0 ${spacing.xl}px`,
        borderRadius: radius.full,
        border: "none",
        background: heroGradient,
        color: "#FFFFFF",
        fontFamily: font.family,
        fontSize: 15,
        fontWeight: 600,
        cursor: "pointer",
        width: fullWidth ? "100%" : "auto",
      }}
    >
      {leadingIcon}
      {children}
    </button>
  );
}
