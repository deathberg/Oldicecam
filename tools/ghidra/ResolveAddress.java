// List function containing address; print entry point and name.
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.FunctionIterator;

public class ResolveAddress extends GhidraScript {
    @Override
    public void run() throws Exception {
        String[] args = getScriptArgs();
        if (args.length >= 1 && !args[0].equals("all")) {
            Address a = toAddr(args[0]);
            Function f = currentProgram.getFunctionManager().getFunctionContaining(a);
            if (f == null) println("NO_FUNC at " + args[0]);
            else println("FUNC " + f.getEntryPoint() + " " + f.getName() + " (query " + args[0] + ")");
            return;
        }
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
        int n = 0;
        while (it.hasNext() && n < 5000) {
            Function f = it.next();
            String name = f.getName().toLowerCase();
            if (name.contains("transact") || name.contains("binder") || name.contains("main")
                    || name.contains("ontransact") || name.contains("dlopen") || name.contains("vcplax")) {
                println(f.getEntryPoint() + " " + f.getName());
            }
            n++;
        }
    }
}
