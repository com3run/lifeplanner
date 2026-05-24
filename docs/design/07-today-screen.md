# D7 (installment 1) — The Today screen

> Part of **TRI-54** (Core screen redesigns), Design Overhaul (**TRI-47**). The first screen built
> on the new system — the **Today** agency surface from D2, using D3 tokens + the D4 `AppButton`.
> D7 ships screen-by-screen; this is Today.

---

## What shipped

`ui/today/TodayScreen.kt` + `TodayViewModel.kt` — a real, compiling, token-pure screen, reachable
now via **Profile → "Today (new design)"** (a temporary entry; it becomes the Home tab once the
shell migrates — see below).

It realises D2's **"top stories for your life"** front door (P1 — *what can I do right now?*), most-
relevant-first:

1. **Greeting** (time-of-day) + a one-line agency framing ("your call, not a to-do list").
2. **"Right now you could…"** — up to 3 ranked possibilities, each with a plain fit reason and (for
   habits) a one-tap `Do it` action.
3. **Today's habits** — inline check-in (tap the circle → `success` check), done items dimmed.
4. **Empty states** for both sections (P2/P6 — never a dead end).

Token discipline (D3/D4): every color via `MaterialTheme.modernColors`, spacing/padding/radius via
`LifePlannerDesign`, the action is an `AppButton(SECONDARY)`, type via the M3 scale. No raw hex/dp.

---

## The integration seams (honest about the branch state)

This branch (the design epic, off `main`) does **not** yet contain the Free Agents pillars (they're
the open PR stack, not merged). So Today is wired to the data that exists on `main` (habits, goals),
with **clean seams** where the pillar engines plug in — no faking:

| Surface | Today (now) | Becomes (when pillars land on `main`) |
|---|---|---|
| "Right now you could…" | `TodayViewModel.buildPossibilities()` — a simple, honest heuristic (an undone habit + nearest goals) | swap that one function for the **Pillar 2 `PossibilityEngine`** (ranked `ActionOption`s); the screen contract is already `List<Possibility>` |
| Choice Points | not shown | add a section fed by **Pillar 3 `ChoicePointDetector`** (already profile-aware from TRI-66) |
| Headline pulse | not shown | a single value-alignment/becoming pulse at the top, *once honest* (D2 §5, decided in **D8**) |
| Per-user framing | neutral | rank + copy adapt to the **Pillar 7 `DecisionProfile`** (TRI-65) |

The seam is deliberately one function (`buildPossibilities`) so the swap is trivial and low-risk.

---

## To promote Today to the Home tab (the remaining step)

Currently Today is an additive route (zero risk to the existing Home/"Life" tab). Promotion, done
during full D7:

1. Repoint the **Life** bottom-tab to `TodayScreen` (replace `appNavHome`'s Home content), per the
   D2 3-tab model (Today / Goals / You).
2. Fold the old Home's still-useful widgets into Today's sections or into Goals/You.
3. Apply the **paced-reveal** rules (D2 §5) to the sections.

This is held until the screen is validated and (ideally) the pillar stack has merged, so Today ships
with its real possibilities + choice points rather than the interim heuristic.

---

## Carried findings (from D4)

- **C1 — `GlassCard` hard-locks `isDark = true`.** Today uses `Surface` + tokens directly and is
  unaffected, but the card primitive still needs the one-line theme fix before screens adopt it.
- **C2 — ~345 ad-hoc buttons.** Today uses `AppButton`; migration of the rest proceeds per screen.

---

---

## Installment 2 — the Goals screen

`ui/goals/GoalsScreen.kt` + `GoalsViewModel.kt` — the D2 **Goals** canvas ("what am I working toward,
and why?"). Reachable via **Profile → "Goals (new design)"**.

- **Active first, then Completed**, sorted by nearest due date; each goal a token-pure card: a
  **category chip** (the now-canonical `displayName` + category color, from G1), title, a custom
  token-pure progress bar (category color on `surfaceVariant`), and due date.
- **New goal** (`AppButton`) and tapping a card **reuse the existing flows** (`goal_wizard`,
  `goal_detail/{id}`) — so it's functional today, not a mock.
- **Empty state** when there are no goals.
- **Pillar 1 seam:** `Goal` has no `valueId` on `main` yet, so the visible tag is the category;
  when Pillar 1 lands, the card grows a **value tag + one-tap Why-Chain** (P4). The card layout
  already leaves room for it.

Same additive, zero-risk approach as Today (new route, no change to the existing Goals tab).

## Next

- Continue D7 with the **You** canvas (the third D2 tab).
- Promote Today + Goals from preview routes into the bottom-tab shell (the D2 3-tab model) once
  validated / the pillar stack merges.
- **D8** decides the Today headline pulse.
- Build remaining D4 primitives (`AppTextField`, `AppChip`, `AppBottomSheet`, …) as screens need them.
