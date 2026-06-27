// Frida script: log shadowhook_hook_sym_name(lib, sym, ...) from libvc init.
// Run on phone (Termux, app must be running / restart app after attach):
//
//   frida -U -n vcplax -l frida_hook_libvc.js -o ~/libvc_hooks.log
//   # or spawn via app process:
//   frida -U -f com.potplayer.music -l frida_hook_libvc.js -o ~/libvc_hooks.log --no-pause
//
'use strict';

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

function hookShadowhook() {
  var names = ['shadowhook_hook_sym_name', 'shadowhook_init'];
  names.forEach(function (sym) {
    var addr = Module.findExportByName(null, sym);
    if (!addr) return;
    console.log('[+] hook ' + sym + ' @ ' + addr);
    Interceptor.attach(addr, {
      onEnter: function (args) {
        if (sym === 'shadowhook_hook_sym_name') {
          var lib = readCppString(args[0]);
          var name = readCppString(args[1]);
          var repl = args[2];
          console.log('HOOK_SYM lib=' + lib + ' sym=' + name + ' replace=' + repl);
        } else {
          console.log('shadowhook_init mode=' + args[0] + ' debug=' + args[1]);
        }
      }
    });
  });
}

function hookBinderTransact() {
  var syms = [
    '_ZN7android8BBinder10onTransactEjRKNS_6ParcelEPS1_j',
    '_ZN7android7BBinder10onTransactEjRKNS_6ParcelEPS1_j'
  ];
  syms.forEach(function (mangled) {
    var addr = Module.findExportByName('libbinder.so', mangled);
    if (!addr) return;
    console.log('[+] hook onTransact @ ' + addr);
    Interceptor.attach(addr, {
      onEnter: function (args) {
        var code = args[1].toInt32();
        if (code >= 0x0b && code <= 0x19) {
          console.log('BINDER_TX code=' + code + ' (0x' + code.toString(16) + ')');
        }
      }
    });
  });
}

setImmediate(function () {
  console.log('=== libvc / vcplax frida hooks ===');
  hookShadowhook();
  hookBinderTransact();
  console.log('=== ready — use the app (play, TX24, etc.) ===');
});
