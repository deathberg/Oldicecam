// Dump memory blocks and resolve query addresses to functions.
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.*;
import ghidra.program.model.mem.MemoryBlock;

public class DumpMemoryAndResolve extends GhidraScript {
    @Override
    public void run() throws Exception {
        println("=== Memory blocks ===");
        for (MemoryBlock b : currentProgram.getMemory().getBlocks()) {
            println(b.getName() + " " + b.getStart() + " - " + b.getEnd() + " r=" + b.isRead() + " x=" + b.isExecute());
        }
        println("Function count: " + currentProgram.getFunctionManager().getFunctionCount());
        println("ImageBase: " + currentProgram.getImageBase());
        Address ep = toAddr("0x43e880");
        println("EntryPoint: " + ep + " fn=" + getFn(ep));

        String[] queries = {
            "0x43e880", "0x0043e880", "0x43f8fc", "0x43fa08", "0x444024",
            "0x4469f4", "0x46279c", "0x4456c0", "0xfe533"
        };
        for (String q : queries) {
            Address a = toAddr(q);
            if (a == null) { println("bad addr " + q); continue; }
            println("Q " + q + " -> " + getFn(a));
        }

        println("=== Interesting functions ===");
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
        while (it.hasNext()) {
            Function f = it.next();
            String n = f.getName().toLowerCase();
            if (n.contains("transact") || n.contains("binder") || n.contains("main")
                    || n.contains("dlopen") || n.contains("ontransact") || n.contains("bbinder")
                    || n.contains("enforce") || n.contains("vcplax")) {
                println(f.getEntryPoint() + " " + f.getName());
            }
        }
    }

    private String getFn(Address a) {
        FunctionManager fm = currentProgram.getFunctionManager();
        Function f = fm.getFunctionContaining(a);
        if (f != null) return f.getEntryPoint() + " " + f.getName();
        f = fm.getFunctionAt(a);
        if (f != null) return f.getEntryPoint() + " " + f.getName() + " (exact)";
        return "NONE";
    }
}
