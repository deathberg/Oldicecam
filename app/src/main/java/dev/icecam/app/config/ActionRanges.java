package dev.icecam.app.config;

/** Default microsecond ranges for face-action seek (TX22). Index matches float button IDs. */
public final class ActionRanges {
    /** SharedPreferences keys — original typo {@code ActionRangebgin} preserved for compatibility. */
    public static final String PREF_BEGIN_PREFIX = "ActionRangebgin";
    public static final String PREF_END_PREFIX = "ActionRangeEnd";

    public static final int ACTION_EYE = 1;
    public static final int ACTION_HEAD_UP = 2;
    public static final int ACTION_MOUTH = 3;
    public static final int ACTION_TURN_LEFT = 4;
    public static final int ACTION_CENTER = 5;
    public static final int ACTION_TURN_RIGHT = 6;
    public static final int ACTION_NOD = 8;

    public static long defaultBegin(int actionId) {
        switch (actionId) {
            case ACTION_EYE: return 0L;
            case ACTION_HEAD_UP: return 5_000_000L;
            case ACTION_MOUTH: return 2_000_000L;
            case ACTION_TURN_LEFT: return 3_200_000L;
            case ACTION_CENTER: return 4_000_000L;
            case ACTION_TURN_RIGHT: return 4_000_000L;
            case ACTION_NOD: return 5_600_000L;
            default: return 0L;
        }
    }

    public static long defaultEnd(int actionId) {
        switch (actionId) {
            case ACTION_EYE: return 1_170_000L;
            case ACTION_HEAD_UP: return 5_900_000L;
            case ACTION_MOUTH: return 3_200_000L;
            case ACTION_TURN_LEFT: return 4_000_000L;
            case ACTION_CENTER: return 4_000_000L;
            case ACTION_TURN_RIGHT: return 5_000_000L;
            case ACTION_NOD: return 6_800_000L;
            default: return 0L;
        }
    }

    private ActionRanges() {}
}
