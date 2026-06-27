#!/usr/bin/env python3
"""Parse binder_sniff.log — extract TX payloads (offset 0x54 after interface token)."""
from __future__ import annotations

import json
import re
import struct
import sys
from collections import Counter, defaultdict
from pathlib import Path

TOKEN_END = 0x54
TX_NAMES = {
    11: "PLAY_SOURCE", 12: "STOP_OR_QUERY", 13: "POLL_STATE", 14: "SET_MODE",
    15: "GET_STATUS", 16: "SET_AUTO_ROTATE", 17: "SET_LOOP", 18: "SET_ANGLE",
    19: "SET_MIRROR", 22: "SEEK_RANGE", 24: "TRANSFORM", 25: "HARD_RECOVERY",
}


def parse_hex_block(block: str, prefix: str = "DATA") -> bytes:
    out = bytearray()
    in_hex = False
    meta_size = None
    m = re.search(rf"{prefix}_meta size=(\d+)", block)
    if m:
        meta_size = int(m.group(1))
    for line in block.splitlines():
        if f"{prefix}_HEX_ALL" in line:
            in_hex = True
            continue
        if in_hex:
            if line.strip().startswith((f"{prefix}_INT32", f"{prefix}_ASCII", "---", "[TX_")):
                break
            m2 = re.match(r"[0-9a-fA-F]+\s+((?:[0-9a-f]{2}\s*)+)", line)
            if m2:
                out.extend(int(h, 16) for h in m2.group(1).split())
    return bytes(out[:meta_size] if meta_size else out)


def read_string16(data: bytes, off: int) -> tuple[str | None, int]:
    if off + 4 > len(data):
        return None, off
    slen = struct.unpack_from("<I", data, off)[0]
    start = off + 4
    end = start + slen * 2
    s = data[start:min(end, len(data))].decode("utf-16-le", errors="replace")
    if end > len(data):
        s += "…"
    end = (start + slen * 2 + 2 + 3) & ~3
    return s, end


def decode_payload(code: int, data: bytes):
    off = TOKEN_END
    if code in (12, 13, 15, 25):
        return "(empty)"
    if code == 11:
        path, o2 = read_string16(data, off)
        mi = struct.unpack_from("<i", data, o2)[0] if o2 + 4 <= len(data) else None
        lp = struct.unpack_from("<i", data, o2 + 4)[0] if o2 + 8 <= len(data) else None
        return {"path": path, "int0": mi, "loop": lp}
    if code == 14:
        mode = struct.unpack_from("<i", data, off)[0]
        path, _ = read_string16(data, off + 4)
        return {"mode": mode, "path_or_url": path}
    if code in (16, 17, 19):
        v = struct.unpack_from("<i", data, off)[0]
        return {"enabled": bool(v), "value": v}
    if code == 18:
        return {"degrees": struct.unpack_from("<i", data, off)[0]}
    if code == 22:
        b, e = struct.unpack_from("<qq", data, off)
        return {"begin_us": b, "end_us": e}
    if code == 24:
        mode, x, y, i, d, c = struct.unpack_from("<iffffI", data, off)
        return {"mode": mode, "x": x, "y": y, "intensity": i, "diameter": d, "colorMode": c}
    return data[off : off + 32].hex()


def main() -> int:
    path = Path(sys.argv[1] if len(sys.argv) > 1 else "binder_sniff.log")
    text = path.read_text()
    events = []
    for block in re.split(r"={70,}", text):
        if "[POLL_SUMMARY]" in block or "[POLL_STATE]" in block:
            continue
        m = re.search(r"\[TX_START\] seq=(\d+) code=(\d+).*?name=(\w+)", block)
        if not m:
            continue
        seq, code = int(m.group(1)), int(m.group(2))
        data = parse_hex_block(block)
        events.append({"seq": seq, "code": code, "name": TX_NAMES.get(code, "?"), "decoded": decode_payload(code, data)})

    print(f"events={len(events)}")
    for code, cnt in sorted(Counter(e["code"] for e in events).items()):
        print(f"  TX{code:2d} {TX_NAMES.get(code,'?'):16s} {cnt}")

    sigs: dict[int, dict[str, list[int]]] = defaultdict(lambda: defaultdict(list))
    for e in events:
        sigs[e["code"]][json.dumps(e["decoded"], sort_keys=True)].append(e["seq"])

    print("\nunique payloads:")
    for code in sorted(sigs):
        print(f"  TX{code}: {len(sigs[code])} unique")
        for k, seqs in sigs[code].items():
            print(f"    x{len(seqs)} {seqs}: {k}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
