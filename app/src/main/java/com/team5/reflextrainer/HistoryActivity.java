package com.team5.reflextrainer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSession;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvNoHistory;
    private View cardFilters;
    private TabLayout tabs;
    private LineChart chart;
    private MaterialButtonToggleGroup toggleModesRow1, toggleModesRow2;
    private MaterialButton btnDateFrom, btnDateTo;
    private RangeSlider sliderScore;
    private TextView tvScoreRange;

    private List<TrainingSession> allSessions = new ArrayList<>();
    private boolean showingGraph = false;
    private Long fromDateMs, toDateMs;
    private float scoreBoundMin, scoreBoundMax;

    private final SimpleDateFormat filterDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button back = findViewById(R.id.btnBackHome);
        back.setOnClickListener(v -> finish());

        rv = findViewById(R.id.rvHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        cardFilters = findViewById(R.id.cardFilters);
        tabs = findViewById(R.id.tabHistory);
        chart = findViewById(R.id.chartHistory);
        toggleModesRow1 = findViewById(R.id.toggleModesRow1);
        toggleModesRow2 = findViewById(R.id.toggleModesRow2);
        btnDateFrom = findViewById(R.id.btnDateFrom);
        btnDateTo = findViewById(R.id.btnDateTo);
        sliderScore = findViewById(R.id.sliderScore);
        tvScoreRange = findViewById(R.id.tvScoreRange);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvNoHistory.setText("Not signed in.");
            tvNoHistory.setVisibility(View.VISIBLE);
            cardFilters.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            chart.setVisibility(View.GONE);
            return;
        }

        toggleModesRow1.addOnButtonCheckedListener((group, checkedId, isChecked) -> applyFilters());
        toggleModesRow2.addOnButtonCheckedListener((group, checkedId, isChecked) -> applyFilters());
        btnDateFrom.setOnClickListener(v -> pickDate(true));
        btnDateTo.setOnClickListener(v -> pickDate(false));
        sliderScore.addOnChangeListener((slider, value, fromUser) -> applyFilters());
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> clearFilters());

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingGraph = tab.getPosition() == 1;
                updateListOrChartVisibility();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        TrainingSessionRepository repo = new TrainingSessionRepository(this);
        repo.getTrainingHistoryForUser(user.getUid(), sessions -> {
            allSessions = sessions;
            if (sessions.isEmpty()) {
                tvNoHistory.setText("No sessions yet. Start training to see your results here.");
                tvNoHistory.setVisibility(View.VISIBLE);
                cardFilters.setVisibility(View.GONE);
                tabs.setVisibility(View.GONE);
                rv.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
                return;
            }
            cardFilters.setVisibility(View.VISIBLE);
            tabs.setVisibility(View.VISIBLE);
            setupScoreBounds();
            applyFilters();
        });
    }

    // ===================== filters =====================

    private void setupScoreBounds() {
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (TrainingSession s : allSessions) {
            min = Math.min(min, s.getAvgReactionMs());
            max = Math.max(max, s.getAvgReactionMs());
        }
        if (min >= max) max = min + 1;
        scoreBoundMin = min;
        scoreBoundMax = max;
        sliderScore.setValueFrom(min);
        sliderScore.setValueTo(max);
        sliderScore.setValues(min, max);
    }

    private void pickDate(boolean isFrom) {
        Calendar seed = Calendar.getInstance();
        Long current = isFrom ? fromDateMs : toDateMs;
        if (current != null) seed.setTimeInMillis(current);

        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day, isFrom ? 0 : 23, isFrom ? 0 : 59, isFrom ? 0 : 59);
            picked.set(Calendar.MILLISECOND, isFrom ? 0 : 999);

            if (isFrom) {
                fromDateMs = picked.getTimeInMillis();
                btnDateFrom.setText("From: " + filterDateFormat.format(new Date(fromDateMs)));
            } else {
                toDateMs = picked.getTimeInMillis();
                btnDateTo.setText("To: " + filterDateFormat.format(new Date(toDateMs)));
            }
            applyFilters();
        }, seed.get(Calendar.YEAR), seed.get(Calendar.MONTH), seed.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void clearFilters() {
        for (int i = 0; i < toggleModesRow1.getChildCount(); i++) {
            ((MaterialButton) toggleModesRow1.getChildAt(i)).setChecked(true);
        }
        for (int i = 0; i < toggleModesRow2.getChildCount(); i++) {
            ((MaterialButton) toggleModesRow2.getChildAt(i)).setChecked(true);
        }
        fromDateMs = null;
        toDateMs = null;
        btnDateFrom.setText("From: Any");
        btnDateTo.setText("To: Any");
        sliderScore.setValues(scoreBoundMin, scoreBoundMax);
        applyFilters();
    }

    private void applyFilters() {
        if (allSessions.isEmpty()) return;

        Set<String> selectedModes = new HashSet<>();
        List<Integer> checkedIds = new ArrayList<>();
        checkedIds.addAll(toggleModesRow1.getCheckedButtonIds());
        checkedIds.addAll(toggleModesRow2.getCheckedButtonIds());
        for (int id : checkedIds) {
            if (id == R.id.btnModeReaction) selectedModes.add(TrainingMode.REACTION.label);
            else if (id == R.id.btnModeRhythm) selectedModes.add(TrainingMode.RHYTHM.label);
            else if (id == R.id.btnModeFatigue) selectedModes.add(TrainingMode.FATIGUE.label);
            else if (id == R.id.btnModeSimon) selectedModes.add(TrainingMode.SIMON_CUMULATIVE.label);
        }

        List<Float> range = sliderScore.getValues();
        float scoreLow = Collections.min(range);
        float scoreHigh = Collections.max(range);
        tvScoreRange.setText(Math.round(scoreLow) + " – " + Math.round(scoreHigh) + " ms");

        List<TrainingSession> filtered = new ArrayList<>();
        for (TrainingSession s : allSessions) {
            String mode = s.getMode() != null ? s.getMode() : TrainingMode.REACTION.label;
            if (!selectedModes.contains(mode)) continue;
            if (fromDateMs != null && s.getTimestamp() < fromDateMs) continue;
            if (toDateMs != null && s.getTimestamp() > toDateMs) continue;
            if (s.getAvgReactionMs() < scoreLow || s.getAvgReactionMs() > scoreHigh) continue;
            filtered.add(s);
        }

        if (filtered.isEmpty()) {
            tvNoHistory.setText("No sessions match your filters.");
            tvNoHistory.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            chart.setVisibility(View.GONE);
        } else {
            tvNoHistory.setVisibility(View.GONE);
            rv.setAdapter(new HistoryAdapter(filtered, this::openSessionDetail));
            setupHistoryChart(chart, filtered);
            updateListOrChartVisibility();
        }
    }

    private void openSessionDetail(TrainingSession s) {
        Intent i = new Intent(this, SessionDetailActivity.class);
        i.putExtra(SessionDetailActivity.EXTRA_AVG, s.getAvgReactionMs());
        i.putExtra(SessionDetailActivity.EXTRA_BEST, s.getBestReactionMs());
        i.putExtra(SessionDetailActivity.EXTRA_TOTAL, s.getTotalRounds());
        i.putExtra(SessionDetailActivity.EXTRA_CORRECT, s.getCorrectRounds());
        i.putExtra(SessionDetailActivity.EXTRA_DIFFICULTY, s.getDifficulty());
        i.putExtra(SessionDetailActivity.EXTRA_MODE, s.getMode());
        i.putExtra(SessionDetailActivity.EXTRA_TIMESTAMP, s.getTimestamp());
        startActivity(i);
    }

    private void updateListOrChartVisibility() {
        if (tvNoHistory.getVisibility() == View.VISIBLE) return;
        rv.setVisibility(showingGraph ? View.GONE : View.VISIBLE);
        chart.setVisibility(showingGraph ? View.VISIBLE : View.GONE);
    }

    // ===================== chart =====================

    private void setupHistoryChart(LineChart chart, List<TrainingSession> sessions) {
        // sessions arrive newest-first; the chart reads left-to-right chronologically.
        List<TrainingSession> chronological = new ArrayList<>(sessions);
        Collections.reverse(chronological);

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < chronological.size(); i++) {
            entries.add(new Entry(i, chronological.get(i).getAvgReactionMs()));
        }

        LineDataSet set = new LineDataSet(entries, "Avg reaction time (ms)");
        set.setColor(Color.parseColor("#29FF88"));
        set.setCircleColor(Color.parseColor("#29FF88"));
        set.setCircleRadius(4f);
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#29FF88"));
        set.setFillAlpha(30);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setHighlightEnabled(false);

        chart.setData(new LineData(set));
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
}
