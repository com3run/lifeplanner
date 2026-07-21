import React from "react";
import { colors, radius, spacing, font } from "../tokens";

export type ButtonVariant = "primary" | "secondary" | "text";

export interface ButtonProps {
  /** Button label. */
  children: React.ReactNode;
  /** Visual weight. `primary` = filled indigo, `secondary` = tonal, `text` = borderless. */
  variant?: ButtonVariant;
  /** Optional leading icon (any element). */
  leadingIcon?: React.ReactNode;
  disabled?: boolean;
  onClick?: () => void;
  /** Stretch to the container width. */
  fullWidth?: boolean;
}

/**
 * The primary action button. Pill-shaped, 48px tall, matches the app's AppButton.
 */
export function Button({
  children,
  variant = "primary",
  leadingIcon,
  disabled = false,
  onClick,
  fullWidth = false,
}: ButtonProps) {
  const palette: Record<ButtonVariant, React.CSSProperties> = {
    primary: { background: colors.primary, color: colors.onPrimary, border: "none" },
    secondary: {
      background: colors.primaryContainer,
      color: colors.primary,
      border: "none",
    },
    text: { background: "transparent", color: colors.primary, border: "none" },
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: spacing.xs,
        height: 48,
        padding: `0 ${spacing.xl}px`,
        borderRadius: radius.full,
        fontFamily: font.family,
        fontSize: 15,
        fontWeight: 600,
        cursor: disabled ? "not-allowed" : "pointer",
        opacity: disabled ? 0.45 : 1,
        width: fullWidth ? "100%" : "auto",
        ...palette[variant],
      }}
    >
      {leadingIcon}
      {children}
    </button>
  );
}
