package com.team5.reflextrainer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
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

        setupRoundsTabs(rv, rounds, best);
        setupAiCoach(avg, best, total, correct, difficulty);

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            // go back to Home, clearing the training stack
            finish();
        });
    }

    private void setupRoundsTabs(RecyclerView rv, List<Integer> rounds, int best) {
        TabLayout tabs = findViewById(R.id.tabRounds);
        BarChart chart = findViewById(R.id.chartRounds);
        setupRoundsChart(chart, rounds, best);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean graph = tab.getPosition() == 1;
                rv.setVisibility(graph ? View.GONE : View.VISIBLE);
                chart.setVisibility(graph ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupRoundsChart(BarChart chart, List<Integer> rounds, int best) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < rounds.size(); i++) {
            entries.add(new BarEntry(i, rounds.get(i)));
        }

        BarDataSet set = new BarDataSet(entries, "Reaction time (ms)");
        set.setColors(colorsForRounds(rounds, best));
        set.setDrawValues(false);
        set.setHighlightEnabled(false);

        BarData data = new BarData(set);
        data.setBarWidth(0.7f);
        chart.setData(data);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setExtraBottomOffset(4f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#8C96A6"));
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value) + 1);
            }
        });

        YAxis left = chart.getAxisLeft();
        left.setTextColor(Color.parseColor("#8C96A6"));
        left.setGridColor(Color.parseColor("#22FFFFFF"));
        left.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        chart.invalidate();
    }

    private int[] colorsForRounds(List<Integer> rounds, int best) {
        int[] colors = new int[rounds.size()];
        for (int i = 0; i < rounds.size(); i++) {
            colors[i] = (rounds.get(i) == best)
                    ? Color.parseColor("#29FF88")
                    : Color.parseColor("#3A4250");
        }
        return colors;
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
            GeminiCoachClient.generate(avg, best, total, correct, difficulty, null, rec -> {
                tvAiTitle.setText(rec.getTitle());
                tvAiDetail.setText(rec.getDetail());
            });
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

            GeminiCoachClient.generate(avg, best, total, correct, difficulty, previous, rec -> {
                tvAiTitle.setText(rec.getTitle());
                tvAiDetail.setText(rec.getDetail());
            });
        });
    }
}