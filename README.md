# Stringblade — 弦切

A cyber bow and an FE-charged quiver for **Timeless and Classics Zero (TaCZ)**.

This branch targets **Minecraft 1.20.1 / Forge**.

| | |
|---|---|
| Minecraft | 1.20.1 |
| Loader | Forge 47.x |
| TaCZ | 1.2.0-hotfix (Forge build) — works with 1.1.8 and newer |
| Curios | 5.14.1+1.20.1 |

Looking for 1.21.1? See the [`main`](../../tree/main) branch.

## What it adds

- **弦切 / Stringblade** — a charge-drawn bow. Hold fire to draw, release to shoot.
  Base damage 24 (falloff 24 / 19 / 15), headshot ×1.4, projectile speed 72,
  gravity 0.03, zeroed at 16 blocks, and the round leaves from the middle of the bow.
- **弧光箭筒 / Arc Quiver** — an ammo box that stores **Forge Energy** instead of arrows:
  12 000 FE per charge, 300 FE per shot, 40 shots. Standard FE capability, plus
  1 redstone dust = 1 shot as a fallback.
  Wears in the Curios **back** slot and feeds the bow from there.
- Both recipes are on TaCZ's **gun smith table**.

See `dist/` for a ready-to-use jar.

## Version notes

- Requires **TaCZ 1.1.8 or newer**. Older TaCZ has no charge (`ChargeData`) support,
  so the draw mechanic would not exist.
- On TaCZ **1.1.8** the HUD ammo counter does not include a worn quiver (that hook needs
  a class added in 1.2.0, and is skipped automatically). Firing, charging and wearing all work.

## Build

```bash
./gradlew build        # -> build/libs/*.jar
```

TaCZ and Curios are pulled from their mavens; nothing needs to be installed by hand.

## Credit

Built for TaCZ by the TaCZ team. Assets and code in this repository are original.
