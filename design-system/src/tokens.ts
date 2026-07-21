/**
 * LifePlanner v2 design tokens.
 *
 * Generated-equivalent to lifeplanner-assets/design/tokens.json (identity: CLASSIC), which itself
 * comes from the Compose theme (ModernColors). Kept in sync by hand here; if the app palette
 * changes, update both. Baked into the components as inline styles so each one renders correctly
 * with no theme provider or external stylesheet.
 */
export const colors = {
  primary: "#4A6FFF",
  primaryContainer: "#ECF0FF",
  secondary: "#7A5AF8",
  accent: "#F86E5A",
  success: "#28C76F",
  warning: "#FF9F43",
  error: "#EA5455",
  background: "#F8F9FC",
  surface: "#FFFFFF",
  surfaceVariant: "#F0F2FA",
  textPrimary: "#2C3345",
  textSecondary: "#6E7A94",
  textTertiary: "#9AA6BC",
  textDisabled: "#CBD0DD",
  divider: "#E8ECF4",
  onPrimary: "#FFFFFF",
} as const;

/** The signature indigo->violet hero gradient. */
export const heroGradient = "linear-gradient(135deg, #667EEA 0%, #764BA2 100%)";

/** 8dp spacing ladder (px). */
export const spacing = {
  xxs: 4,
  xs: 8,
  sm: 12,
  md: 16,
  lg: 20,
  xl: 24,
  xxl: 32,
} as const;

/** Corner radii (px). `full` is the pill radius. */
export const radius = {
  extraSmall: 8,
  small: 12,
  medium: 16,
  large: 20,
  extraLarge: 24,
  full: 999,
} as const;

export const font = {
  family:
    "'Satoshi', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
} as const;

export const elevation = {
  card: "0 1px 2px rgba(44, 51, 69, 0.06)",
  raised: "0 6px 20px rgba(44, 51, 69, 0.10)",
} as const;
