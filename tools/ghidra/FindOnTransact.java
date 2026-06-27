// Find vcplax onTransact implementation via enforceInterface xrefs.
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.ReferenceIterator;
import ghidra.program.model.symbol.ReferenceManager;

public class FindOnTransact extends GhidraScript {
    @Override
    public void run() throws Exception {
        println("ImageBase=" + currentProgram.getImageBase());
        println("Functions=" + currentProgram.getFunctionManager().getFunctionCount());

        String[] needles = {
            "com.xiaomi.vlive.IMyBinderService",
            "enforceInterface",
            "onTransact",
            "defaultServiceManager",
            "joinThreadPool",
            "ProcessState"
        };
        ReferenceManager rm = currentProgram.getReferenceManager();
        for (String needle : needles) {
            println("=== refs to string/symbol: " + needle + " ===");
            Address str = findString(needle);
            if (str == null) {
                println("  (not found)");
                continue;
            }
            println("  @ " + str);
            ReferenceIterator refs = rm.getReferencesTo(str);
            int n = 0;
            while (refs.hasNext() && n < 40) {
                Reference ref = refs.next();
                Address from = ref.getFromAddress();
                Function f = getFunctionContaining(from);
                println("  ref from " + from + " in " + (f != null ? f.getEntryPoint() + " " + f.getName() : "NO_FN"));
                n++;
            }
        }

        println("=== large switch-like functions (candidate dispatch) ===");
        FunctionIterator it = currentProgram.getFunctionManager().getFunctions(true);
        while (it.hasNext()) {
            Function f = it.next();
            if (f.getBody().getNumAddresses() > 800) {
                String n = f.getName().toLowerCase();
                if (n.contains("transact") || n.contains("fun_005") || f.getEntryPoint().toString().contains("53f")
                        || f.getEntryPoint().toString().contains("544") || f.getEntryPoint().toString().contains("546")) {
                    println(f.getEntryPoint() + " " + f.getName() + " size=" + f.getBody().getNumAddresses());
                }
            }
        }
    }

    private Address findString(String needle) {
        DataIterator di = currentProgram.getListing().getDefinedData(true);
        while (di.hasNext()) {
            Data d = di.next();
            Object val = d.getValue();
            if (val instanceof String && ((String) val).contains(needle)) {
                return d.getAddress();
            }
        }
        return null;
    }
}
