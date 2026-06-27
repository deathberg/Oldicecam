#!/usr/bin/env python3
"""Deep native library analysis for IceCam APK."""
from __future__ import annotations

import json
import re
import struct
import subprocess
import sys
from collections import Counter, defaultdict
from pathlib import Path

try:
    import lief
except ImportError:
    lief = None

try:
    from capstone import CS_ARCH_ARM, CS_ARCH_ARM64, CS_MODE_ARM, CS_MODE_LITTLE_ENDIAN, Cs
except ImportError:
    Cs = None

LIBS = [
    ("arm64-v8a", "vcplax.so"),
    ("arm64-v8a", "libvc.so"),
    ("arm64-v8a", "libshadowhook.so"),
    ("armeabi-v7a", "vcplax.so"),
    ("armeabi-v7a", "libvc.so"),
    ("armeabi-v7a", "libshadowhook.so"),
]

ROOT = Path("/workspace/decompiled/raw/lib")
OUT = Path("/workspace/docs/native_analysis")
OUT.mkdir(parents=True, exist_ok=True)

INTEREST_RE = re.compile(
    r"(binder|camera|mediacodec|ffmpeg|shadow|hook|graphic|/data/|vcplax|libvc|"
    r"service|transact|parcel|inject|replace|rtmp|h264|hevc|yuv|egl|gles|"
    r"IMyBinder|vlive|JNI|Java_|register|codec|stream|surface|buffer)",
    re.I,
)


def run(cmd: list[str]) -> str:
    try:
        return subprocess.check_output(cmd, stderr=subprocess.STDOUT, text=True, errors="replace")
    except subprocess.CalledProcessError as e:
        return e.output or str(e)


def strings_extract(path: Path) -> list[str]:
    raw = path.read_bytes()
    out: list[str] = []
    cur = bytearray()
    for b in raw:
        if 32 <= b < 127:
            cur.append(b)
        else:
            if len(cur) >= 4:
                out.append(cur.decode("ascii", errors="ignore"))
            cur.clear()
    if len(cur) >= 4:
        out.append(cur.decode("ascii", errors="ignore"))
    return out


def readelf_summary(path: Path) -> dict:
    d = {"path": str(path)}
    d["file"] = run(["file", str(path)]).strip()
    d["build_id"] = run(["readelf", "-n", str(path)])
    d["needed"] = [
        ln.split("[", 1)[1].rstrip("]")
        for ln in run(["readelf", "-d", str(path)]).splitlines()
        if "(NEEDED)" in ln and "[" in ln
    ]
    d["soname"] = [
        ln.split("[", 1)[1].rstrip("]")
        for ln in run(["readelf", "-d", str(path)]).splitlines()
        if "(SONAME)" in ln and "[" in ln
    ]
    d["dynamic_symbols"] = []
    for ln in run(["readelf", "--dyn-syms", "-W", str(path)]).splitlines():
        parts = ln.split()
        if len(parts) >= 8 and parts[-1] not in ("Name", "LOCAL", "GLOBAL", "WEAK"):
            sym = parts[-1]
            if sym and sym != "Name":
                d["dynamic_symbols"].append(sym)
    d["sections"] = []
    for ln in run(["readelf", "-S", "-W", str(path)]).splitlines()[3:]:
        parts = ln.split()
        if len(parts) >= 2:
            d["sections"].append({"name": parts[1], "size": parts[-1] if parts else "?"})
    return d


def lief_analysis(path: Path, abi: str) -> dict:
    if lief is None:
        return {"error": "lief not installed"}
    b = lief.parse(str(path))
    if b is None:
        return {"error": "parse failed"}
    info = {
        "format": str(b.format),
        "entrypoint": hex(b.entrypoint) if hasattr(b, "entrypoint") else None,
        "exported_functions": [],
        "imported_functions": [],
        "libraries": list(getattr(b, "libraries", []) or []),
        "jni_onload": None,
        "interesting_exports": [],
    }
    for sym in b.dynamic_symbols:
        name = sym.name
        if not name:
            continue
        if sym.exported:
            info["exported_functions"].append(name)
            if INTEREST_RE.search(name):
                info["interesting_exports"].append(name)
        if sym.imported:
            info["imported_functions"].append(name)
    info["imported_functions"] = sorted(set(info["imported_functions"]))
    info["exported_functions"] = sorted(set(info["exported_functions"]))
    for name in info["exported_functions"]:
        if name == "JNI_OnLoad":
            info["jni_onload"] = name
    # Capstone disassemble entry / JNI if possible
    if Cs is not None and hasattr(b, "entrypoint") and b.entrypoint:
        arch = CS_ARCH_ARM64 if "arm64" in abi else CS_ARCH_ARM
        mode = CS_MODE_LITTLE_ENDIAN | (CS_MODE_ARM if "v7a" in abi else 0)
        md = Cs(arch, mode)
        md.detail = True
        sec = None
        for s in b.sections:
            if s.name == ".text" or (hasattr(s, "flags") and s.size > 0):
                sec = s
                break
        if sec and sec.content:
            base = sec.virtual_address
            ep_off = b.entrypoint - base if b.entrypoint >= base else 0
            chunk = bytes(sec.content[ep_off : ep_off + 256])
            info["entry_disasm"] = [
                f"0x{ins.address:x}: {ins.mnemonic} {ins.op_str}"
                for ins in md.disasm(chunk, b.entrypoint)
            ][:40]
    return info


def objdump_plt_grep(path: Path) -> dict:
    out = run(["objdump", "-T", str(path)])
    imports = Counter()
    for ln in out.splitlines():
        if " UND " in ln:
            parts = ln.split()
            if parts:
                imports[parts[-1]] += 1
    top_imports = imports.most_common(80)
    # Disassemble start of .text for main (limited)
    dis = run(["objdump", "-d", "--no-show-raw-insn", str(path)])
    main_chunk = []
    in_text = False
    count = 0
    for ln in dis.splitlines():
        if "<.text>:" in ln or "Disassembly of section .text" in ln:
            in_text = True
            continue
        if in_text:
            if ln.startswith("Disassembly of section"):
                break
            if ln.strip():
                main_chunk.append(ln)
                count += 1
                if count >= 60:
                    break
    return {"top_undefined_symbols": top_imports, "text_head_disasm": main_chunk}


def categorize_strings(strings: list[str]) -> dict:
    cats = defaultdict(list)
    for s in strings:
        sl = s.lower()
        if INTEREST_RE.search(s):
            if "/data/" in sl or s.startswith("/"):
                cats["paths"].append(s)
            elif "java_" in sl or "jni" in sl:
                cats["jni"].append(s)
            elif any(x in sl for x in ("error", "fail", "warn", "invalid")):
                cats["errors"].append(s)
            elif any(x in sl for x in ("codec", "h264", "hevc", "ffmpeg", "mediacodec", "rtmp")):
                cats["media"].append(s)
            elif any(x in sl for x in ("binder", "parcel", "transact", "service")):
                cats["binder"].append(s)
            elif any(x in sl for x in ("hook", "shadow", "camera", "graphic")):
                cats["hook_camera"].append(s)
            else:
                cats["other_interesting"].append(s)
    return {k: sorted(set(v))[:200] for k, v in cats.items()}


def find_string_xrefs_approx(path: Path, needle: bytes, max_hits=20) -> list[int]:
    data = path.read_bytes()
    hits = []
    start = 0
    while True:
        i = data.find(needle, start)
        if i < 0:
            break
        hits.append(i)
        start = i + 1
        if len(hits) >= max_hits:
            break
    return hits


def main():
    report = {"libraries": {}}
    for abi, name in LIBS:
        path = ROOT / abi / name
        if not path.exists():
            continue
        key = f"{abi}/{name}"
        print(f"Analyzing {key}...", file=sys.stderr)
        strs = strings_extract(path)
        interesting = [s for s in strs if INTEREST_RE.search(s)]
        entry = {
            "size_bytes": path.stat().st_size,
            "readelf": readelf_summary(path),
            "lief": lief_analysis(path, abi),
            "objdump": objdump_plt_grep(path),
            "string_count": len(strs),
            "interesting_string_count": len(interesting),
            "categorized_strings": categorize_strings(strs),
            "string_xrefs": {},
        }
        for needle in [b"IMyBinderService", b"/data/vcplax", b"libvc.so", b"shadowhook", b"FFmpeg", b"AMediaCodec"]:
            off = find_string_xrefs_approx(path, needle)
            if off:
                entry["string_xrefs"][needle.decode()] = [hex(x) for x in off]
        report["libraries"][key] = entry

    json_path = OUT / "native_deep_report.json"
    json_path.write_text(json.dumps(report, indent=2, ensure_ascii=False))
    print(f"Wrote {json_path}", file=sys.stderr)

    # Markdown summary
    md = ["# Native deep analysis (automated)\n"]
    for key, e in report["libraries"].items():
        md.append(f"## {key}\n")
        md.append(f"- Size: {e['size_bytes']:,} bytes")
        md.append(f"- Strings: {e['string_count']} total, {e['interesting_string_count']} interesting")
        re_info = e["readelf"]
        md.append(f"- Type: `{re_info.get('file', '')}`")
        md.append(f"- NEEDED: `{', '.join(re_info.get('needed', []))}`")
        if e["lief"].get("entrypoint"):
            md.append(f"- Entry: `{e['lief']['entrypoint']}`")
        imp = e["lief"].get("imported_functions") or []
        md.append(f"- Imported symbols: **{len(imp)}**")
        top_imp = [x[0] for x in e["objdump"].get("top_undefined_symbols", [])[:25]]
        if top_imp:
            md.append("- Top imports: " + ", ".join(f"`{x}`" for x in top_imp))
        exp = e["lief"].get("interesting_exports") or []
        if exp:
            md.append("- Interesting exports: " + ", ".join(f"`{x}`" for x in exp[:30]))
        for cat, items in e["categorized_strings"].items():
            if items:
                md.append(f"\n### Strings: {cat} ({len(items)})\n")
                for s in items[:35]:
                    md.append(f"- `{s[:200]}`")
        xrefs = e.get("string_xrefs") or {}
        if xrefs:
            md.append("\n### String file offsets\n")
            for k, v in xrefs.items():
                md.append(f"- `{k}`: {', '.join(v)}")
        md.append("\n---\n")

    md_path = OUT / "NATIVE_DEEP_ANALYSIS.md"
    md_path.write_text("\n".join(md), encoding="utf-8")
    print(f"Wrote {md_path}", file=sys.stderr)


if __name__ == "__main__":
    main()
