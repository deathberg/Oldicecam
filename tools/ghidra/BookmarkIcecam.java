// Ghidra post-script: bookmark recovered IceCam native addresses (arm64 vcplax).
//@category IceCam

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.BookmarkType;
import ghidra.program.model.listing.BookmarkManager;

public class BookmarkIcecam extends GhidraScript {

    private static final String[][] MARKS = {
        {"entry", "0x43e880", "PIE entry point"},
        {"binder", "0xfe533", "Rodata: com.xiaomi.vlive.IMyBinderService"},
        {"binder-code", "0x43f8fc", "ADRP xref cluster (interface setup)"},
        {"tx14", "0x43fa08", "TX14 setMode cluster"},
        {"tx24", "0x444024", "TX24 transform/color cluster"},
        {"tx25", "0x4456c0", "TX25 hard recovery cluster"},
        {"tx22", "0x46279c", "TX22 seek range cluster"},
        {"libvc-str", "0x102960", "Rodata: libvc.so path"},
    };

    @Override
    public void run() throws Exception {
        BookmarkManager bm = currentProgram.getBookmarkManager();
        for (String[] m : MARKS) {
            Address addr = toAddr(m[1]);
            if (addr == null) {
                println("Skip invalid address " + m[1]);
                continue;
            }
            bm.setBookmark(addr, BookmarkType.ANALYSIS, m[0], m[2]);
            println("Bookmark " + m[0] + " @ " + m[1]);
        }
    }
}
