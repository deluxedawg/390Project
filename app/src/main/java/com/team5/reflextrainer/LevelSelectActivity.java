package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSessionRepository;

public class LevelSelectActivity extends AppCompatActivity {

    public static final String EXTRA_TIMEOUT = "timeout_ms";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_ROUNDS = "rounds";

    private MaterialButtonToggleGroup toggleRounds;
    private View tvRecommendedEasy, tvRecommendedMedium, tvRecommendedHard;

    // set when this screen was opened to start a challenge
    private String challengeToUid;
    private String challengeToUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        challengeToUid = getIntent().getStringExtra("challengeToUid");
        challengeToUsername = getIntent().getStringExtra("challengeToUsername");

        toggleRounds = findViewById(R.id.toggleRounds);
        toggleRounds.check(R.id.round10);   // default to 10

        tvRecommendedEasy = findViewById(R.id.tvRecommendedEasy);
        tvRecommendedMedium = findViewById(R.id.tvRecommendedMedium);
        tvRecommendedHard = findViewById(R.id.tvRecommendedHard);

        findViewById(R.id.cardEasy).setOnClickListener(v -> startTraining(2000, "Easy"));
        findViewById(R.id.cardMedium).setOnClickListener(v -> startTraining(1000, "Medium"));
        findViewById(R.id.cardHard).setOnClickListener(v -> startTraining(500, "Hard"));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        showRecommendedDifficulty();
    }

    /**
     * Highlights whichever card the AI coach's stats-based logic suggests, based on the
     * accuracy/speed of the user's most recent Reaction sessions. Purely advisory — every
     * card stays tappable, this just points a new or improving user at a sensible default.
     */
    private void showRecommendedDifficulty() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new TrainingSessionRepository(this).getTrainingHistoryForUserAndMode(
                user.getUid(), TrainingMode.REACTION.label, history -> {
                    String recommended = new AiRecommendationEngine().recommendDifficulty(history);
                    tvRecommendedEasy.setVisibility("Easy".equals(recommended) ? View.VISIBLE : View.GONE);
                    tvRecommendedMedium.setVisibility("Medium".equals(recommended) ? View.VISIBLE : View.GONE);
                    tvRecommendedHard.setVisibility("Hard".equals(recommended) ? View.VISIBLE : View.GONE);
                });
    }

    private int getSelectedRounds() {
        int checked = toggleRounds.getCheckedButtonId();
        if (checked == R.id.round5) return 5;
        if (checked == R.id.round20) return 20;
        return 10;   // default / round10
    }

    private void startTraining(int timeoutMs, String difficulty) {
        Intent i = new Intent(this, TrainingActivity.class);
        i.putExtra(EXTRA_TIMEOUT, timeoutMs);
        i.putExtra(EXTRA_DIFFICULTY, difficulty);
        i.putExtra(EXTRA_ROUNDS, getSelectedRounds());

        // forward challenge info if this session is a challenge
        if (challengeToUid != null) {
            i.putExtra("challengeToUid", challengeToUid);
            i.putExtra("challengeToUsername", challengeToUsername);
        }

        startActivity(i);
        finish();
    }
}