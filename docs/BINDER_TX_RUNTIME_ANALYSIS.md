# Runtime Binder TX analysis — live capture (Frida)

Source log: `libvc_hooks_82f0.log` (318 events, Poco X3 Pro / Android 13)

## TX frequency (forensic)

| Code | Name | Count | % | Verdict |
|---:|---|---:|---:|---|
| **13** | POLL_STATE | 263 | **82.7%** | **Background 1 Hz poll**, not UI buttons |
| 22 | SEEK_RANGE | 12 | 3.8% | Seek/scrub burst (2–4 rapid calls) |
| 15 | GET_STATUS | 8 | 2.5% | Status query after actions |
| 17 | SET_LOOP | 8 | 2.5% | Loop toggle |
| 18 | SET_ANGLE | 7 | 2.2% | Rotation 0/90/180/270 |
| 14 | SET_MODE | 5 | 1.6% | Mode + path switch |
| 24 | TRANSFORM | 4 | 1.3% | 三色 injection params |
| 25 | HARD_RECOVERY | 4 | 1.3% | Recovery toggle (paired 2×) |
| 16 | SET_AUTO_ROTATE | 2 | 0.6% | Auto-rotate bool |
| 11 | PLAY_SOURCE | 2 | 0.6% | Start playback |
| 19 | SET_MIRROR | 2 | 0.6% | Mirror bool |
| 12 | STOP_OR_QUERY | 1 | 0.3% | Stop pipeline |

## TX13 — NOT button commands

**Confirmed** from reconstructed Java:

```java
// App.java — poll TX13 every 1s
int[] state = VliveBridge.service().pollState();
```

Native handler `FUN_00540324` (TX13): `writeNoException` + **5× int32** counters — no input payload beyond interface token.

Every other TX is followed by 1–N poll ticks → creates illusion that "13 = buttons".

## Per-TX hypotheses (backed by Java + Ghidra)

| TX | Hypothesis | Evidence |
|---|---|---|
| **11** | Start demux/decode for path | `writeString(path)`, appears after TX14 clusters |
| **12** | Stop / query idle | Rare; end of session |
| **13** | **Heartbeat / pipeline counters** | 82.7% traffic, 1 Hz, empty input |
| **14** | Set mode (int) + media path (string) | Before TX11; user switched source |
| **15** | Get playback status (int, 5=playing) | After mode/play changes |
| **16** | Auto-rotate enable | `writeInt(bool)` |
| **17** | Loop enable | Often paired with 16/18 |
| **18** | Rotation angle (0, 0x5a, 0xb4, 0x10e) | Triple burst = user cycling angles |
| **19** | Mirror flag | Single bool |
| **22** | Seek range µs (two int64) | Burst 2–4 = scrubbing timeline |
| **24** | Transform: mode + 4 float + color int | Double fire = apply + confirm |
| **25** | Hard recovery XOR toggle | Always paired 2× |

## Typical sequences from log

```text
TX14 → TX13×N → TX11        # set path + play
TX16 / TX17 / TX18          # display settings
TX22×2–4                    # seek scrub
TX24×2                      # color/transform
TX25×2                      # recovery
TX12 → TX11                 # stop then restart
```

## Next capture (advanced sniffer)

Use updated `frida_hook_libvc.js`:
- Full `[TX_START]..[TX_END]` blocks with Parcel hexdump
- TX13 suppressed to `[POLL_SUMMARY]` every 20 ticks
- Press **one button**, capture **one block**, diff hex vs another button

```bash
PID=$(pidof vcplax)
/data/local/tmp/frida-inject -p "$PID" \
  -s /data/local/tmp/frida_hook_libvc.js --runtime=qjs \
  > /data/local/tmp/binder_sniff.log 2>&1 &
```
