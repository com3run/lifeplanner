import React from "react";
import { colors, radius } from "../tokens";

export interface SwitchProps {
  checked: boolean;
  accent?: string;
  onChange?: (checked: boolean) => void;
}

/** A toggle switch. */
export function Switch({ checked, accent = colors.primary, onChange }: SwitchProps) {
  return (
    <button
      role="switch"
      aria-checked={checked}
      onClick={() => onChange?.(!checked)}
      style={{
        width: 46,
        height: 28,
        borderRadius: radius.full,
        border: "none",
        background: checked ? accent : colors.divider,
        position: "relative",
        cursor: "pointer",
        transition: "background 150ms",
        padding: 0,
      }}
    >
      <span
        style={{
          position: "absolute",
          top: 3,
          left: checked ? 21 : 3,
          width: 22,
          height: 22,
          borderRadius: radius.full,
          background: "#FFFFFF",
          boxShadow: "0 1px 3px rgba(0,0,0,0.2)",
          transition: "left 150ms",
        }}
      />
    </button>
  );
}
