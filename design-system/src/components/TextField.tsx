import React from "react";
import { colors, radius, spacing, font } from "../tokens";

export interface TextFieldProps {
  value?: string;
  placeholder?: string;
  label?: string;
  /** Show a clear (×) button when there is text. */
  clearable?: boolean;
  accent?: string;
  multiline?: boolean;
  onChange?: (value: string) => void;
  onClear?: () => void;
}

/** A bordered text input with an optional floating label and a clear button. */
export function TextField({
  value = "",
  placeholder,
  label,
  clearable = false,
  accent = colors.primary,
  multiline = false,
  onChange,
  onClear,
}: TextFieldProps) {
  const inputStyle: React.CSSProperties = {
    width: "100%",
    border: "none",
    outline: "none",
    background: "transparent",
    fontFamily: font.family,
    fontSize: 15,
    color: colors.textPrimary,
    resize: "none",
  };
  return (
    <div style={{ fontFamily: font.family }}>
      {label ? (
        <div style={{ fontSize: 12, color: accent, fontWeight: 600, marginBottom: 4 }}>{label}</div>
      ) : null}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: spacing.xs,
          border: `1.5px solid ${value ? accent : colors.divider}`,
          borderRadius: radius.medium,
          padding: `${spacing.sm}px ${spacing.md}px`,
          background: colors.surface,
        }}
      >
        {multiline ? (
          <textarea rows={3} value={value} placeholder={placeholder} onChange={(e) => onChange?.(e.target.value)} style={inputStyle} />
        ) : (
          <input value={value} placeholder={placeholder} onChange={(e) => onChange?.(e.target.value)} style={inputStyle} />
        )}
        {clearable && value ? (
          <button
            aria-label="Clear"
            onClick={onClear}
            style={{ border: "none", background: "transparent", color: colors.textTertiary, cursor: "pointer", fontSize: 16 }}
          >
            ×
          </button>
        ) : null}
      </div>
    </div>
  );
}
