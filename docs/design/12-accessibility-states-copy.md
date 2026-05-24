# D12 — Accessibility, Empty/Error States & UX Copy

> **TRI-59** · Design Overhaul (**TRI-47**). The polish layer that defines top-tier craft:
> accessible to everyone, every state designed, every word warm. Serves **P2** (honest, no blame)
> and **P3** (agency-affirming, never guilt).

---

## 1. Accessibility — WCAG 2.1 AA checklist

Every screen/component is reviewed against this (the D7 gate):

- **Touch targets ≥ 48dp.** `AppButton` is 48 by default; icon-only buttons must pad to 48 even if
  the glyph is smaller.
- **Contrast** ≥ 4.5:1 for text, ≥ 3:1 for large text / icons — verify each `modernColors` pair in
  **both** light and dark (the D3 tokens were picked for this; re-check category colors on cards).
- **Never color-only.** Selected / error / done states also carry an icon, shape, or text cue (e.g.
  the habit check uses a filled icon + color, not color alone).
- **Semantics.** Every interactive node has a `contentDescription` / role; decorative icons pass
  `null` (as `IconChip` does); loading announces *busy*; selected toggles announce *selected*.
- **Dynamic type.** Respect the OS font scale; no fixed-height containers that clip scaled text.
- **Reduce-motion.** Honor the OS setting — D10 motion collapses to instant/crossfade; nothing
  depends on animation to be understood.
- **Focus order.** Logical traversal for keyboard / switch / screen-reader users.

## 2. State patterns — `ui/components/StateView.kt`

The canonical **empty / error / loading** state: centered icon (or spinner) + title + message +
optional action. Designed, friendly, and offering a way forward — **never a dead end** (P2/P6).

- **Empty** — inviting, not apologetic: "Nothing here yet — start with one that matters to you"
  (now used on the Goals canvas).
- **Error** — say *what happened* and *how to recover*, never blame the user: "Couldn't sync just
  now — we'll retry automatically" + a "Try again".
- **Loading** — a calm spinner with context, not a blank screen.

**Rule:** every screen ships all three. A screen with no empty + error state is incomplete (D7 gate).

## 3. UX copy voice

**Voice:** a warm, plain-spoken thinking partner. Second person ("you"), active verbs, no jargon,
no guilt, no hype. Agency-affirming — the user decides (P1/P3).

| Context | ✅ Do | ⛔ Don't |
|---|---|---|
| Buttons | verbs: "Create your first goal", "Do it" | "Submit", "OK" |
| A missed habit | "Life happens — pick it back up when you're ready" | "You broke your streak!" |
| Empty state | "Start with one that matters to you" | "No data." |
| Error | "Couldn't sync — we'll retry" | "Error 500: request failed" |
| Insight | "You tend to focus best in the mornings" (+ confidence) | "Your productivity score: 42/100" |
| Celebration | "Three days of showing up — that's becoming a pattern" | "🎉🎉 STREAK!! Don't lose it!!" |

Microcopy is part of design, not a fill-in-later: write it with the screen.

## 4. Handoff

- `StateView` is the empty/error/loading primitive for all D7 screens; adopt it as each screen lands.
- A full WCAG audit pass (contrast measurements, screen-reader walkthrough) runs once the redesigned
  screens are promoted into the shell.
- Copy voice here governs D11 onboarding and every new string.

*Next: **D9** (motivation & emotional design), **D6** (signature interaction), **D11** (onboarding),
the **D5** Figma visual identity, or **D13** (handoff specs — last).*
