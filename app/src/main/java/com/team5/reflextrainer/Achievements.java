package com.team5.reflextrainer;

import com.team5.reflextrainer.data.TrainingSession;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for the achievement badges: what they're called, how
 * they're earned, and which avatar (if any) each one unlocks. Shared by the Home
 * badge shelf, the Achievements screen, and Profile's avatar picker so all three
 * agree on the same scoring.
 */
public final class Achievements {
    private Achievements() { }

    public static final int COUNT = 16;

    public static final String[] NAMES = {
            "First Rep", "Sub-300 Club", "Sub-200 Club", "Sharpshooter",
            "Century", "3-Day Streak", "7-Day Streak", "Challenger",
            "Marathon", "Elite Reflexes", "Rival Slayer", "Social Butterfly",
            "Iron Will", "Grinder", "Hard Mode Ace", "Consistent"
    };

    public static final String[] DESCRIPTIONS = {
            "Complete your first training session",
            "Log a best reaction time under 300ms",
            "Log a best reaction time under 200ms",
            "Finish a session with 100% accuracy",
            "Complete 100 rounds total",
            "Reach a 3-day streak",
            "Reach a 7-day streak",
            "Win a challenge against a friend",
            "Reach a 14-day streak",
            "Log a best reaction time under 150ms",
            "Win 5 challenges against friends",
            "Add your first friend",
            "Reach a 30-day streak",
            "Complete 500 rounds total",
            "Finish a Hard session with 100% accuracy",
            "Log 10 Reaction training sessions",
    };

    /** Which avatar (index into Avatars.DRAWABLES) is shown for badge i on the Home shelf / Achievements list. */
    public static final int[] BADGE_ICON_AVATAR_INDEX = { 0, 4, 7, 3, 5, 2, 1, 6, 8, 9, 10, 11, 12, 13, 14, 15 };

    /** avatar index -> badge index required to unlock it in the Profile picker. -1 = always unlocked (the default). */
    public static final int[] AVATAR_REQUIRES_BADGE = { -1, 6, 5, 3, 1, 4, 7, 2, 8, 9, 10, 11, 12, 13, 14, 15 };

    public static boolean isAvatarUnlocked(int avatarIndex, boolean[] earned) {
        if (avatarIndex < 0 || avatarIndex >= AVATAR_REQUIRES_BADGE.length) return false;
        int required = AVATAR_REQUIRES_BADGE[avatarIndex];
        if (required < 0) return true;
        return earned != null && required < earned.length && earned[required];
    }

    public static final class Streak {
        public final int days;
        public final boolean trainedToday;
        Streak(int days, boolean trainedToday) { this.days = days; this.trainedToday = trainedToday; }
    }

    /** Consecutive calendar days (ending today or yesterday) with at least one logged session. */
    public static Streak computeStreak(List<TrainingSession> sessions) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Set<String> trainedDays = new HashSet<>();
        for (TrainingSession s : sessions) {
            trainedDays.add(dayFormat.format(s.getTimestamp()));
        }
        if (trainedDays.isEmpty()) return new Streak(0, false);

        Calendar cursor = Calendar.getInstance();
        String today = dayFormat.format(cursor.getTime());
        boolean trainedToday = trainedDays.contains(today);

        if (!trainedToday) {
            cursor.add(Calendar.DAY_OF_YEAR, -1);
            if (!trainedDays.contains(dayFormat.format(cursor.getTime()))) {
                return new Streak(0, false); // no session today or yesterday: streak is broken
            }
        }

        int streak = 0;
        while (trainedDays.contains(dayFormat.format(cursor.getTime()))) {
            streak++;
            cursor.add(Calendar.DAY_OF_YEAR, -1);
        }
        return new Streak(streak, trainedToday);
    }

    /**
     * Fills the session/streak-derived badge indices (0-2, 3, 4-6, 8-9). Indices 7, 10
     * (challenge-based) and 11 (friend-based) need separate queries elsewhere.
     *
     * OR-merges into whatever's already in {@code out} rather than overwriting it, so a badge
     * seeded as already-earned (e.g. restored from a Firestore backup after local session
     * history was wiped by a reinstall) can't be recomputed back to false.
     */
    public static void computeSessionBadges(List<TrainingSession> sessions, int streakDays, boolean[] out) {
        out[0] = out[0] || !sessions.isEmpty();

        int totalRounds = 0;
        boolean sub300 = false, sub200 = false, sub150 = false, perfect = false, hardAce = false;
        for (TrainingSession s : sessions) {
            totalRounds += s.getTotalRounds();
            if (s.getBestReactionMs() > 0 && s.getBestReactionMs() < 300) sub300 = true;
            if (s.getBestReactionMs() > 0 && s.getBestReactionMs() < 200) sub200 = true;
            if (s.getBestReactionMs() > 0 && s.getBestReactionMs() < 150) sub150 = true;
            if (s.getTotalRounds() > 0 && s.getCorrectRounds() == s.getTotalRounds()) {
                perfect = true;
                if ("Hard".equals(s.getDifficulty())) hardAce = true;
            }
        }
        out[1] = out[1] || sub300;
        out[2] = out[2] || sub200;
        out[3] = out[3] || perfect;
        out[4] = out[4] || totalRounds >= 100;
        out[5] = out[5] || streakDays >= 3;
        out[6] = out[6] || streakDays >= 7;
        out[8] = out[8] || streakDays >= 14;
        out[9] = out[9] || sub150;
        out[12] = out[12] || streakDays >= 30;
        out[13] = out[13] || totalRounds >= 500;
        out[14] = out[14] || hardAce;
        out[15] = out[15] || sessions.size() >= 10;
    }
}
