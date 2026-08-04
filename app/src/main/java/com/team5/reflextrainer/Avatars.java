package com.team5.reflextrainer;

/** The fixed set of pickable profile badges. avatarId is just an index into this list. */
public final class Avatars {
    private Avatars() { }

    public static final int[] DRAWABLES = {
            R.drawable.ic_avatar_1,
            R.drawable.ic_avatar_2,
            R.drawable.ic_avatar_3,
            R.drawable.ic_avatar_4,
            R.drawable.ic_avatar_5,
            R.drawable.ic_avatar_6,
            R.drawable.ic_avatar_7,
            R.drawable.ic_avatar_8,
            R.drawable.ic_avatar_9,
            R.drawable.ic_avatar_10,
            R.drawable.ic_avatar_11,
            R.drawable.ic_avatar_12,
    };

    public static int resFor(int avatarId) {
        if (avatarId < 0 || avatarId >= DRAWABLES.length) return DRAWABLES[0];
        return DRAWABLES[avatarId];
    }
}
