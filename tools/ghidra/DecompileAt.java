// Decompile function containing address; writes C to re-workspace/exports/.
// Usage: -postScript DecompileAt.java 0x444024 label [basename]
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.app.decompiler.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import java.io.File;
import java.io.FileWriter;

public class DecompileAt extends GhidraScript {

    @Override
    public void run() throws Exception {
        String[] args = getScriptArgs();
        if (args.length < 2) {
            println("Usage: DecompileAt.java <address> <label> [outputBasename]");
            return;
        }
        Address addr = toAddr(args[0]);
        if (addr == null) {
            println("Invalid address: " + args[0]);
            return;
        }
        FunctionManager fm = currentProgram.getFunctionManager();
        Function fn = fm.getFunctionContaining(addr);
        if (fn == null) {
            println("No function at " + addr);
            return;
        }
        DecompInterface decomp = new DecompInterface();
        decomp.openProgram(currentProgram);
        DecompileResults res = decomp.decompileFunction(fn, 180, monitor);
        if (!res.decompileCompleted()) {
            println("Decompile failed: " + res.getErrorMessage());
            return;
        }
        String label = args[1];
        String prog = currentProgram.getName();
        String base = args.length > 2 ? args[2] : (prog + "_" + label);
        File outDir = new File("/workspace/re-workspace/exports");
        outDir.mkdirs();
        File out = new File(outDir, base + ".c");
        String header = "// " + label + " @ " + fn.getEntryPoint() + " (query " + addr + ")\n";
        String body = res.getDecompiledFunction().getC();
        try (FileWriter w = new FileWriter(out)) {
            w.write(header);
            w.write(body);
        }
        println("Wrote " + out.getAbsolutePath());
    }
}
