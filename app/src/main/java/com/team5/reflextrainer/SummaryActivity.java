package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSession;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        int avg = getIntent().getIntExtra("avg", 0);
        int best = getIntent().getIntExtra("best", 0);
        int total = getIntent().getIntExtra("total", 0);
        int correct = getIntent().getIntExtra("correct", 0);
        String difficulty = getIntent().getStringExtra("difficulty");
        ArrayList<Integer> rounds = getIntent().getIntegerArrayListExtra("rounds");
        if (rounds == null) rounds = new ArrayList<>();

        TextView tvAvg = findViewById(R.id.tvAvg);
        TextView tvBest = findViewById(R.id.tvBest);
        TextView tvAccuracy = findViewById(R.id.tvAccuracy);

        tvAvg.setText(avg > 0 ? avg + "" : "—");
        tvBest.setText(best > 0 ? best + "" : "—");

        int pct = total > 0 ? Math.round((correct * 100f) / total) : 0;
        tvAccuracy.setText(pct + "%");

        RecyclerView rv = findViewById(R.id.rvRounds);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new RoundAdapter(rounds, best));

        setupAiCoach(avg, best, total, correct, difficulty);

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            // go back to Home, clearing the training stack
            finish();
        });
    }

    private void setupAiCoach(int avg, int best, int total, int correct, String difficulty) {
        View cardAiCoach = findViewById(R.id.cardAiCoach);
        TextView tvAiTitle = findViewById(R.id.tvAiTitle);
        TextView tvAiDetail = findViewById(R.id.tvAiDetail);
        TextView tvExerciseName = findViewById(R.id.tvExerciseName);
        TextView tvExerciseDetail = findViewById(R.id.tvExerciseDetail);

        if (!AiCoachSettings.isEnabled(this)) {
            cardAiCoach.setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.btnDismissAi).setOnClickListener(v -> cardAiCoach.setVisibility(View.GONE));

        // Exercise explanation: static per difficulty, no DB needed — show it immediately.
        ExerciseExplanationProvider.Explanation exp =
                new ExerciseExplanationProvider().forDifficulty(difficulty);
        tvExerciseName.setText(exp.getName());
        tvExerciseDetail.setText(exp.getHowItWorks() + "\n\nTip: " + exp.getTip());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            AiRecommendationEngine.Recommendation rec =
                    new AiRecommendationEngine().build(avg, best, total, correct, difficulty, null);
            tvAiTitle.setText(rec.getTitle());
            tvAiDetail.setText(rec.getDetail());
            return;
        }

        TrainingSessionRepository repo = new TrainingSessionRepository(this);
        repo.getTrainingHistoryForUser(user.getUid(), sessions -> {
            // TrainingActivity saves the session BEFORE launching this screen,
            // so the newest history entry IS this session. Drop it so the coach
            // compares against genuinely previous sessions.
            // (Assumes the repository returns newest-first — verify the DAO's ORDER BY.)
            List<TrainingSession> previous =
                    (sessions != null && !sessions.isEmpty())
                            ? sessions.subList(1, sessions.size())
                            : sessions;

            AiRecommendationEngine.Recommendation rec =
                    new AiRecommendationEngine().build(avg, best, total, correct, difficulty, previous);

            runOnUiThread(() -> {
                tvAiTitle.setText(rec.getTitle());
                tvAiDetail.setText(rec.getDetail());
            });
        });
    }
}