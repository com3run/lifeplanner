# @lifeplanner/design-system

A small **React mirror** of LifePlanner's core UI, styled from the v2 tokens.

The app itself is Kotlin Compose, which Claude Design can't consume. This package exists so
`/design-sync` has a real React component library to import — from then on Claude Design builds
LifePlanner screens out of these actual components instead of generic ones.

## Components

| Component | What it is |
|---|---|
| `Button` | Primary / secondary / text, pill, 48px, optional leading icon |
| `Card` | Standard surface card: white fill, hairline border, whisper shadow (the GlassCard) |
| `GradientHero` | Page hero: eyebrow + title + subtitle on the indigo→violet gradient, trailing slot |
| `ProgressRing` | Circular progress, used in heroes and goal detail |
| `StatTile` | A single labelled stat (the "This Week" row) |
| `CoachCard` | Icon tile + name + role + chevron row |

All components are **self-contained**: styles come from `src/tokens.ts` (the v2 palette) as inline
styles, so each renders correctly with no theme provider or external stylesheet.

## Keeping it in sync

`src/tokens.ts` mirrors the Compose theme (`ModernColors`, identity `CLASSIC`). If the app palette
changes, update `tokens.ts` here to match. This is a design-time mirror, not a shared runtime — the
app does not depend on it.

## Build

```bash
npm install
npm run build   # tsup -> dist/index.js (ESM) + dist/index.d.ts
```
