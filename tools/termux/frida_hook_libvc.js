// Advanced native Binder sniffer for vcplax (NO Java / NO Java.perform).
// QuickJS: frida-inject -p $(pidof vcplax) -s frida_hook_libvc.js --runtime=qjs
'use strict';

// ─── config ───────────────────────────────────────────────────────────────
var CONFIG = {
  ONTRANSACT_OFFSET: ptr('0x43f8b4'),
  DUMP_LEN: 128,
  MAX_INTS: 12,
  // TX13 = pollState @ ~1 Hz from App.java — suppress full blocks, show summary
  SUPPRESS_POLL_FULL: true,
  POLL_SUMMARY_EVERY: 20,
  // Log full [TX_START]..[TX_END] for these; empty = all non-poll
  FULL_BLOCK_CODES: null,
  DESCRIPTOR: 'com.xiaomi.vlive.IMyBinderService'
};

var TX_NAMES = {
  11: 'PLAY_SOURCE',
  12: 'STOP_OR_QUERY',
  13: 'POLL_STATE',
  14: 'SET_MODE',
  15: 'GET_STATUS',
  16: 'SET_AUTO_ROTATE',
  17: 'SET_LOOP',
  18: 'SET_ANGLE',
  19: 'SET_MIRROR',
  22: 'SEEK_RANGE',
  24: 'TRANSFORM',
  25: 'HARD_RECOVERY'
};

var TX_JAVA_HINT = {
  11: 'writeString path + int0 + loop',
  12: 'empty + reply int',
  13: 'empty IN -> int[5] OUT (daemon counters)',
  14: 'writeInt mode + writeString path',
  15: 'empty -> status int (5=playing)',
  16: 'writeInt bool loop-rotate flag',
  17: 'writeInt bool loop',
  18: 'writeInt degrees (0/90/180/270)',
  19: 'writeInt bool mirror',
  22: 'writeLong beginUs + writeLong endUs',
  24: 'int mode + 4x float + int colorMode',
  25: 'empty -> toggle recovery'
};

// arm64 Parcel layout candidates (Android 11–14)
var PARCEL_LAYOUTS = [
  { data: 0x08, size: 0x10, pos: 0x20, cap: 0x18 },
  { data: 0x10, size: 0x18, pos: 0x28, cap: 0x20 },
  { data: 0x00, size: 0x08, pos: 0x10, cap: 0x0c }
];

var seq = 0;
var pollCount = 0;
var pollSummaryLast = 0;
var shadowhookInstalled = false;
var onTransactInstalled = false;

function log(msg) {
  console.log(msg);
}

function line(ch, n) {
  var s = '';
  for (var i = 0; i < n; i++) s += ch;
  return s;
}

function txName(code) {
  return TX_NAMES[code] || ('UNKNOWN_' + code);
}

function shouldFullBlock(code) {
  if (code === 13 && CONFIG.SUPPRESS_POLL_FULL) return false;
  if (CONFIG.FULL_BLOCK_CODES && CONFIG.FULL_BLOCK_CODES.indexOf(code) === -1) return false;
  return true;
}

function readCppString(p) {
  if (p.isNull()) return '<null>';
  try {
    var tag = p.readU8();
    if ((tag & 1) === 0) {
      return p.add(1).readUtf8String();
    }
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
    if (m.name.indexOf(namePart) !== -1 || m.path.indexOf(namePart) !== -1) {
      found = m;
    }
  });
  return found;
}

function u32(buf, off) {
  if (off + 4 > buf.length) return null;
  return (buf[off] & 0xff) |
    ((buf[off + 1] & 0xff) << 8) |
    ((buf[off + 2] & 0xff) << 16) |
    ((buf[off + 3] & 0xff) << 24);
}

function u64(buf, off) {
  if (off + 8 > buf.length) return null;
  var lo = u32(buf, off);
  var hi = u32(buf, off + 4);
  if (lo === null || hi === null) return null;
  return hi * 0x100000000 + (lo >>> 0);
}

function isPrintableAscii(c) {
  return c >= 0x20 && c <= 0x7e;
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
      // validate: read first bytes
      dataPtr.readU8();
      return {
        layout: lay,
        dataPtr: dataPtr,
        dataSize: Number(dataSize),
        dataPos: Number(dataPos),
        dataCap: cap ? Number(cap) : 0
      };
    } catch (e) {
      /* try next layout */
    }
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
  } catch (e) {
    return null;
  }
}

function parseStrictModeString16(buf, off) {
  if (!buf || off + 8 > buf.length) return null;
  var strict = u32(buf, off);
  var charLen = u32(buf, off + 4);
  if (charLen === null || charLen <= 0 || charLen > 512) return null;
  var byteLen = charLen * 2;
  var start = off + 8;
  if (start + byteLen > buf.length) return null;
  var s = '';
  var i;
  for (i = 0; i < charLen; i++) {
    var lo = buf[start + i * 2];
    var hi = buf[start + i * 2 + 1];
    if (hi !== 0) break;
    if (!isPrintableAscii(lo)) break;
    s += String.fromCharCode(lo);
  }
  var padded = byteLen;
  if (padded % 4 !== 0) padded += 4 - (padded % 4);
  return {
    strict: strict,
    text: s,
    nextOff: start + padded
  };
}

function scanAsciiRuns(buf, minLen) {
  minLen = minLen || 4;
  var out = [];
  if (!buf) return out;
  var i = 0, run = '';
  while (i < buf.length) {
    if (isPrintableAscii(buf[i])) {
      run += String.fromCharCode(buf[i]);
    } else {
      if (run.length >= minLen) out.push(run);
      run = '';
    }
    i++;
  }
  if (run.length >= minLen) out.push(run);
  return out;
}

function scanUtf16LeStrings(buf, minChars) {
  minChars = minChars || 4;
  var out = [];
  if (!buf || buf.length < 8) return out;
  var i = 0;
  while (i + 4 <= buf.length) {
    var charLen = u32(buf, i);
    if (charLen !== null && charLen >= minChars && charLen < 256) {
      var need = 4 + charLen * 2;
      if (i + need <= buf.length) {
        var s = '';
        var ok = true, j;
        for (j = 0; j < charLen; j++) {
          var lo = buf[i + 4 + j * 2];
          var hi = buf[i + 4 + j * 2 + 1];
          if (hi !== 0 || !isPrintableAscii(lo)) { ok = false; break; }
          s += String.fromCharCode(lo);
        }
        if (ok && s.length >= minChars) {
          out.push('@' + i + ':' + s);
          i += need;
          if (i % 4 !== 0) i += 4 - (i % 4);
          continue;
        }
      }
    }
    i += 4;
  }
  return out;
}

function formatInts(buf, startOff, count) {
  var ints = [];
  var i, v;
  if (!buf) return ints;
  for (i = 0; i < count; i++) {
    v = u32(buf, startOff + i * 4);
    if (v === null) break;
    ints.push('0x' + (v >>> 0).toString(16) + '(' + (v | 0) + ')');
  }
  return ints;
}

function formatFloatsFromInts(buf, startOff, count) {
  var out = [];
  var i, bits;
  if (!buf) return out;
  for (i = 0; i < count; i++) {
    bits = u32(buf, startOff + i * 4);
    if (bits === null) break;
    // QJS-safe: show hex bits; user can decode offline
    out.push('f32_bits=0x' + (bits >>> 0).toString(16));
  }
  return out;
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
    try {
      lines.push(hexdump(parcelPtr, { offset: 0, length: 64, header: true, ansi: false }));
    } catch (e2) {
      lines.push(label + ': hexdump err ' + e2);
    }
    return lines;
  }
  lines.push(label + '_meta size=' + info.dataSize + ' pos=' + info.dataPos + ' cap=' + info.dataCap);
  var rawAll = readParcelBytes(info, 0, dumpLen);
  var rawPayload = readParcelBytes(info, info.dataPos, dumpLen);
  if (rawAll) {
    lines.push(label + '_HEX_ALL (first ' + rawAll.length + ' bytes @0):');
    lines.push(hexdump(info.dataPtr, { offset: 0, length: rawAll.length, header: true, ansi: false }));
  }
  if (rawPayload && info.dataPos > 0) {
    lines.push(label + '_HEX_PAYLOAD (from pos ' + info.dataPos + '):');
    lines.push(hexdump(info.dataPtr.add(info.dataPos), {
      offset: 0, length: rawPayload.length, header: true, ansi: false
    }));
  }
  var tok = rawAll ? parseStrictModeString16(rawAll, 0) : null;
  if (tok) {
    lines.push(label + '_TOKEN strict=' + tok.strict + ' desc="' + tok.text + '"');
    if (tok.text.indexOf(CONFIG.DESCRIPTOR) !== -1) {
      lines.push(label + '_TOKEN ok IMyBinderService');
    }
    var payloadOff = tok.nextOff;
    var payload = readParcelBytes(info, payloadOff, dumpLen);
    if (payload && payload.length >= 4) {
      lines.push(label + '_INT32 @token_end: ' + formatInts(payload, 0, CONFIG.MAX_INTS).join(' '));
      if (payload.length >= 16) {
        lines.push(label + '_FLOAT_HINT: ' + formatFloatsFromInts(payload, 0, 4).join(' '));
      }
    }
  } else if (rawPayload) {
    lines.push(label + '_INT32 @pos: ' + formatInts(rawPayload, 0, CONFIG.MAX_INTS).join(' '));
  }
  var ascii = scanAsciiRuns(rawAll, 4);
  if (ascii.length) lines.push(label + '_ASCII: ' + ascii.join(' | '));
  var u16 = scanUtf16LeStrings(rawAll, 4);
  if (u16.length) lines.push(label + '_UTF16: ' + u16.join(' | '));
  return lines;
}

function logTxBlock(ctx) {
  var code = ctx.code;
  var name = txName(code);
  var hint = TX_JAVA_HINT[code] || '';
  var border = line('=', 72);
  var mid = line('-', 72);

  log(border);
  log('[TX_START] seq=' + ctx.seq + ' code=' + code + ' (0x' + code.toString(16) + ') name=' + name);
  log('[TX_HINT]  ' + hint);
  log('[TX_META]  flags=0x' + (ctx.flags >>> 0).toString(16) + ' this=' + ctx.thiz +
      ' data=' + ctx.data + ' reply=' + ctx.reply);
  log(mid);

  var i;
  for (i = 0; i < ctx.dataLines.length; i++) log(ctx.dataLines[i]);
  log(mid);
  log('[TX_END]   seq=' + ctx.seq + ' retval=' + ctx.retval +
      ' reply_summary=' + ctx.replySummary);
  for (i = 0; i < ctx.replyLines.length; i++) log(ctx.replyLines[i]);
  log(border);
  log('');
}

function summarizeReply(parcelPtr) {
  var info = probeParcelLayout(parcelPtr);
  if (!info) return 'probe_fail';
  var raw = readParcelBytes(info, info.dataPos, 64);
  if (!raw || raw.length === 0) return 'empty pos=' + info.dataPos;
  var ints = formatInts(raw, 0, 8);
  return 'size=' + info.dataSize + ' pos=' + info.dataPos + ' ints=' + ints.join(',');
}

function onTransactHandler(args) {
  var code = args[1].toInt32();
  var flags = args[4].toInt32();
  var thiz = args[0];
  var data = args[2];
  var reply = args[3];

  // Background poll flood — compact summary
  if (code === 13 && CONFIG.SUPPRESS_POLL_FULL) {
    pollCount++;
    if (pollCount - pollSummaryLast >= CONFIG.POLL_SUMMARY_EVERY) {
      pollSummaryLast = pollCount;
      log('[POLL_SUMMARY] TX13 count=' + pollCount + ' (App.java ~1Hz pollState, NOT button events)');
    }
    this._pollOnly = true;
    this._code = code;
    this._reply = reply;
    return;
  }

  if (code < 0x0b || code > 0x19) {
    return;
  }

  if (!shouldFullBlock(code)) {
    return;
  }

  this._pollOnly = false;
  this._seq = ++seq;
  this._code = code;
  this._flags = flags;
  this._thiz = thiz;
  this._data = data;
  this._reply = reply;
  this._t0 = Date.now();
  this._dataLines = dumpParcelSide('DATA', data, CONFIG.DUMP_LEN);
}

function onTransactLeave(retval) {
  if (this._pollOnly) {
    // optional: one-line poll reply every N
    return;
  }
  if (this._seq === undefined) return;

  var ctx = {
    seq: this._seq,
    code: this._code,
    flags: this._flags,
    thiz: this._thiz,
    data: this._data,
    reply: this._reply,
    retval: retval.toInt32(),
    dataLines: this._dataLines,
    replyLines: dumpParcelSide('REPLY', this._reply, CONFIG.DUMP_LEN),
    replySummary: summarizeReply(this._reply)
  };
  logTxBlock(ctx);
}

function installShadowhookHooks() {
  if (shadowhookInstalled) return;
  var hooked = 0;
  var hookSym = Module.findExportByName(null, 'shadowhook_hook_sym_name');
  if (hookSym) {
    Interceptor.attach(hookSym, {
      onEnter: function (args) {
        log('HOOK_SYM lib=' + readCppString(args[0]) +
            ' sym=' + readCppString(args[1]) + ' replace=' + args[2]);
      }
    });
    hooked++;
  }
  var hookInit = Module.findExportByName(null, 'shadowhook_init');
  if (hookInit) {
    Interceptor.attach(hookInit, {
      onEnter: function (args) {
        log('shadowhook_init mode=' + args[0] + ' debug=' + args[1]);
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
  ['dlopen', 'android_dlopen_ext'].forEach(function (name) {
    var addr = Module.findExportByName(null, name);
    if (!addr) return;
    Interceptor.attach(addr, {
      onEnter: function (args) {
        try { this.path = args[0].readCString(); } catch (e) { this.path = '?'; }
      },
      onLeave: function (retval) {
        if (retval.isNull() || !this.path) return;
        if (this.path.indexOf('libvc') !== -1 || this.path.indexOf('shadowhook') !== -1) {
          log('[dlopen] ' + this.path);
          installShadowhookHooks();
        }
      }
    });
  });
}

function installVcplaxOnTransact() {
  if (onTransactInstalled) return;
  var mod = findModule('vcplax');
  if (!mod) return;
  var addr = mod.base.add(CONFIG.ONTRANSACT_OFFSET);
  log('[+] hook vcplax onTransact @ ' + addr + ' (base ' + mod.base + ')');
  Interceptor.attach(addr, {
    onEnter: onTransactHandler,
    onLeave: onTransactLeave
  });
  onTransactInstalled = true;
}

// ─── native entry (no Java, no setImmediate) ───────────────────────────────
log('=== vcplax Advanced Binder Sniffer (native / qjs) ===');
log('arch=' + Process.arch + ' pid=' + Process.id);
log('poll suppress=' + CONFIG.SUPPRESS_POLL_FULL + ' dump_len=' + CONFIG.DUMP_LEN);

hookDlopen();
installShadowhookHooks();
installVcplaxOnTransact();

log('=== ready — each UI action should produce one [TX_START] block (except TX13 poll) ===');
