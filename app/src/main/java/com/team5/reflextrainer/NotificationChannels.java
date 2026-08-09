package com.team5.reflextrainer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationChannels {
    private NotificationChannels() { }

    public static final String MESSAGES_CHANNEL_ID = "messages";

    /** Safe to call repeatedly — createNotificationChannel is a no-op if the channel already exists. */
    public static void ensureMessagesChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
                MESSAGES_CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("New messages from friends");
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
    }
}
