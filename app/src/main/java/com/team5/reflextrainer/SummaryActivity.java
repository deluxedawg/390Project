package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        int avg = getIntent().getIntExtra("avg", 0);
        int best = getIntent().getIntExtra("best", 0);
        int total = getIntent().getIntExtra("total", 0);
        int correct = getIntent().getIntExtra("correct", 0);
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

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            // go back to Home, clearing the training stack
            finish();
        });
    }
}
