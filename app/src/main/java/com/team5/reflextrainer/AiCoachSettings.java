package com.team5.reflextrainer;

import android.content.Context;
import android.content.SharedPreferences;

public final class AiCoachSettings {
    private static final String PREFS = "ai_coach_settings";
    private static final String KEY_ENABLED = "enabled";

    private AiCoachSettings() { }

    public static boolean isEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }
}
