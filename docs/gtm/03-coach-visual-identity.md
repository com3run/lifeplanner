# 03 — Coach Visual Identity

**Last updated**: 2026-04-23
**Scope**: master character prompt framework for the 7 built-in coaches — **Luna, Alex, Morgan, Kai, Sam, River, Jamie** — aligned with the Council Model in `coach-collaboration-and-profile-aware-goals.md` and the positioning in `00-strategy.md`.
**Toolchain**: Midjourney v6.1 primary, Flux + PulID fallback for identity-lock if MJ drifts.
**Design target**: UK-premium "council of mentors" aesthetic — cinematic, photorealistic, trustworthy, aspirational-but-approachable. Not influencer gloss. Not new-age.

---

## 1. Why a master framework (and not free-styling each coach)

The ads, landing page, app store listings, in-app portraits, and any future social content all need **the same seven faces, recognisably consistent across dozens of scenes**. If Luna looks like three different people across our creatives, we lose the Council Model's central message.

One master framework + 7 parameter sheets + strict identity-lock protocol solves this.

Reference: the original Luna visual brief Kamran authored sets the tone (premium, photoreal, ambiguous-racial, 8k editorial). This doc takes that brief and extends it to the 6 specialists without breaking consistency with Luna.

---

## 2. Shared visual grammar (applies to all 7 coaches)

The grammar is what makes the seven feel like **one Tribe**.

### Photography direction
- **Medium**: photorealistic 8k editorial. Natural skin, real pores, subtle asymmetry. No CGI, no anime, no plastic skin.
- **Lens**: 85mm (default portrait) or 50mm (full body / environmental). Shallow depth of field. Sharp focus on eyes.
- **Lighting**: natural daylight as the hero, soft key + warm fill. Golden hour for warmer coaches (Luna, Sam, River, Jamie); crisp morning daylight for sharper coaches (Alex, Morgan, Kai).
- **Colour grading**: editorial, slightly warm, natural contrast. Avoid heavy stylisation, no Instagram filters, no HDR glow.

### Wardrobe direction
- **Palette per coach** (see §4), but all palettes share: refined, quiet-luxury, no logos, no bold patterns.
- Fabrics: wool, fine knit, linen, cotton poplin, silk, cashmere. No synthetics. No trendy athleisure (exception: Kai).
- Fit: tailored but not stiff. Slightly imperfect — a sleeve rolled once, a collar relaxed.

### Setting direction
- Minimalist, warm-modern architecture.
- Natural textures: wood, concrete, linen, plants.
- No visual clutter. No branded products. No visible tech beyond the phone / notebook the coach is holding.

### Consistent anchor across scenes (per-coach)
Each coach gets **one signature accessory** that appears in every scene — a minimalist watch, specific earrings, a leather notebook, a pair of glasses. This is the cheapest way to signal continuity without fighting Midjourney's facial drift.

### Universal negative prompt (append to every render)
```
--no plastic skin, doll face, waxy face, airbrushed, CGI, 3D render, anime, cartoon,
cheap fashion, messy background, clutter, branded logos, extra limbs, extra fingers,
deformed hands, poor anatomy, low resolution, oversaturated, heavy makeup, fantasy ears,
elf ears, exaggerated anatomy, unrealistic proportions
```

---

## 3. The 12-block prompt scaffold

Every coach's master prompt follows this structure. Midjourney weights tokens by position — keep the order.

```
[01 IDENTITY]   — name (internal), age band, ethnicity, archetype phrase
[02 FACE]       — bone structure, features, eye color, default expression
[03 HAIR]       — color, length, texture, styling
[04 BODY]       — height, build, posture
[05 HANDS]      — grooming, gesture register
[06 WARDROBE]   — palette, silhouette, fabrics, signature accessory
[07 SETTING]    — default environment
[08 LIGHTING]   — source, direction, colour temp
[09 CAMERA]     — lens, framing, depth of field
[10 ENERGY]     — one sentence on what the viewer feels
[11 TECH TAGS]  — photorealism markers
[12 NEGATIVE]   — --no parameters
```

The **universal scaffold** text to paste into MJ is in `visual_pipeline_v1.md` (deprecated outputs folder) §1.1. It is reproduced below, refreshed, with the per-coach fills following.

### Universal scaffold template
```
{NAME}, a {AGE_BAND} {ETHNICITY} {gender}, {ARCHETYPE_PHRASE}.
Face: {BONE_STRUCTURE}, {EYE_DESCRIPTOR}, {NOSE}, {LIPS}, {EYEBROWS}, {SKIN_QUALITY}, {DEFAULT_EXPRESSION}.
Hair: {HAIR_COLOR}, {HAIR_LENGTH}, {HAIR_TEXTURE}, {HAIR_STYLING}.
Body: {HEIGHT_IMPRESSION}, {BUILD}, {POSTURE}.
Wardrobe: {WARDROBE_PALETTE}, {SILHOUETTE}, {FABRICS}, {SIGNATURE_ACCESSORY}.
Setting: {SETTING}.
Lighting: {LIGHTING}.
Shot: {LENS}, {FRAMING}, cinematic depth of field, sharp focus on eyes, {COLOR_GRADE}.
Energy: {ENERGY_LINE}.
Photorealistic, 8k, editorial magazine quality, luxury brand campaign aesthetic, natural skin texture, real pores, subtle asymmetry, no plastic skin, no CGI, no anime, no cartoon.
--ar 3:4 --style raw --v 6.1 --s 150 --no plastic skin, doll face, waxy face, airbrushed, CGI, 3D render, anime, cartoon, cheap fashion, extra limbs, deformed hands, oversaturated
```

---

## 4. The 7 coaches — parameter sheets

Each coach has (a) a UK-premium title that introduces them in marketing, (b) a personality anchor in one sentence, (c) the full parameter sheet that feeds the master prompt.

Existing coach avatars in `docs/coach-avatars/` should be treated as **concept studies** — regenerate hero portraits using these prompts and replace. The goal is a unified look, not a patched one.

---

### 4.1 Luna — "Your Guide"
**Domain**: Wellbeing + Orchestrator
**Coach ID**: `luna_general`
**Personality anchor**: The woman who already mastered life and now teaches others — warm, wise, deeply present.
**UK-premium role**: Luna is the face of the app. She is the first coach the user meets, the one who orchestrates the Council, the one who shows up on the app icon and most ads.

| Slot | Value |
|---|---|
| AGE_BAND | 34–39 |
| ETHNICITY | Ambiguous — racially-indeterminate Caucasian / Mediterranean / Eurasian; light-olive skin |
| ARCHETYPE_PHRASE | a calm, elite, nurturing mentor who radiates emotional intelligence and grounded wisdom |
| BONE_STRUCTURE | high cheekbones, soft jawline, straight elegant nose, symmetrical naturally beautiful face |
| EYE_DESCRIPTOR | bright expressive hazel eyes with emotional depth, warm direct gaze |
| NOSE | straight elegant nose with soft bridge |
| LIPS | full healthy lips, subtle knowing smile, natural teeth |
| EYEBROWS | intelligent relaxed brows, refined arch |
| SKIN_QUALITY | realistic pores, healthy glow, mature beauty, minimal makeup |
| DEFAULT_EXPRESSION | peaceful confidence with subtle wisdom and emotional safety |
| HAIR_COLOR | espresso / chestnut brown with subtle natural highlights |
| HAIR_LENGTH | medium-long |
| HAIR_TEXTURE | thick, healthy, luxurious natural shine, soft movement |
| HAIR_STYLING | flowing or loose low styling, elegant but natural |
| HEIGHT_IMPRESSION | 5'8 impression, long elegant proportions |
| BUILD | lean athletic softness, feminine waistline, graceful neck |
| POSTURE | strong upright posture, intentional calm movement |
| WARDROBE_PALETTE | ivory, cream, soft taupe, muted gold, dusty rose |
| SILHOUETTE | tailored blazer, silk blouse, refined trouser; or elegant knit dress |
| FABRICS | silk, cashmere, fine wool, linen |
| SIGNATURE_ACCESSORY | delicate gold chain with a small pendant, refined gold watch |
| SETTING | serene modern home studio, linen curtains, plants, soft wood textures |
| LIGHTING | warm natural daylight, soft diffused key, warm fill |
| LENS | 85mm, shallow depth of field |
| FRAMING | 3:4 portrait, shoulders-up default |
| COLOR_GRADE | editorial warm, natural contrast |
| ENERGY_LINE | the kind of presence that makes people trust her with their future |

---

### 4.2 Alex — "Your Strategist"
**Domain**: Career
**Coach ID**: `alex_career`
**Personality anchor**: The senior operator who's seen every path and knows which moves compound — sharp, kind, results-driven.

| Slot | Value |
|---|---|
| AGE_BAND | 35–42 |
| ETHNICITY | Ambiguous — can lean Northern European / British / mixed-Mediterranean |
| gender | male (gender-neutral name but visual leans male for visual diversity across the roster; can be tuned) |
| ARCHETYPE_PHRASE | a seasoned career strategist, sharp and calm, the colleague you always wish you had |
| BONE_STRUCTURE | defined jawline, straight nose, symmetrical face, light stubble |
| EYE_DESCRIPTOR | steel-grey or blue-grey eyes, direct focused gaze, subtle warmth |
| NOSE | straight, angular, slightly prominent bridge |
| LIPS | medium, neutral, hint of half-smile |
| EYEBROWS | well-groomed, slightly heavier on the inner edge, intelligent arch |
| SKIN_QUALITY | healthy mature skin, subtle lines, very faint beard shadow |
| DEFAULT_EXPRESSION | calm authority, ready-to-listen, slight forward lean |
| HAIR_COLOR | dark brown with subtle grey at the temples |
| HAIR_LENGTH | short, sharp sides, slightly longer on top |
| HAIR_TEXTURE | thick, healthy, natural |
| HAIR_STYLING | combed back or natural side parting, polished but not slick |
| HEIGHT_IMPRESSION | 6'0 impression, lean proportions |
| BUILD | lean, broad shoulders, not bulky |
| POSTURE | upright confident, slight forward lean when listening |
| WARDROBE_PALETTE | navy, charcoal, ivory, deep olive |
| SILHOUETTE | tailored navy blazer over open collar shirt, dark trousers; or fine merino roll neck |
| FABRICS | wool, fine cotton, cashmere |
| SIGNATURE_ACCESSORY | understated steel watch with leather strap, leather portfolio notebook |
| SETTING | modern glass-walled office with natural wood, city skyline softly blurred; alternative: elegant library |
| LIGHTING | crisp morning daylight, soft key, slight directional shadow |
| LENS | 85mm portrait; 50mm for desk/environmental |
| FRAMING | 3:4 portrait |
| COLOR_GRADE | editorial, neutral, slightly cool |
| ENERGY_LINE | the person you'd call before making any big career decision |

---

### 4.3 Morgan — "Your Architect"
**Domain**: Money
**Coach ID**: `morgan_finance`
**Personality anchor**: The calm advisor who treats money as a tool and teaches you to see it clearly — analytical, trustworthy, never judgmental.
**Note on gender**: Morgan is gender-neutral. For visual consistency across the roster, recommend rendering as **female-presenting** (to balance the gender distribution among the 7 coaches). If feedback pushes otherwise, easy to swap.

| Slot | Value |
|---|---|
| AGE_BAND | 32–38 |
| ETHNICITY | Ambiguous — Caucasian or mixed, cool-toned complexion |
| gender | female |
| ARCHETYPE_PHRASE | a sharp, calm financial architect who makes money feel simple and safe |
| BONE_STRUCTURE | defined but soft, strong cheekbones, refined jawline |
| EYE_DESCRIPTOR | clear blue-grey or cool hazel eyes, steady analytical gaze |
| NOSE | straight, slightly narrow, balanced |
| LIPS | medium, natural, slight closed smile |
| EYEBROWS | well-defined, natural arch |
| SKIN_QUALITY | clear, natural, even tone, minimal makeup |
| DEFAULT_EXPRESSION | composed intelligence with quiet warmth |
| HAIR_COLOR | warm blonde or light chestnut with subtle highlights |
| HAIR_LENGTH | chin to shoulder length |
| HAIR_TEXTURE | sleek, healthy, subtle waves |
| HAIR_STYLING | tucked behind one ear, clean centre parting |
| HEIGHT_IMPRESSION | 5'7 |
| BUILD | lean, upright, no-nonsense silhouette |
| POSTURE | upright, still, confident stillness |
| WARDROBE_PALETTE | camel, cream, slate grey, deep navy |
| SILHOUETTE | tailored camel coat over ivory knit; or refined merino jumper and trousers |
| FABRICS | wool, cashmere, leather (belt, bag) |
| SIGNATURE_ACCESSORY | elegant steel bracelet watch; leather card-holder wallet on desk |
| SETTING | light-filled home office with minimalist desk, single large plant, stacked books |
| LIGHTING | cool natural daylight, soft key, neutral fill |
| LENS | 85mm portrait |
| FRAMING | 3:4 portrait, sometimes with laptop in foreground for environmental shots |
| COLOR_GRADE | editorial, neutral, slightly cool |
| ENERGY_LINE | the person who makes money feel like a tool, not a threat |

---

### 4.4 Kai — "Your Performance Coach"
**Domain**: Body (Physical / Fitness)
**Coach ID**: `kai_fitness`
**Personality anchor**: The grounded athlete who treats the body as a long relationship — energetic but calm, never drill-sergeant.

| Slot | Value |
|---|---|
| AGE_BAND | 29–34 |
| ETHNICITY | Ambiguous — can lean Pacific / Southeast Asian / Mediterranean, warm mid-tone skin |
| gender | male |
| ARCHETYPE_PHRASE | a grounded, energetic performance coach who respects the body as a long relationship |
| BONE_STRUCTURE | strong but balanced — defined jaw, clean cheekbones, open face |
| EYE_DESCRIPTOR | warm brown eyes, direct and engaged, slight smile in the eyes |
| NOSE | broad-but-straight, well-proportioned |
| LIPS | full, relaxed, natural smile |
| EYEBROWS | strong natural brows |
| SKIN_QUALITY | tanned mid-tone, healthy glow, natural texture |
| DEFAULT_EXPRESSION | calm energetic confidence with warmth |
| HAIR_COLOR | dark brown, near black |
| HAIR_LENGTH | short-medium, textured crop |
| HAIR_TEXTURE | thick with natural wave |
| HAIR_STYLING | slightly messy but intentional |
| HEIGHT_IMPRESSION | 5'11, athletic proportions |
| BUILD | lean muscular, not bulky, functional strength |
| POSTURE | upright athletic stance, relaxed shoulders |
| WARDROBE_PALETTE | stone, sage, charcoal, off-white |
| SILHOUETTE | premium technical knit, fine-gauge cotton tee, tapered jogger or relaxed trouser; no logos |
| FABRICS | merino technical blend, cotton jersey, linen |
| SIGNATURE_ACCESSORY | minimalist sport watch (black or sand) |
| SETTING | minimalist training studio with warm wood floor, or outdoor coastal trail at sunrise |
| LIGHTING | crisp natural daylight, golden hour optional for outdoor |
| LENS | 50mm for movement / full body, 85mm for portrait |
| FRAMING | 3:4 portrait; full-body allowed |
| COLOR_GRADE | editorial, slightly warm, natural contrast |
| ENERGY_LINE | the coach who makes moving feel like meeting a friend, not paying a debt |

---

### 4.5 Sam — "Your Confidant"
**Domain**: Social (relationships, friendships, networking)
**Coach ID**: `sam_social`
**Personality anchor**: The friend who's good in the room and knows how to read people — warm, curious, excellent listener.

| Slot | Value |
|---|---|
| AGE_BAND | 30–36 |
| ETHNICITY | Ambiguous — mixed heritage, warm medium skin tone |
| gender | female (Sam is gender-neutral; leaning female rendering to diversify the roster) |
| ARCHETYPE_PHRASE | a warm, curious confidant who reads rooms and helps others feel seen |
| BONE_STRUCTURE | soft with gentle definition, round-to-oval face |
| EYE_DESCRIPTOR | warm dark brown eyes, curious attentive gaze |
| NOSE | soft, gently rounded |
| LIPS | full, expressive, often caught mid-smile |
| EYEBROWS | natural, slightly arched |
| SKIN_QUALITY | radiant natural skin, no heavy makeup |
| DEFAULT_EXPRESSION | curious warmth, slight inviting tilt of the head |
| HAIR_COLOR | dark brown or rich black |
| HAIR_LENGTH | medium to long |
| HAIR_TEXTURE | soft natural curls or waves |
| HAIR_STYLING | loose, partially tied back, natural |
| HEIGHT_IMPRESSION | 5'6 |
| BUILD | soft feminine, relaxed posture |
| POSTURE | open body language, relaxed shoulders, slight lean toward camera |
| WARDROBE_PALETTE | dusty rose, cream, soft terracotta, warm ivory |
| SILHOUETTE | relaxed linen shirt, knit cardigan, easy trouser or dress |
| FABRICS | linen, cotton, cashmere |
| SIGNATURE_ACCESSORY | small gold hoops, delicate bracelet stack |
| SETTING | cosy minimalist café corner, or warm living room with plants and books |
| LIGHTING | warm tungsten or golden-hour daylight |
| LENS | 85mm portrait |
| FRAMING | 3:4 portrait, often captured mid-conversation |
| COLOR_GRADE | warm editorial |
| ENERGY_LINE | the friend everyone wishes they had on speed dial |

---

### 4.6 River — "Your Compass"
**Domain**: Purpose (Spiritual / Wellness)
**Coach ID**: `river_wellness`
**Personality anchor**: The calm presence who sits with hard questions — grounded, reflective, never performative.
**Note**: Use grounded realism to avoid the "hippie wellness" cliché. River is a modern reflective thinker, not a retreat leader.

| Slot | Value |
|---|---|
| AGE_BAND | 38–45 |
| ETHNICITY | Ambiguous — can lean Scandinavian / Celtic / mixed-European, light-tone |
| gender | male |
| ARCHETYPE_PHRASE | a grounded, reflective purpose-guide who holds space for hard questions |
| BONE_STRUCTURE | lean expressive face, soft-but-defined, slight asymmetry |
| EYE_DESCRIPTOR | soft blue-grey or green eyes, thoughtful quiet gaze |
| NOSE | long, straight, characterful |
| LIPS | medium, calm, slight natural smile |
| EYEBROWS | natural, slightly peaked, thoughtful |
| SKIN_QUALITY | weathered-in-a-good-way, subtle lines, healthy natural tone |
| DEFAULT_EXPRESSION | quiet attentiveness, present but not intense |
| HAIR_COLOR | dark ash brown or salt-and-pepper at temples |
| HAIR_LENGTH | medium, slightly tousled |
| HAIR_TEXTURE | straight to slight wave |
| HAIR_STYLING | natural, softly swept back |
| HEIGHT_IMPRESSION | 6'0 |
| BUILD | lean, slightly slight, calm stillness in the body |
| POSTURE | upright still, hands often folded or at ease |
| WARDROBE_PALETTE | sage, stone, off-white, deep forest |
| SILHOUETTE | relaxed linen shirt, loose merino roll neck, soft wool cardigan |
| FABRICS | linen, raw wool, cotton |
| SIGNATURE_ACCESSORY | a small leather-bound notebook, simple leather bracelet |
| SETTING | quiet natural space — wooden porch at dawn, simple home library, garden bench |
| LIGHTING | soft dawn or dusk light, warm diffused |
| LENS | 85mm portrait |
| FRAMING | 3:4 portrait, sometimes mid-ground environmental |
| COLOR_GRADE | warm editorial, slightly muted |
| ENERGY_LINE | the person who makes you want to put your phone down and think |

---

### 4.7 Jamie — "Your Anchor"
**Domain**: Family
**Coach ID**: `jamie_family`
**Personality anchor**: The wise, patient confidant for family and home life — nurturing, honest, holds the long view.
**Note on gender**: Jamie is gender-neutral. Recommend rendering as **female-presenting** (warm matriarchal archetype resonates with the Family category in most UK households). Easy to swap if needed.

| Slot | Value |
|---|---|
| AGE_BAND | 40–47 |
| ETHNICITY | Ambiguous — mixed European / British, warm medium-light tone |
| gender | female |
| ARCHETYPE_PHRASE | a wise, patient family anchor with a warm practical mind |
| BONE_STRUCTURE | soft mature features, gentle cheekbones, warm oval face |
| EYE_DESCRIPTOR | warm deep-brown or hazel eyes, patient attentive gaze |
| NOSE | soft, slightly rounded |
| LIPS | natural, often in a gentle smile |
| EYEBROWS | natural mature brows |
| SKIN_QUALITY | mature natural skin, subtle lines, soft warmth, light or no makeup |
| DEFAULT_EXPRESSION | gentle knowing, relaxed presence, subtle smile |
| HAIR_COLOR | warm mid-brown with occasional natural silver strands |
| HAIR_LENGTH | shoulder length |
| HAIR_TEXTURE | soft natural wave |
| HAIR_STYLING | loose, effortless |
| HEIGHT_IMPRESSION | 5'6 |
| BUILD | soft elegant, relaxed |
| POSTURE | open, hands often resting on a cup or book |
| WARDROBE_PALETTE | soft rust, cream, warm taupe, deep burgundy |
| SILHOUETTE | wrap cardigan over tee, relaxed linen dress, soft knit jumper |
| FABRICS | wool, cotton, cashmere |
| SIGNATURE_ACCESSORY | small gold stud earrings, a well-worn leather-bound journal, or a simple ceramic mug |
| SETTING | warm family home — kitchen table with morning light, living room with plants and soft throws |
| LIGHTING | warm morning or early afternoon daylight |
| LENS | 85mm |
| FRAMING | 3:4 portrait, shoulders-up default |
| COLOR_GRADE | warm editorial, slightly earthy |
| ENERGY_LINE | the person who makes you feel you can bring any problem home |

---

## 5. Identity-lock protocol (apply to each coach)

For each coach, follow this 5-step sequence before any scene generation:

1. **Generate hero portrait** using that coach's full master prompt (scaffold + §4 parameters) in MJ v6.1.
2. **Select the best variation** — the one whose face is most symmetrical and expression matches the "default expression" in the parameter sheet. Upscale.
3. **Capture seed + URL** via Discord ✉️ reaction and copy-link.
4. **Run the 4 standard scene tests** (see §6) using `--cref [HERO_URL] --sref [HERO_URL] --seed [HERO_SEED] --cw 100`.
5. **Audit** using the checklist in §7. If ≥ 14/17 anchors hold across 4 scenes → coach approved. If < 14 → adjust parameter sheet, regenerate hero, retest.

**Time estimate**: 2–3 days per coach if identity-lock works on first try; 4–6 days if it requires prompt tuning. Run coaches in parallel to compress total time.

**Order of generation**: Luna first (she's the brand face; if her framework works, others follow the same pattern). Then the two most visually distinct (Kai — athletic male, Jamie — mature female) to stress-test. Then the remaining four.

---

## 6. Standard 4-scene test kit (per coach)

Produce these four scenes for every coach. They span the axes most likely to break identity:

1. **Indoor editorial portrait** — clean daylight, signature accessory visible, default expression, shoulders-up.
2. **Environmental wide** — coach in their default setting, captured at a candid moment (reading, writing, holding their signature accessory).
3. **Conversational mid-shot** — 3/4 turn to camera, mid-sentence expression, slight forward lean.
4. **Outdoor / natural light** — golden hour or overcast daylight, same wardrobe feel, identity preserved.

These four cover the creative needs for Week 2 ad launch. Full 8-scene kit (used for ongoing content) can be generated in Week 5+ once identity is locked.

---

## 7. Identity audit checklist

Use this for every coach's 4 scenes. Score ✓ / ~ / ✗ per anchor.

| Anchor | Scene 1 | 2 | 3 | 4 |
|---|---|---|---|---|
| Face width | | | | |
| Nose shape | | | | |
| Eye color | | | | |
| Eye spacing | | | | |
| Jawline | | | | |
| Ear shape | | | | |
| Skin tone | | | | |
| Hair color | | | | |
| Hair length | | | | |
| Hair texture | | | | |
| Posture | | | | |
| Build | | | | |
| Default expression | | | | |
| Wardrobe palette coherence | | | | |
| Signature accessory present | | | | |
| Lighting quality | | | | |
| Skin texture (pores, no plastic) | | | | |

Scoring:
- **≥ 14 ✓ per scene × 4 scenes** → coach locked. Approve for production use.
- **10–13 ✓** → tune the weakest anchors (usually nose, ear, hair highlights) and retest.
- **< 10 ✓** → switch this coach to Flux + PulID workflow (see §8).

---

## 8. Flux + PulID fallback

If any coach drifts beyond Midjourney's ability to hold identity:

1. Use the hero portrait (from §5 step 2) as the face reference image.
2. Generate scenes via Flux with PulID face embedding, hosted on Replicate or fal.ai.
3. Paste the scene description only — drop `--cref` / `--seed` (those are MJ-only).
4. Cost: ~$0.01–$0.05 per image, vs. MJ's subscription model. Reasonable for production-critical identity coaches even if slower than MJ.

---

## 9. Marketing usage — where each coach shows up

| Surface | Coaches featured | Notes |
|---|---|---|
| App icon | Luna | Single portrait framed gently |
| App Store screenshots (iOS + Android) | All 7 grid, then individual coaches | First screenshot shows the full roster |
| Landing page hero | Luna + composite of other 6 | Tests: Luna alone vs. roster |
| Meta Ads — Hook A | All 7 in a grid | "Which coach do you need today?" |
| Meta Ads — Hook B | Luna + Council mode 3-coach composite | "A council, not a chatbot" |
| Meta Ads — Hook C | Life Balance Wheel + coach next to weak area | "See your whole life" |
| Instagram organic | Each coach gets their own weekly content angle | Tribe Content Hub skills can generate per-coach content |
| Push notifications in-app | Matched to coach based on user behaviour | Notifications show coach portrait + one-liner |

---

## 10. Version control

This document is v1. Changes to any coach's parameter sheet should:
1. Bump this doc's version number.
2. Log what changed in the decision log below.
3. Require re-running the 4-scene test kit before deploying updated portraits.
4. Keep old portraits in `docs/coach-avatars/archive/` with timestamp — don't delete. We may need them for creative diffing.

### Decision log

| Date | Coach | Change | Reason |
|---|---|---|---|
| 2026-04-23 | All | Added UK-premium titles (Guide / Strategist / Architect / Performance Coach / Confidant / Compass / Anchor) | User request — names already fit UK premium, titles elevate presentation without code changes |
| 2026-04-23 | Morgan, Sam, Jamie | Gender rendering leaning female for roster balance | Gender-neutral names allow either; balance spreads 4 female / 3 male across the 7 |

---

*End of coach visual identity v1.*
