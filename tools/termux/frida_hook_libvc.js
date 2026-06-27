// Native-only Frida hooks for vcplax (C/C++ daemon — NO JVM/ART).
// Do NOT use Java.perform / Java.* — this process has no Java runtime.
//
// Recommended (frida-server + root):
//   tsu -c "setenforce 0; pkill frida-server; /data/local/tmp/frida-server -D &"
//   PID=$(tsu -c "pidof vcplax")
//   frida -U -p "$PID" -l frida_hook_libvc.js -o ~/libvc_hooks.log --runtime=qjs
//
// frida-inject (must use qjs to avoid Java-bridge init crash):
//   tsu -c "setenforce 0"
//   /data/local/tmp/frida-inject -p $(tsu -c pidof vcplax) \
//     -s /data/local/tmp/frida_hook_libvc.js --runtime=qjs \
//     > /data/local/tmp/libvc_hooks.log
//
'use strict';

var ONTRANSACT_OFFSET = ptr('0x43f8b4'); // file VA, PIE load base + offset
var shadowhookInstalled = false;
var onTransactInstalled = false;

function log(msg) {
  console.log(msg);
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

function installShadowhookHooks() {
  if (shadowhookInstalled) return;
  var hooked = 0;

  var hookSym = Module.findExportByName(null, 'shadowhook_hook_sym_name');
  if (hookSym) {
    log('[+] hook shadowhook_hook_sym_name @ ' + hookSym);
    Interceptor.attach(hookSym, {
      onEnter: function (args) {
        var lib = readCppString(args[0]);
        var sym = readCppString(args[1]);
        var repl = args[2];
        log('HOOK_SYM lib=' + lib + ' sym=' + sym + ' replace=' + repl);
      }
    });
    hooked++;
  }

  var hookInit = Module.findExportByName(null, 'shadowhook_init');
  if (hookInit) {
    log('[+] hook shadowhook_init @ ' + hookInit);
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

function installVcplaxOnTransact() {
  if (onTransactInstalled) return;

  var mod = findModule('vcplax');
  if (!mod) {
    return;
  }

  var addr = mod.base.add(ONTRANSACT_OFFSET);
  log('[+] hook vcplax onTransact @ ' + addr + ' (base ' + mod.base + ' + ' + ONTRANSACT_OFFSET + ')');

  try {
    Interceptor.attach(addr, {
      onEnter: function (args) {
        // BBinder::onTransact(BBinder* this, uint32_t code, Parcel* data, Parcel* reply, uint32_t flags)
        var code = args[1].toInt32();
        if (code >= 0x0b && code <= 0x19) {
          log('BINDER_TX code=' + code + ' (0x' + code.toString(16) + ')');
        }
      }
    });
    onTransactInstalled = true;
  } catch (e) {
    log('[!] onTransact hook failed @ ' + addr + ': ' + e);
  }
}

function hookDlopen() {
  ['dlopen', 'android_dlopen_ext'].forEach(function (name) {
    var addr = Module.findExportByName(null, name);
    if (!addr) return;
    log('[+] hook ' + name + ' @ ' + addr);
    Interceptor.attach(addr, {
      onEnter: function (args) {
        try {
          this.path = args[0].readCString();
        } catch (e) {
          this.path = '?';
        }
      },
      onLeave: function (retval) {
        if (retval.isNull()) return;
        if (this.path && (this.path.indexOf('libvc') !== -1 ||
            this.path.indexOf('shadowhook') !== -1 ||
            this.path.indexOf('libvc++') !== -1)) {
          log('[dlopen] ' + this.path + ' -> ' + retval);
          installShadowhookHooks();
        }
      }
    });
  });
}

function hookModuleLoad() {
  if (typeof Process.attachModuleObserver !== 'function') return;
  Process.attachModuleObserver({
    onAdded: function (module) {
      var n = module.name + ' ' + module.path;
      if (n.indexOf('libvc') !== -1 || n.indexOf('shadowhook') !== -1) {
        log('[module] loaded ' + module.name + ' @ ' + module.base);
        installShadowhookHooks();
      }
    }
  });
}

// --- native entry: run immediately, no setImmediate / no Java ---
log('=== libvc / vcplax native hooks (no Java) ===');
log('arch=' + Process.arch + ' pid=' + Process.id + ' platform=' + Process.platform);

hookDlopen();
hookModuleLoad();
installShadowhookHooks();
installVcplaxOnTransact();

log('=== ready — use the app (play, TX24, etc.) ===');
