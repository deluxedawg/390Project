package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.team5.reflextrainer.data.TrainingSessionRepository;
import com.team5.reflextrainer.hardware.ESPBluetoothManager;
import com.team5.reflextrainer.hardware.SensorMessage;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ESPBluetoothManager.Listener {

    private TextView tvSensorStatus;
    private ImageView dotHold, dotSet, dotGo;
    private TextView tvStreak, tvStreakSub;
    private TextView tvWelcome;
    private ImageView ivHomeAvatar;

    private ImageView[] badgeViews;
    private TextView tvBadgeCount;
    private final boolean[] badgeEarned = new boolean[Achievements.COUNT];

    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    private static final float LIT = 1f;
    private static final float UNLIT = 0.25f;
    private static final float BADGE_LOCKED_ALPHA = 0.28f;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), results -> {
        boolean allGranted = true;
        for (Boolean granted : results.values()) {
            if (!granted) allGranted = false;
        }
        if(allGranted) {
            connectToSensor();
        } else {
            updateSensorStatus(SensorStatus.DISCONNECTED);
        }
    });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user != null) ? user.getUid() : null;
        sessionRepository = new TrainingSessionRepository(this);

        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        dotHold = findViewById(R.id.dotHold);
        dotSet = findViewById(R.id.dotSet);
        dotGo = findViewById(R.id.dotGo);
        updateSensorStatus(SensorStatus.DISCONNECTED);

        tvStreak = findViewById(R.id.tvStreak);
        tvStreakSub = findViewById(R.id.tvStreakSub);

        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        badgeViews = new ImageView[] {
                findViewById(R.id.badge0), findViewById(R.id.badge1),
                findViewById(R.id.badge2), findViewById(R.id.badge3),
                findViewById(R.id.badge4), findViewById(R.id.badge5),
                findViewById(R.id.badge6), findViewById(R.id.badge7),
                findViewById(R.id.badge8), findViewById(R.id.badge9),
                findViewById(R.id.badge10), findViewById(R.id.badge11),
        };
        View.OnClickListener openAchievements = v ->
                startActivity(new Intent(this, AchievementsActivity.class));
        for (ImageView badge : badgeViews) {
            badge.setOnClickListener(openAchievements);
        }
        findViewById(R.id.cardBadges).setOnClickListener(openAchievements);
        refreshBadgeUi();

        loadStreak();
        loadChallengeBadge();
        loadFriendBadge();

        tvWelcome = findViewById(R.id.tvWelcome);
        ivHomeAvatar = findViewById(R.id.ivHomeAvatar);
        tvWelcome.setText(fallbackName(user));
        loadProfileHeader(user);

        View logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        Button startTraining = findViewById(R.id.btnStart);
        startTraining.setOnClickListener(v ->
                startActivity(new Intent(this, ModeSelectActivity.class)));

        // NEW from teammate
        View viewHistory = findViewById(R.id.btnViewHistory);
        viewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        View leaderboard = findViewById(R.id.btnLeaderboard);
        leaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        View profile = findViewById(R.id.btnProfile);
        profile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // NEW from teammate
        View challenges = findViewById(R.id.btnChallenges);
        challenges.setOnClickListener(v ->
                startActivity(new Intent(this, ChallengesActivity.class)));

        View messages = findViewById(R.id.btnMessages);
        messages.setOnClickListener(v ->
                startActivity(new Intent(this, InboxActivity.class)));

        ESPBluetoothManager.getInstance().setListener(this);
        checkPermissionsAndConnect();

        setUpMessagingNotifications(user);
    }

    /** Notification channel + runtime permission (Android 13+) + FCM token sync for chat push. */
    private void setUpMessagingNotifications(FirebaseUser user) {
        NotificationChannels.ensureMessagesChannel(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (user != null) {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                    new ProfileManager().updateFcmToken(user.getUid(), token));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!ESPBluetoothManager.getInstance().isConnected()){
            checkPermissionsAndConnect();
        }
        loadStreak();
        loadChallengeBadge();
        loadFriendBadge();
        loadProfileHeader(FirebaseAuth.getInstance().getCurrentUser());
    }

    private void loadProfileHeader(FirebaseUser user) {
        if (user == null) return;
        new ProfileManager().loadProfile(user.getUid(), new ProfileManager.ProfileCallback() {
            @Override
            public void onResult(UserProfile profile) {
                if (profile.getUsername() != null && !profile.getUsername().isEmpty()) {
                    tvWelcome.setText(profile.getUsername());
                }
                ivHomeAvatar.setImageResource(Avatars.resFor(profile.getAvatarId()));
            }
            @Override
            public void onError(String message) { /* keep the fallback name and default avatar */ }
        });
    }

    /** Shown immediately, before the Firestore username lookup resolves. */
    private String fallbackName(FirebaseUser user) {
        if (user == null || user.getEmail() == null) return "Trainer";
        String email = user.getEmail();
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    // ===================== streak =====================

    private void loadStreak() {
        if (currentUserId == null || tvStreak == null) return;
        sessionRepository.getTrainingHistoryForUser(currentUserId, sessions -> {
            Achievements.Streak streak = Achievements.computeStreak(sessions);
            updateStreakUi(streak);
            Achievements.computeSessionBadges(sessions, streak.days, badgeEarned);
            refreshBadgeUi();
        });
    }

    private void updateStreakUi(Achievements.Streak streak) {
        if (streak.days == 0) {
            tvStreak.setText("No streak yet");
            tvStreakSub.setText("Train today to start one");
        } else {
            tvStreak.setText(streak.days == 1 ? "1-day streak" : streak.days + "-day streak");
            tvStreakSub.setText(streak.trainedToday
                    ? "Nice work today — come back tomorrow"
                    : "Train today to keep it alive");
        }
    }

    // ===================== badges =====================

    private void loadChallengeBadge() {
        if (currentUserId == null || tvBadgeCount == null) return;
        new ChallengeManager().loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> challenges) {
                int wins = 0;
                for (Challenge c : challenges) {
                    if (currentUserId.equals(c.getWinnerUid())) wins++;
                }
                badgeEarned[7] = wins >= 1;
                badgeEarned[10] = wins >= 5;
                refreshBadgeUi();
            }
            @Override
            public void onError(String message) { /* leave the Challenger/Rival Slayer badges as-is */ }
        });
    }

    private void loadFriendBadge() {
        if (currentUserId == null || tvBadgeCount == null) return;
        new FriendManager().loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                badgeEarned[11] = !friends.isEmpty();
                refreshBadgeUi();
            }
            @Override
            public void onError(String message) { /* leave the Social Butterfly badge as-is */ }
        });
    }

    private void refreshBadgeUi() {
        if (badgeViews == null) return;
        int earnedCount = 0;
        for (int i = 0; i < badgeViews.length; i++) {
            badgeViews[i].setAlpha(badgeEarned[i] ? LIT : BADGE_LOCKED_ALPHA);
            if (badgeEarned[i]) earnedCount++;
        }
        tvBadgeCount.setText(earnedCount + " of " + badgeViews.length + " unlocked");
    }

    private void checkPermissionsAndConnect() {
        java.util.List<String> needed = new java.util.ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            needed.add(Manifest.permission.BLUETOOTH_CONNECT);
            needed.add(Manifest.permission.BLUETOOTH_SCAN);
        }   else {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        java.util.List<String> missing = new java.util.ArrayList<>();
        for(String perm : needed) {
            if(ContextCompat.checkSelfPermission(this, perm)!= PackageManager.PERMISSION_GRANTED){
                missing.add(perm);
            }
        }

        if(missing.isEmpty()){
            connectToSensor();
        }   else {
            permissionLauncher.launch(missing.toArray(new String[0]));
        }
    }

    private void connectToSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED)
        {
            updateSensorStatus(SensorStatus.DISCONNECTED);
            return;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            updateSensorStatus(SensorStatus.DISCONNECTED);
            return;
        }
        updateSensorStatus(SensorStatus.CONNECTING);
        ESPBluetoothManager.getInstance().connect(adapter);
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        runOnUiThread(()->{
            if (connected) updateSensorStatus(SensorStatus.CONNECTED);
            else if (connecting) updateSensorStatus(SensorStatus.CONNECTING);
            else updateSensorStatus(SensorStatus.DISCONNECTED);
        });
    }

    @Override
    public void onMessage(SensorMessage message) {

    }

    public void updateSensorStatus(SensorStatus status) {
        dotHold.setAlpha(UNLIT);
        dotSet.setAlpha(UNLIT);
        dotGo.setAlpha(UNLIT);

        switch (status) {
            case CONNECTED:
                tvSensorStatus.setText("Connected");
                tvSensorStatus.setTextColor(getColor(R.color.accent));
                dotGo.setAlpha(LIT);
                break;
            case CONNECTING:
                tvSensorStatus.setText("Connecting...");
                tvSensorStatus.setTextColor(getColor(R.color.color_set));
                dotSet.setAlpha(LIT);
                break;
            case DISCONNECTED:
            default:
                tvSensorStatus.setText("Disconnected");
                tvSensorStatus.setTextColor(getColor(R.color.danger));
                dotHold.setAlpha(LIT);
                break;
        }
    }
}