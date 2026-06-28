# IceCam MaxRecon v8 — TX24 transform reconstruction

This build is a clean-room reconstruction focused on the parts that are now well understood:

- root native deployment
- `/data/vcplax <ServerName>` startup
- Binder connection to the recovered service name
- TX11 / TX14 source control
- TX22 range control
- TX24 transform control
- TX25 stop/reset
- floating overlay controls
- expanded runtime diagnostics

## TX24 model used by v8

Recovered signature:

```text
TX24(int, float, float, float, float, int)
```

v8 maps it as:

```text
TX24(mode, panX, panY, zoomX, zoomY, flags)
```

Modes:

```text
0 FREE
1 FIT
2 FILL
3 STRETCH
4 NATIVE
```

Flags:

```text
bit 0      mirror horizontal
bit 1      mirror vertical
bits 2..3  rotation quadrant: 0/90/180/270
bit 5      auto rotate
bit 6      lock aspect
```

## Floating window mapping

The original 4x4 control layout was remapped to practical transform controls:

```text
Zoom +     Up       Zoom -     Fit/Fill
Left       Center   Right      Reset
Play       Down     Loop       Status
Rotate     Mirror   Stop       Close
```

Each transform button immediately saves state and sends TX24.

## What is still not guaranteed

The original native code is stripped. v8 does not claim exact source recovery. It sends the most probable transform parcel layout according to the recovered DEX/Binder signature and Android renderer design. If native TX24 uses a different internal meaning, the logs will show successful Binder transactions but visual transform may remain unchanged.

## Debug procedure

1. Start app.
2. Grant root.
3. Select media.
4. Enable floating window permission.
5. Press Root bootstrap.
6. Press Connect.
7. Press Play from overlay.
8. Test Zoom / Pan / Rotate / Mirror.
9. Share log from Settings if TX24 returns errors or no visual change.
