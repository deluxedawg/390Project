package com.team5.reflextrainer;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/** Receives data-only FCM pushes for new chat messages and shows a local notification
 *  that deep-links into the relevant ChatActivity thread. */
public class PushService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            new ProfileManager().updateFcmToken(FirebaseAuth.getInstance().getCurrentUser().getUid(), token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String otherUid = data.get("otherUid");
        String otherUsername = data.get("otherUsername");
        String text = data.get("text");
        if (otherUid == null || otherUsername == null) return;

        int otherAvatarId = 0;
        try { otherAvatarId = Integer.parseInt(data.get("otherAvatarId")); } catch (Exception ignored) { }

        NotificationChannels.ensureMessagesChannel(this);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChatActivity.EXTRA_OTHER_UID, otherUid);
        intent.putExtra(ChatActivity.EXTRA_OTHER_USERNAME, otherUsername);
        intent.putExtra(ChatActivity.EXTRA_OTHER_AVATAR_ID, otherAvatarId);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, otherUid.hashCode(), intent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannels.MESSAGES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_messages)
                .setContentTitle(otherUsername)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        boolean canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
        if (canPost) {
            NotificationManagerCompat.from(this).notify(otherUid.hashCode(), builder.build());
        }
    }
}
