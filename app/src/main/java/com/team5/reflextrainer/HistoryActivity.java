package com.team5.reflextrainer;

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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSession;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button back = findViewById(R.id.btnBackHome);
        back.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvHistory);
        TextView tvNoHistory = findViewById(R.id.tvNoHistory);
        TabLayout tabs = findViewById(R.id.tabHistory);
        LineChart chart = findViewById(R.id.chartHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvNoHistory.setText("Not signed in.");
            tvNoHistory.setVisibility(View.VISIBLE);
            tabs.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            chart.setVisibility(View.GONE);
            return;
        }

        TrainingSessionRepository repo = new TrainingSessionRepository(this);
        repo.getTrainingHistoryForUser(user.getUid(), sessions -> {
            if (sessions.isEmpty()) {
                tvNoHistory.setVisibility(View.VISIBLE);
                tabs.setVisibility(View.GONE);
                rv.setVisibility(View.GONE);
                chart.setVisibility(View.GONE);
            } else {
                tvNoHistory.setVisibility(View.GONE);
                tabs.setVisibility(View.VISIBLE);
                rv.setVisibility(View.VISIBLE);
                rv.setAdapter(new HistoryAdapter(sessions));
                setupHistoryChart(chart, sessions);

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
        });
    }

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