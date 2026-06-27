// frida_hook_libvc.js v4 — smart / self-healing / native-only (QuickJS)
//
// IMPORTANT: always use --runtime=qjs (never default V8/java-bridge on native vcplax)
//
// Attach daemon (Binder + libvc XOR):
//   PID=$(pidof vcplax) && /data/local/tmp/frida-inject -p "$PID" \
//     -s /data/local/tmp/frida_hook_libvc.js --runtime=qjs
//
// Attach app (passive profile — polls vcplax, no Java hooks):
//   PID=$(pidof com.potplayer.music) && /data/local/tmp/frida-inject -p "$PID" \
//     -s /data/local/tmp/frida_hook_libvc.js --runtime=qjs
//
// Spawn vcplax (best for HOOK_SYM before libvc init):
//   /data/local/tmp/frida_spawn_vcplax.sh
'use strict';

// ─── Config ───────────────────────────────────────────────────────────────────
var CONFIG = {
  TARGET_APP: 'com.potplayer.music',
  TARGET_DAEMON_NAMES: ['vcplax', '/data/vcplax'],

  POLL_MS: 2000,
  POLL_MAX_ATTEMPTS: 0, // 0 = forever

  ONTRANSACT_OFFSET: ptr('0x43f8b4'),
  LIBVC_INIT_OFFSET: ptr('0x774b0'),
  DUMP_LEN: 128,
  DESCRIPTOR: 'com.xiaomi.vlive.IMyBinderService',

  SUPPRESS_POLL_FULL: true,
  POLL_PARSE_REPLY: true,
  POLL_LOG_DELTA_ONLY: true,
  POLL_SUMMARY_EVERY: 60,

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
    { off: ptr('0x14b0a2'), len: 0x28, keyOff: 0x51, name: 'sym_5' }
  ]
};

var TX_NAMES = {
  11: 'PLAY_SOURCE', 12: 'STOP_OR_QUERY', 13: 'POLL_STATE', 14: 'SET_MODE',
  15: 'GET_STATUS', 16: 'SET_AUTO_ROTATE', 17: 'SET_LOOP', 18: 'SET_ANGLE',
  19: 'SET_MIRROR', 22: 'SEEK_RANGE', 24: 'TRANSFORM', 25: 'HARD_RECOVERY'
};

var PARCEL_LAYOUTS = [
  { data: 0x08, size: 0x10, pos: 0x20, cap: 0x18 },
  { data: 0x10, size: 0x18, pos: 0x28, cap: 0x20 },
  { data: 0x00, size: 0x08, pos: 0x10, cap: 0x0c }
];

// ─── Runtime state ────────────────────────────────────────────────────────────
var STATE = {
  profile: null,
  processName: '',
  tick: 0,
  hooks: {},
  seq: 0,
  pollCount: 0,
  pollSummaryLast: 0,
  lastPollCounters: null
};

// ─── Logging ─────────────────────────────────────────────────────────────────
function ts() {
  return new Date().toISOString().replace('T', ' ').replace('Z', '');
}

function log(level, msg) {
  console.log('[' + ts() + '][' + level + '][' + (STATE.profile || '?') + '] ' + msg);
}

function logInfo(m) { log('INFO', m); }
function logOk(m) { log('OK', m); }
function logWarn(m) { log('WARN', m); }
function logErr(m) { log('ERR', m); }

// ─── Process context ─────────────────────────────────────────────────────────
function readCmdline() {
  try {
    var f = new File('/proc/self/cmdline', 'r');
    var raw = f.read();
    f.close();
    if (!raw) return '';
    var parts = raw.split('\0');
    return parts[0] || raw.replace(/\0/g, ' ').trim().split(' ')[0];
  } catch (e) {
    return '';
  }
}

function basenamePath(p) {
  if (!p) return '';
  var i = p.lastIndexOf('/');
  return i >= 0 ? p.substring(i + 1) : p;
}

function detectProfile(procName) {
  if (!procName) return null;
  if (procName.indexOf(CONFIG.TARGET_APP) !== -1) return 'app';
  var base = basenamePath(procName);
  var i;
  for (i = 0; i < CONFIG.TARGET_DAEMON_NAMES.length; i++) {
    var dn = CONFIG.TARGET_DAEMON_NAMES[i];
    if (procName.indexOf(dn) !== -1 || base === basenamePath(dn)) return 'daemon';
  }
  if (base === 'vcplax') return 'daemon';
  return null;
}

function assertTargetProcess() {
  STATE.processName = readCmdline();
  STATE.profile = detectProfile(STATE.processName);
  logInfo('boot pid=' + Process.id + ' arch=' + Process.arch + ' cmdline="' + STATE.processName + '"');
  if (!STATE.profile) {
    logWarn('process not in allowlist — hooks disabled');
    logWarn('allowed: ' + CONFIG.TARGET_APP + ' | vcplax (/data/vcplax)');
    logWarn('inject with: frida-inject -p $(pidof vcplax) -s script.js --runtime=qjs');
    return false;
  }
  logOk('profile=' + STATE.profile + ' (native-only, no Java.perform)');
  return true;
}

// ─── Safe hook helper (resilience) ───────────────────────────────────────────
function safeAttach(name, address, callbacks) {
  if (STATE.hooks[name]) return true;
  if (!address || address.isNull()) return false;
  try {
    Interceptor.attach(address, callbacks);
    STATE.hooks[name] = true;
    logOk('hook installed: ' + name + ' @ ' + address);
    return true;
  } catch (e) {
    logErr('hook failed: ' + name + ' @ ' + address + ' — ' + e);
    return false;
  }
}

function findExport(moduleName, symbolName) {
  try {
    var addr = Module.findExportByName(moduleName, symbolName);
    if (addr && !addr.isNull()) return addr;
  } catch (e1) { /* ignore */ }
  try {
    var addr2 = Module.findExportByName(null, symbolName);
    if (addr2 && !addr2.isNull()) return addr2;
  } catch (e2) { /* ignore */ }
  return null;
}

function findModule(namePart) {
  var found = null;
  try {
    Process.enumerateModules().forEach(function (m) {
      if (found) return;
      if (m.name.indexOf(namePart) !== -1 || m.path.indexOf(namePart) !== -1) found = m;
    });
  } catch (e) {
    logErr('enumerateModules: ' + e);
  }
  return found;
}

function schedule(name, tryInstall, intervalMs) {
  var attempts = 0;
  var run = function () {
    STATE.tick++;
    if (STATE.hooks[name]) return;
    if (CONFIG.POLL_MAX_ATTEMPTS > 0 && attempts >= CONFIG.POLL_MAX_ATTEMPTS) {
      logWarn('giving up: ' + name + ' after ' + attempts + ' attempts');
      return;
    }
    attempts++;
    try {
      if (tryInstall()) {
        logOk('self-heal complete: ' + name + ' (attempt ' + attempts + ')');
        return;
      }
      if (attempts === 1 || attempts % 5 === 0) {
        logInfo('waiting for: ' + name + ' (attempt ' + attempts + ')');
      }
    } catch (e) {
      logErr('scheduler ' + name + ': ' + e);
    }
    setTimeout(run, intervalMs);
  };
  run();
}

// ─── Parcel / Binder helpers ─────────────────────────────────────────────────
function u32(buf, off) {
  if (!buf || off + 4 > buf.length) return null;
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
      return { dataPtr: dataPtr, dataSize: Number(dataSize), dataPos: Number(dataPos) };
    } catch (e) { /* try next layout */ }
  }
  return null;
}

function readParcelBytes(info, fromPos, maxLen) {
  if (!info) return null;
  var start = fromPos !== undefined ? fromPos : info.dataPos;
  var avail = info.dataSize - start;
  if (avail <= 0) return new Uint8Array(0);
  var len = maxLen !== undefined && avail > maxLen ? maxLen : avail;
  try {
    return new Uint8Array(info.dataPtr.add(start).readByteArray(len));
  } catch (e) { return null; }
}

function parseReplyInt32Array(parcelPtr) {
  var info = probeParcelLayout(parcelPtr);
  if (!info) return null;
  var raw = readParcelBytes(info, 0, 64);
  if (!raw || raw.length < 8) return null;
  var off = (u32(raw, 0) === 0) ? 4 : 0;
  var len = u32(raw, off);
  if (len === null || len <= 0 || len > 32) {
    var flat = [], j;
    for (j = 0; j < 5; j++) {
      var v = u32(raw, off + j * 4);
      if (v === null) break;
      flat.push(v | 0);
    }
    return flat.length >= 5 ? flat : null;
  }
  var out = [], k;
  for (k = 0; k < len; k++) {
    var val = u32(raw, off + 4 + k * 4);
    if (val === null) break;
    out.push(val | 0);
  }
  return out.length > 0 ? out : null;
}

function readCppString(p) {
  if (p.isNull()) return '<null>';
  try {
    var tag = p.readU8();
    if ((tag & 1) === 0) return p.add(1).readUtf8String();
    var len = p.add(8).readU64();
    var data = p.add(16).readPointer();
    if (data.isNull()) return '<empty>';
    return data.readUtf8String(Number(len));
  } catch (e) { return '<err>'; }
}

function dumpParcelSide(label, parcelPtr, dumpLen) {
  var lines = [];
  if (parcelPtr.isNull()) return [label + ': <null>'];
  var info = probeParcelLayout(parcelPtr);
  if (!info) return [label + ': layout probe failed'];
  lines.push(label + '_meta size=' + info.dataSize + ' pos=' + info.dataPos);
  var raw = readParcelBytes(info, 0, dumpLen);
  if (raw) {
    lines.push(label + '_HEX (first ' + raw.length + ' bytes):');
    lines.push(hexdump(info.dataPtr, { offset: 0, length: raw.length, header: true, ansi: false }));
  }
  return lines;
}

// ─── XOR decode (libvc static blobs) ─────────────────────────────────────────
function decodeXorString(base, blobOff, length, keyByte) {
  var out = [], i, b, seed = 7;
  for (i = 0; i < length; i++) {
    b = seed ^ base.add(blobOff).add(i).readU8();
    seed = (seed + 0x1f) & 0xff;
    out.push((((i + b - 0x11) ^ (keyByte + i)) & 0xff));
  }
  try { return String.fromCharCode.apply(null, out); }
  catch (e) { return out.join(','); }
}

function dumpStaticXorTable(libvcBase, hookState) {
  if (!CONFIG.DUMP_XOR_RODATA || !libvcBase) return;
  var keyBase = hookState && !hookState.isNull() ? hookState : libvcBase;
  CONFIG.XOR_BLOB_OFFSETS.forEach(function (entry) {
    try {
      var keyByte = keyBase.add(entry.keyOff).readU8();
      var decoded = decodeXorString(libvcBase, entry.off, entry.len, keyByte);
      logInfo('[XOR_STATIC] ' + entry.name + ' => "' + decoded + '"');
    } catch (e) {
      logErr('[XOR_STATIC] ' + entry.name + ': ' + e);
    }
  });
}

// ─── Daemon hooks (vcplax) ───────────────────────────────────────────────────
function installLibvcInitHook(mod) {
  if (STATE.hooks.libvc_init || !mod) return false;
  var initAddr = mod.base.add(CONFIG.LIBVC_INIT_OFFSET);
  return safeAttach('libvc_init', initAddr, {
    onEnter: function (args) {
      this.ctx = args[0];
      logInfo('libvc init enter ctx=' + this.ctx);
    },
    onLeave: function (retval) {
      logInfo('libvc init leave ret=' + retval.toInt32());
      dumpStaticXorTable(mod.base, this.ctx);
    }
  });
}

function installShadowhookHooks() {
  var n = 0;
  var hookSym = findExport(null, 'shadowhook_hook_sym_name');
  if (hookSym && CONFIG.HOOK_SHADOWHOOK && !STATE.hooks.shadowhook_sym) {
    if (safeAttach('shadowhook_hook_sym_name', hookSym, {
      onEnter: function (args) {
        logInfo('[HOOK_SYM] lib="' + readCppString(args[0]) + '" sym="' +
                readCppString(args[1]) + '" replace=' + args[2]);
      }
    })) n++;
  }
  var hookInit = findExport(null, 'shadowhook_init');
  if (hookInit && CONFIG.HOOK_SHADOWHOOK && !STATE.hooks.shadowhook_init) {
    if (safeAttach('shadowhook_init', hookInit, {
      onEnter: function (args) {
        logInfo('[shadowhook_init] mode=' + args[0] + ' debug=' + args[1]);
      }
    })) n++;
  }
  return n > 0;
}

function installDlopenHooks() {
  if (!CONFIG.HOOK_DLOPEN) return true;
  var names = ['android_dlopen_ext', 'dlopen'];
  var any = false;
  names.forEach(function (name) {
    if (STATE.hooks['dlopen_' + name]) return;
    var addr = findExport(null, name);
    if (!addr) return;
    if (safeAttach('dlopen_' + name, addr, {
      onEnter: function (args) {
        try { this.path = args[0].readCString(); } catch (e) { this.path = '?'; }
      },
      onLeave: function (retval) {
        if (retval.isNull() || !this.path) return;
        logInfo('[dlopen] ' + this.path);
        if (this.path.indexOf('libvc++.so') !== -1 || this.path.indexOf('shadowhook') !== -1) {
          installShadowhookHooks();
        }
        if (this.path.indexOf('libvc.so') !== -1) {
          var mod = findModule('libvc.so');
          if (mod) installLibvcInitHook(mod);
        }
      }
    })) any = true;
  });
  return any;
}

function installDlsymHook() {
  if (!CONFIG.HOOK_DLSYM || STATE.hooks.dlsym) return true;
  var addr = findExport(null, 'dlsym');
  if (!addr) return false;
  return safeAttach('dlsym', addr, {
    onEnter: function (args) {
      try { this.sym = args[1].readCString(); } catch (e) { this.sym = '?'; }
    },
    onLeave: function (retval) {
      if (!this.sym || retval.isNull()) return;
      if (this.sym.indexOf('init') !== -1 || this.sym.indexOf('hook') !== -1) {
        logInfo('[dlsym] ' + this.sym + ' => ' + retval);
      }
    }
  });
}

function installOnTransactHook() {
  if (STATE.hooks.onTransact) return true;
  var mod = findModule('vcplax');
  if (!mod) return false;
  var addr = mod.base.add(CONFIG.ONTRANSACT_OFFSET);
  return safeAttach('onTransact', addr, {
    onEnter: onTransactEnter,
    onLeave: onTransactLeave
  });
}

function onTransactEnter(args) {
  var code = args[1].toInt32();
  this._code = code;
  this._reply = args[3];

  if (code === 13 && CONFIG.SUPPRESS_POLL_FULL) {
    STATE.pollCount++;
    this._pollOnly = true;
    if (STATE.pollCount - STATE.pollSummaryLast >= CONFIG.POLL_SUMMARY_EVERY) {
      STATE.pollSummaryLast = STATE.pollCount;
      logInfo('[POLL_SUMMARY] TX13 ticks=' + STATE.pollCount);
    }
    return;
  }
  if (code < 0x0b || code > 0x19) return;

  this._pollOnly = false;
  this._seq = ++STATE.seq;
  this._flags = args[4].toInt32();
  this._dataLines = dumpParcelSide('DATA', args[2], CONFIG.DUMP_LEN);
}

function onTransactLeave(retval) {
  if (this._code === 13 && this._pollOnly) {
    if (CONFIG.POLL_PARSE_REPLY) {
      try {
        var counters = parseReplyInt32Array(this._reply);
        if (counters) {
          var changed = !STATE.lastPollCounters;
          if (!changed) {
            var i;
            for (i = 0; i < counters.length; i++) {
              if (STATE.lastPollCounters[i] !== counters[i]) { changed = true; break; }
            }
          }
          if (!CONFIG.POLL_LOG_DELTA_ONLY || changed) {
            logInfo('[POLL_STATE] #' + STATE.pollCount + ' ' + counters.join(','));
            STATE.lastPollCounters = counters.slice();
          }
        }
      } catch (e) { logErr('TX13 parse: ' + e); }
    }
    return;
  }
  if (this._seq === undefined) return;
  var i;
  logInfo('=== TX seq=' + this._seq + ' code=' + this._code + ' ' +
          (TX_NAMES[this._code] || '?') + ' retval=' + retval.toInt32() + ' ===');
  for (i = 0; i < this._dataLines.length; i++) logInfo(this._dataLines[i]);
  var rlines = dumpParcelSide('REPLY', this._reply, CONFIG.DUMP_LEN);
  for (i = 0; i < rlines.length; i++) logInfo(rlines[i]);
}

function bootstrapDaemonProfile() {
  logInfo('stage: daemon profile — scheduling self-healing hooks');

  schedule('dlopen', installDlopenHooks, CONFIG.POLL_MS);
  schedule('dlsym', installDlsymHook, CONFIG.POLL_MS);
  schedule('shadowhook', installShadowhookHooks, CONFIG.POLL_MS);
  schedule('onTransact', installOnTransactHook, CONFIG.POLL_MS);

  schedule('libvc_init', function () {
    var mod = findModule('libvc.so');
    if (!mod) return false;
    if (STATE.hooks.libvc_init) return true;
    if (installLibvcInitHook(mod)) return true;
    if (CONFIG.DUMP_XOR_RODATA) {
      logWarn('libvc already loaded — XOR keys may be stale; prefer spawn via frida_spawn_vcplax.sh');
      dumpStaticXorTable(mod.base, mod.base);
    }
    return STATE.hooks.libvc_init === true;
  }, CONFIG.POLL_MS);

  logOk('daemon scheduler started (interval=' + CONFIG.POLL_MS + 'ms)');
}

// ─── App profile (com.potplayer.music) — passive only ───────────────────────
function readPidFromProcComm(comm) {
  try {
    var f = new File('/proc/' + comm, 'r');
    // not used — scan /proc numerically is heavy; use module hint instead
    f.close();
  } catch (e) { /* ignore */ }
  return null;
}

function checkVcplaxRunning() {
  // Passive: look for vcplax mapping in any process we can't from app — log hint only
  var mod = findModule('vcplax');
  if (mod) {
    logInfo('vcplax binary mapped in THIS process (unusual for app profile)');
    return true;
  }
  return false;
}

function bootstrapAppProfile() {
  logInfo('stage: app profile — passive monitoring (no Java.perform, no patchCode)');
  logInfo('Binder/libvc hooks live in vcplax daemon — attach there for full capture:');
  logInfo('  PID=$(pidof vcplax); frida-inject -p $PID -s script.js --runtime=qjs');

  schedule('app_dlopen', installDlopenHooks, CONFIG.POLL_MS);

  var vcplaxHintTick = 0;
  schedule('vcplax_hint', function () {
    vcplaxHintTick++;
    if (vcplaxHintTick % 10 === 1) {
      logInfo('app heartbeat #' + vcplaxHintTick +
              ' — open app UI, then attach frida to vcplax for HOOK_SYM/Binder');
    }
    checkVcplaxRunning();
    return false; // never mark complete — keep hinting
  }, CONFIG.POLL_MS);

  logOk('app scheduler started');
}

// ─── Entry ───────────────────────────────────────────────────────────────────
function main() {
  logInfo('=== frida_hook_libvc v4 smart (native / qjs) ===');
  logInfo('anti-debug: Interceptor.attach only — no Java, no Interceptor.replace, no patchCode');

  if (!assertTargetProcess()) {
    logWarn('idle mode — wrong process, exiting hook setup');
    return;
  }

  if (STATE.profile === 'daemon') {
    bootstrapDaemonProfile();
  } else if (STATE.profile === 'app') {
    bootstrapAppProfile();
  }

  logOk('ready — profile=' + STATE.profile + ' pid=' + Process.id);
}

main();
