package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementsActivity extends AppCompatActivity {

    private final boolean[] earned = new boolean[Achievements.COUNT];
    private final Set<Integer> persistedBadges = new HashSet<>();

    private TrainingSessionRepository sessionRepository;
    private final ProfileManager profileManager = new ProfileManager();
    private String userId;

    private ProgressBar progressBar;
    private TextView tvPercent, tvCount;
    private AchievementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        progressBar = findViewById(R.id.progressAchievements);
        tvPercent = findViewById(R.id.tvAchievementPercent);
        tvCount = findViewById(R.id.tvAchievementCount);

        RecyclerView rv = findViewById(R.id.rvAchievements);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementAdapter(earned);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = (user != null) ? user.getUid() : null;
        sessionRepository = new TrainingSessionRepository(this);

        loadPersistedBadges();
        loadSessionBadges();
        loadChallengeBadge();
        loadFriendBadge();
        refreshUi();
    }

    private void loadPersistedBadges() {
        if (userId == null) return;
        profileManager.loadEarnedBadges(userId, badgeIndices -> {
            for (int i : badgeIndices) {
                if (i >= 0 && i < earned.length) earned[i] = true;
            }
            persistedBadges.addAll(badgeIndices);
            refreshUi();
        });
    }

    private void loadSessionBadges() {
        if (userId == null) return;
        // streak counts any training mode; the session-content badges (sub-XXXms, Century,
        // Sharpshooter, etc.) stay scoped to Reaction sessions, since that's what they measure.
        sessionRepository.getTrainingHistoryForUser(userId, allSessions -> {
            Achievements.Streak streak = Achievements.computeStreak(allSessions);
            sessionRepository.getTrainingHistoryForUserAndMode(userId, TrainingMode.REACTION.label, reactionSessions -> {
                Achievements.computeSessionBadges(reactionSessions, streak.days, earned);
                refreshUi();
            });
        });
    }

    private void loadChallengeBadge() {
        if (userId == null) return;
        new ChallengeManager().loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> challenges) {
                int wins = 0;
                for (Challenge c : challenges) {
                    if (userId.equals(c.getWinnerUid())) wins++;
                }
                earned[7] = earned[7] || wins >= 1;
                earned[10] = earned[10] || wins >= 5;
                refreshUi();
            }
            @Override
            public void onError(String message) { /* leave the Challenger/Rival Slayer badges as-is */ }
        });
    }

    private void loadFriendBadge() {
        if (userId == null) return;
        new FriendManager().loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                earned[11] = earned[11] || !friends.isEmpty();
                refreshUi();
            }
            @Override
            public void onError(String message) { /* leave the Social Butterfly badge as-is */ }
        });
    }

    private void refreshUi() {
        int earnedCount = 0;
        for (boolean b : earned) if (b) earnedCount++;
        int percent = Math.round(100f * earnedCount / Achievements.COUNT);

        tvPercent.setText(percent + "%");
        tvCount.setText(earnedCount + " of " + Achievements.COUNT + " unlocked");
        progressBar.setProgress(percent);
        adapter.notifyDataSetChanged();

        syncNewlyEarnedBadges();
    }

    /** Pushes any badges that just flipped true and aren't in Firestore yet, so they survive a reinstall. */
    private void syncNewlyEarnedBadges() {
        if (userId == null) return;
        List<Integer> newlyEarned = new ArrayList<>();
        for (int i = 0; i < earned.length; i++) {
            if (earned[i] && !persistedBadges.contains(i)) newlyEarned.add(i);
        }
        if (newlyEarned.isEmpty()) return;
        persistedBadges.addAll(newlyEarned);
        profileManager.addEarnedBadges(userId, newlyEarned);
    }
}
