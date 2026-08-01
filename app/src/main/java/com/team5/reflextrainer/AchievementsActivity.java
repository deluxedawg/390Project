package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private final boolean[] earned = new boolean[Achievements.COUNT];

    private TrainingSessionRepository sessionRepository;
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

        loadSessionBadges();
        loadChallengeBadge();
        refreshUi();
    }

    private void loadSessionBadges() {
        if (userId == null) return;
        sessionRepository.getTrainingHistoryForUser(userId, sessions -> {
            Achievements.Streak streak = Achievements.computeStreak(sessions);
            Achievements.computeSessionBadges(sessions, streak.days, earned);
            refreshUi();
        });
    }

    private void loadChallengeBadge() {
        if (userId == null) return;
        new ChallengeManager().loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> challenges) {
                boolean wonOne = false;
                for (Challenge c : challenges) {
                    if (userId.equals(c.getWinnerUid())) { wonOne = true; break; }
                }
                earned[7] = wonOne;
                refreshUi();
            }
            @Override
            public void onError(String message) { /* leave the Challenger badge as-is */ }
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
    }
}
