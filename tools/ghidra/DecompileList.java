// Decompile a list of functions by (file-VA) address into one C file.
// Usage: -postScript DecompileList.java <outBasename> <addr1:label1> <addr2:label2> ...
// Output: /workspace/re-workspace/exports/<outBasename>.c
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import java.io.File;
import java.io.FileWriter;

public class DecompileList extends GhidraScript {

    @Override
    public void run() throws Exception {
        String[] args = getScriptArgs();
        if (args.length < 2) {
            println("Usage: DecompileList.java <outBasename> <addr:label> ...");
            return;
        }
        String base = args[0];
        File outDir = new File("/workspace/re-workspace/exports");
        outDir.mkdirs();
        File out = new File(outDir, base + ".c");

        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        FunctionManager fm = currentProgram.getFunctionManager();

        int ok = 0, fail = 0;
        try (FileWriter w = new FileWriter(out)) {
            w.write("// Targeted decompilation of " + currentProgram.getName()
                    + " (imageBase=" + currentProgram.getImageBase() + ")\n\n");
            for (int i = 1; i < args.length; i++) {
                String[] kv = args[i].split(":", 2);
                String addrStr = kv[0];
                String label = kv.length > 1 ? kv[1] : ("fn" + i);
                Address a = resolve(addrStr, fm);
                Function fn = (a == null) ? null : fm.getFunctionContaining(a);
                w.write("// ===== " + label + " (query " + addrStr + ") =====\n");
                if (fn == null) {
                    w.write("// [no function at " + addrStr + "]\n\n");
                    fail++;
                    continue;
                }
                DecompileResults res = decomp.decompileFunction(fn, 180, monitor);
                w.write("// " + fn.getName() + " @ " + fn.getEntryPoint()
                        + " size=" + fn.getBody().getNumAddresses() + "\n");
                if (res != null && res.decompileCompleted()) {
                    w.write(res.getDecompiledFunction().getC());
                    ok++;
                } else {
                    w.write("// [decompile failed]\n");
                    fail++;
                }
                w.write("\n");
            }
        }
        println("DecompileList wrote " + out.getAbsolutePath() + " ok=" + ok + " fail=" + fail);
    }

    private Address resolve(String raw, FunctionManager fm) throws Exception {
        String s = raw.trim();
        if (!s.startsWith("0x") && !s.startsWith("0X")) s = "0x" + s;
        long fileVa = Long.decode(s);
        Address direct = toAddr(s);
        if (fm.getFunctionContaining(direct) != null) return direct;
        Address shifted = currentProgram.getImageBase().add(fileVa);
        if (fm.getFunctionContaining(shifted) != null) return shifted;
        return direct;
    }
}
