# Le Chonk sprite set

Pixel art sprites for Le Chonk, the chonky orange striped cat:

| File | Frames | Use |
| --- | --- | --- |
| `lechonk_idle_1.png`, `lechonk_idle_2.png` | 2 | Idle pose (breathing animation) |
| `lechonk_run_1.png` … `lechonk_run_8.png` | 8 | Run cycle, played while Le Chonk runs on the ground |
| `lechonk_jump.png` | 1 | Jump pose, shown while Le Chonk is in the air |

## Source and license

The sprites come from the **Orange Fat Cat** pack by **shuaa**
(www.megupets.com), downloaded from
https://opengameart.org/content/orange-fat-cat

The pack is dedicated to the public domain under
**CC0 1.0 Universal (CC0 1.0) — Public Domain Dedication**
(see the pack's `Read Me.txt`: "Sprites by shuaa, www.megupets.com,
CC0 1.0 Universal (CC0 1.0), Public Domain Dedication"). No credit is
required, and the sprites may be used, modified and redistributed
freely.

## How these files were produced

The pack ships high-resolution frames (`Idle/CAT IDLE0001-0002.png`,
`Walk/CAT WALK0001-0019.png`, `Jump/CAT JUMP0001-0010.png`). The files
in this folder are those frames, unchanged in content, downscaled 8x
with nearest-neighbour sampling so they stay crisp pixel art and small
enough to ship in the app:

- `lechonk_idle_1.png` / `lechonk_idle_2.png` ← `Idle/CAT IDLE0001.png` / `CAT IDLE0002.png`
- `lechonk_run_1..8.png` ← `Walk/CAT WALK0001, 0003, 0006, 0008, 0011, 0013, 0016, 0018.png`
  (8 frames sampled evenly across the 19-frame walk cycle, used as the run cycle)
- `lechonk_jump.png` ← `Jump/CAT JUMP0005.png` (the apex of the jump, used as the jump pose)
