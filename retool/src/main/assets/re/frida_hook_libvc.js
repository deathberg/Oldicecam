// vcplax native instrumentation: Binder sniffer + TX13 delta + libvc XOR symbol capture.
// Attach:  frida-inject -p $(pidof vcplax) -s frida_hook_libvc.js --runtime=qjs
// Spawn:   see frida_spawn_vcplax.sh (hooks dlopen before libvc init)
'use strict';

var CONFIG = {
  ONTRANSACT_OFFSET: ptr('0x43f8b4'),
  GHIDRA_IMAGE_BASE: ptr('0x100000'),      // libvc.so Ghidra base → fileOff = VA - base
  LIBVC_INIT_OFFSET: ptr('0x774b0'),       // file offset of init() in libvc.so (arm64)
  DUMP_LEN: 128,
  MAX_INTS: 12,
  DESCRIPTOR: 'com.xiaomi.vlive.IMyBinderService',

  // TX13: no full hexdump blocks; log int[5] reply only when values change
  SUPPRESS_POLL_FULL: true,
  POLL_PARSE_REPLY: true,
  POLL_LOG_DELTA_ONLY: true,
  POLL_SUMMARY_EVERY: 60,

  // XOR / hook symbol capture
  HOOK_DLOPEN: true,
  HOOK_DLSYM: true,
  HOOK_SHADOWHOOK: true,
  HOOK_LIBVC_INIT: true,
  DUMP_XOR_RODATA: true,
  XOR_BLOB_OFFSETS: [
    { off: ptr('0x14ae86'), len: 0x13, keyOff: 0x08, name: 'lib_name' },
    { off: ptr('0x14ae99'), len: 0x9f, keyOff: 0x7d, name: 'sym_1' },
    { off: ptr('0x14af38'), len: 0x9d, keyOff: 0x7f, name: 'sym_2' },
    { off: ptr('0x14afd5'), len: 0x99, keyOff: 0x29, name: 'sym_3' },
    { off: ptr('0x14b06e'), len: 0x34, keyOff: 0x4f, name: 'sym_4' },
    { off: ptr('0x14b0a2'), len: 0x28, keyOff: 0x44, name: 'sym_5' }
  ],

  FULL_BLOCK_CODES: null
};

var TX_NAMES = {
  11: 'PLAY_SOURCE', 12: 'STOP_OR_QUERY', 13: 'POLL_STATE', 14: 'SET_MODE',
  15: 'GET_STATUS', 16: 'SET_AUTO_ROTATE', 17: 'SET_LOOP', 18: 'SET_ANGLE',
  19: 'SET_MIRROR', 22: 'SEEK_RANGE', 24: 'TRANSFORM', 25: 'HARD_RECOVERY'
};

var TX_JAVA_HINT = {
  11: 'writeString path + int0 + loop',
  12: 'empty + reply int',
  13: 'empty IN -> int[5] OUT (pipeline counters)',
  14: 'writeInt mode + writeString path',
  15: 'empty -> status int (5=playing)',
  16: 'writeInt bool loop-rotate flag',
  17: 'writeInt bool loop',
  18: 'writeInt degrees (0/90/180/270)',
  19: 'writeInt bool mirror',
  22: 'writeLong beginUs + writeLong endUs',
  24: 'int mode + 4x float + int colorMode',
  25: 'empty -> toggle recovery (reply void)'
};

var PARCEL_LAYOUTS = [
  { data: 0x08, size: 0x10, pos: 0x20, cap: 0x18 },
  { data: 0x10, size: 0x18, pos: 0x28, cap: 0x20 },
  { data: 0x00, size: 0x08, pos: 0x10, cap: 0x0c }
];

var seq = 0;
var pollCount = 0;
var pollSummaryLast = 0;
var lastPollCounters = null;
var shadowhookInstalled = false;
var libvcInitHooked = false;
var onTransactInstalled = false;
var libvcHookStatePtr = null;

function log(msg) { console.log(msg); }

function line(ch, n) {
  var s = '';
  for (var i = 0; i < n; i++) s += ch;
  return s;
}

function txName(code) { return TX_NAMES[code] || ('UNKNOWN_' + code); }

function readCppString(p) {
  if (p.isNull()) return '<null>';
  try {
    var tag = p.readU8();
    if ((tag & 1) === 0) return p.add(1).readUtf8String();
    var len = p.add(8).readU64();
    var data = p.add(16).readPointer();
    if (data.isNull()) return '<empty>';
    return data.readUtf8String(Number(len));
  } catch (e) {
    return '<err:' + e + '>';
  }
}

function findModule(namePart) {
  var found = null;
  Process.enumerateModules().forEach(function (m) {
    if (found) return;
    if (m.name.indexOf(namePart) !== -1 || m.path.indexOf(namePart) !== -1) found = m;
  });
  return found;
}

function u32(buf, off) {
  if (off + 4 > buf.length) return null;
  return (buf[off] & 0xff) | ((buf[off + 1] & 0xff) << 8) |
    ((buf[off + 2] & 0xff) << 16) | ((buf[off + 3] & 0xff) << 24);
}

function probeParcelLayout(parcelPtr) {
  if (parcelPtr.isNull()) return null;
  var i, lay, dataPtr, dataSize, dataPos, cap;
  for (i = 0; i < PARCEL_LAYOUTS.length; i++) {
    lay = PARCEL_LAYOUTS[i];
    try {
      dataPtr = parcelPtr.add(lay.data).readPointer();
      dataSize = parcelPtr.add(lay.size).readU64();
      dataPos = parcelPtr.add(lay.pos).readU64();
      if (lay.cap) cap = parcelPtr.add(lay.cap).readU64();
      if (dataPtr.isNull()) continue;
      if (Number(dataSize) === 0 || Number(dataSize) > 65536) continue;
      if (Number(dataPos) > Number(dataSize)) continue;
      dataPtr.readU8();
      return {
        layout: lay,
        dataPtr: dataPtr,
        dataSize: Number(dataSize),
        dataPos: Number(dataPos),
        dataCap: cap ? Number(cap) : 0
      };
    } catch (e) { /* next */ }
  }
  return null;
}

function readParcelBytes(info, fromPos, maxLen) {
  if (!info) return null;
  var start = fromPos !== undefined ? fromPos : info.dataPos;
  var avail = info.dataSize - start;
  if (avail <= 0) return new Uint8Array(0);
  var len = avail;
  if (maxLen !== undefined && len > maxLen) len = maxLen;
  try {
    return new Uint8Array(info.dataPtr.add(start).readByteArray(len));
  } catch (e) { return null; }
}

function parseReplyInt32Array(parcelPtr) {
  var info = probeParcelLayout(parcelPtr);
  if (!info) return null;
  var raw = readParcelBytes(info, 0, 64);
  if (!raw || raw.length < 8) return null;

  // Layout A: writeNoException(0) + writeInt32(len) + len × int32
  var off = 0;
  var exc = u32(raw, off);
  if (exc !== 0) off = 0;
  else off = 4;

  var len = u32(raw, off);
  if (len === null || len <= 0 || len > 32) {
    // Layout B: flat int32[5] without length prefix (fallback)
    var flat = [];
    var j;
    for (j = 0; j < 5; j++) {
      var v = u32(raw, off + j * 4);
      if (v === null) break;
      flat.push(v | 0);
    }
    return flat.length >= 5 ? flat : null;
  }

  var out = [];
  var k;
  for (k = 0; k < len; k++) {
    var val = u32(raw, off + 4 + k * 4);
    if (val === null) break;
    out.push(val | 0);
  }
  return out.length > 0 ? out : null;
}

function pollCountersChanged(a, b) {
  if (!a || !b || a.length !== b.length) return true;
  var i;
  for (i = 0; i < a.length; i++) if (a[i] !== b[i]) return true;
  return false;
}

function formatPollCounters(arr) {
  if (!arr) return '<parse_fail>';
  var labels = ['c0', 'c1', 'c2', 'c3', 'c4'];
  var parts = [];
  var i;
  for (i = 0; i < arr.length; i++) {
    parts.push((labels[i] || ('c' + i)) + '=' + arr[i] + '(0x' + (arr[i] >>> 0).toString(16) + ')');
  }
  return parts.join(' ');
}

function logPollDelta(counters, prev) {
  var delta = [];
  var i;
  if (prev && counters) {
    for (i = 0; i < counters.length; i++) {
      if (i >= prev.length || counters[i] !== prev[i]) {
        delta.push('c' + i + ':' + prev[i] + '->' + counters[i]);
      }
    }
  }
  log('[POLL_STATE] seq=' + pollCount + ' counters=[' + formatPollCounters(counters) + ']' +
      (delta.length ? ' DELTA {' + delta.join(', ') + '}' : ' (first sample)'));
}

function ghidraVaToFileOff(va) {
  return va.sub(CONFIG.GHIDRA_IMAGE_BASE);
}

function decodeXorString(base, blobVa, length, keyByte) {
  var out = [];
  var i, b, seed;
  var blobOff = ghidraVaToFileOff(blobVa);
  seed = 7;
  for (i = 0; i < length; i++) {
    b = seed ^ base.add(blobOff).add(i).readU8();
    seed = (seed + 0x1f) & 0xff;
    out.push((((i + b - 0x11) ^ (keyByte + i)) & 0xff));
  }
  try {
    return String.fromCharCode.apply(null, out);
  } catch (e) {
    return out.map(function (c) { return ('0' + c.toString(16)).slice(-2); }).join('');
  }
}

function dumpStaticXorTable(libvcBase, hookState) {
  if (!CONFIG.DUMP_XOR_RODATA || !libvcBase) return;
  // Keys live in LoadedLib / global hook state (PTR @ libvc+0x13a090), not in init code.
  var globalState = libvcBase.add(ghidraVaToFileOff(ptr('0x23a090')));
  CONFIG.XOR_BLOB_OFFSETS.forEach(function (entry) {
    var keyByte = 0;
    try {
      if (hookState && !hookState.isNull()) {
        keyByte = hookState.add(entry.keyOff).readU8();
      } else {
        keyByte = globalState.add(entry.keyOff).readU8();
      }
    } catch (e) { keyByte = 0; }
    var decoded = decodeXorString(libvcBase, entry.off, entry.len, keyByte);
    log('[XOR_STATIC] ' + entry.name + ' key@+' + entry.keyOff.toString(16) +
        '(0x' + keyByte.toString(16) + ') => "' + decoded + '"');
  });
}

function installLibvcInitHook(mod) {
  if (libvcInitHooked || !CONFIG.HOOK_LIBVC_INIT || !mod) return;
  var initAddr = mod.base.add(CONFIG.LIBVC_INIT_OFFSET);
  try {
    Interceptor.attach(initAddr, {
      onEnter: function (args) {
        this.ctx = args[0];
        log('[libvc init] enter ctx=' + this.ctx);
      },
      onLeave: function (retval) {
        log('[libvc init] leave ret=' + retval.toInt32());
        dumpStaticXorTable(mod.base, this.ctx);
      }
    });
    libvcInitHooked = true;
    log('[+] libvc init hook @ ' + initAddr);
  } catch (e) {
    log('[!] libvc init hook failed: ' + e);
  }
}

function installShadowhookHooks() {
  if (shadowhookInstalled) return;
  var hooked = 0;

  var hookSym = Module.findExportByName(null, 'shadowhook_hook_sym_name');
  if (hookSym && CONFIG.HOOK_SHADOWHOOK) {
    Interceptor.attach(hookSym, {
      onEnter: function (args) {
        var lib = readCppString(args[0]);
        var sym = readCppString(args[1]);
        log('[HOOK_SYM] lib="' + lib + '" sym="' + sym + '" replace=' + args[2]);
      }
    });
    hooked++;
  }

  var hookInit = Module.findExportByName(null, 'shadowhook_init');
  if (hookInit && CONFIG.HOOK_SHADOWHOOK) {
    Interceptor.attach(hookInit, {
      onEnter: function (args) {
        log('[shadowhook_init] mode=' + args[0] + ' debug=' + args[1]);
      }
    });
    hooked++;
  }

  if (hooked > 0) {
    shadowhookInstalled = true;
    log('[+] shadowhook hooks active (' + hooked + ')');
  }
}

function hookDlopen() {
  if (!CONFIG.HOOK_DLOPEN) return;
  ['dlopen', 'android_dlopen_ext'].forEach(function (name) {
    var addr = Module.findExportByName(null, name);
    if (!addr) return;
    Interceptor.attach(addr, {
      onEnter: function (args) {
        try { this.path = args[0].readCString(); } catch (e) { this.path = '?'; }
      },
      onLeave: function (retval) {
        if (retval.isNull() || !this.path) return;
        log('[dlopen] ' + this.path + ' => ' + retval);
        if (this.path.indexOf('libvc++.so') !== -1 || this.path.indexOf('shadowhook') !== -1) {
          installShadowhookHooks();
        }
        if (this.path.indexOf('libvc.so') !== -1) {
          var mod = Process.findModuleByName('libvc.so');
          if (mod) installLibvcInitHook(mod);
        }
      }
    });
    log('[+] hooked ' + name);
  });
}

function hookDlsym() {
  if (!CONFIG.HOOK_DLSYM) return;
  var addr = Module.findExportByName(null, 'dlsym');
  if (!addr) return;
  Interceptor.attach(addr, {
    onEnter: function (args) {
      try { this.sym = args[1].readCString(); } catch (e) { this.sym = '?'; }
    },
    onLeave: function (retval) {
      if (!this.sym || retval.isNull()) return;
      if (this.sym.indexOf('init') !== -1 || this.sym.indexOf('hook') !== -1) {
        log('[dlsym] sym="' + this.sym + '" => ' + retval);
      }
    }
  });
  log('[+] hooked dlsym');
}

function dumpParcelSide(label, parcelPtr, dumpLen) {
  var lines = [];
  if (parcelPtr.isNull()) {
    lines.push(label + ': <null>');
    return lines;
  }
  lines.push(label + '_ptr=' + parcelPtr);
  var info = probeParcelLayout(parcelPtr);
  if (!info) {
    lines.push(label + ': <layout probe failed>');
    return lines;
  }
  lines.push(label + '_meta size=' + info.dataSize + ' pos=' + info.dataPos);
  var rawAll = readParcelBytes(info, 0, dumpLen);
  if (rawAll) {
    lines.push(label + '_HEX_ALL (first ' + rawAll.length + ' bytes @0):');
    lines.push(hexdump(info.dataPtr, { offset: 0, length: rawAll.length, header: true, ansi: false }));
  }
  return lines;
}

function logTxBlock(ctx) {
  var border = line('=', 72);
  var mid = line('-', 72);
  log(border);
  log('[TX_START] seq=' + ctx.seq + ' code=' + ctx.code + ' (0x' + ctx.code.toString(16) + ') name=' + txName(ctx.code));
  log('[TX_HINT]  ' + (TX_JAVA_HINT[ctx.code] || ''));
  log('[TX_META]  flags=0x' + (ctx.flags >>> 0).toString(16));
  log(mid);
  var i;
  for (i = 0; i < ctx.dataLines.length; i++) log(ctx.dataLines[i]);
  log(mid);
  log('[TX_END]   seq=' + ctx.seq + ' retval=' + ctx.retval);
  for (i = 0; i < ctx.replyLines.length; i++) log(ctx.replyLines[i]);
  log(border);
  log('');
}

function onTransactHandler(args) {
  var code = args[1].toInt32();
  this._code = code;
  this._reply = args[3];

  if (code === 13 && CONFIG.SUPPRESS_POLL_FULL) {
    pollCount++;
    this._pollOnly = true;
    if (pollCount - pollSummaryLast >= CONFIG.POLL_SUMMARY_EVERY) {
      pollSummaryLast = pollCount;
      log('[POLL_SUMMARY] TX13 ticks=' + pollCount + ' (~1Hz App.java pollState)');
    }
    return;
  }

  if (code < 0x0b || code > 0x19) return;

  this._pollOnly = false;
  this._seq = ++seq;
  this._flags = args[4].toInt32();
  this._dataLines = dumpParcelSide('DATA', args[2], CONFIG.DUMP_LEN);
}

function onTransactLeave(retval) {
  if (this._code === 13 && this._pollOnly) {
    if (CONFIG.POLL_PARSE_REPLY) {
      var counters = parseReplyInt32Array(this._reply);
      if (counters) {
        if (!CONFIG.POLL_LOG_DELTA_ONLY || pollCountersChanged(counters, lastPollCounters)) {
          logPollDelta(counters, lastPollCounters);
          lastPollCounters = counters.slice();
        }
      }
    }
    return;
  }
  if (this._seq === undefined) return;
  logTxBlock({
    seq: this._seq,
    code: this._code,
    flags: this._flags,
    retval: retval.toInt32(),
    dataLines: this._dataLines,
    replyLines: dumpParcelSide('REPLY', this._reply, CONFIG.DUMP_LEN)
  });
}

function installVcplaxOnTransact() {
  if (onTransactInstalled) return;
  var mod = findModule('vcplax');
  if (!mod) return;
  var addr = mod.base.add(CONFIG.ONTRANSACT_OFFSET);
  Interceptor.attach(addr, { onEnter: onTransactHandler, onLeave: onTransactLeave });
  onTransactInstalled = true;
  log('[+] hook vcplax onTransact @ ' + addr + ' (base ' + mod.base + ')');
}

function bootstrapLateAttach() {
  logModuleScan();
  var libvc = Process.findModuleByName('libvc.so');
  var vcplax = findModule('vcplax');
  if (libvc) {
    installLibvcInitHook(libvc);
    if (CONFIG.DUMP_XOR_RODATA) {
      log('[!] libvc already loaded — trying static XOR dump');
      dumpStaticXorTable(libvc.base, libvc.base);
    }
  } else if (vcplax && CONFIG.DUMP_XOR_RODATA) {
    log('[!] no libvc.so module in maps (dlopen+unlink) — skip runtime XOR');
    log('[!] offline: python3 tools/decode_libvc_xor.py on pulled libvc.so.real');
  }
  installShadowhookHooks();
  installVcplaxOnTransact();
}

function logModuleScan() {
  log('[MODULE_SCAN] pid=' + Process.id);
  Process.enumerateModules().forEach(function (m) {
    if (m.path.indexOf('/data/') !== -1 ||
        m.name.indexOf('vc') !== -1 ||
        m.name.indexOf('shadow') !== -1 ||
        m.path.indexOf('deleted') !== -1) {
      log('[MODULE] ' + m.name + ' base=' + m.base + ' size=' + m.size + ' path=' + m.path);
    }
  });
}

log('=== vcplax Frida toolkit v3 (Binder + TX13 + XOR symbols) ===');
log('arch=' + Process.arch + ' pid=' + Process.id);
log('TX13 delta=' + CONFIG.POLL_LOG_DELTA_ONLY + ' XOR=' + CONFIG.DUMP_XOR_RODATA);

hookDlopen();
hookDlsym();
bootstrapLateAttach();

log('=== ready ===');
