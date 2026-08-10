package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team5.reflextrainer.data.TrainingMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Read-only detail view for a session already logged in History — no AI coach, no
 *  per-round chart, since per-round reaction times aren't persisted past the summary screen. */
public class SessionDetailActivity extends AppCompatActivity {

    public static final String EXTRA_AVG = "avg";
    public static final String EXTRA_BEST = "best";
    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_CORRECT = "correct";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_TIMESTAMP = "timestamp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_detail);

        int avg = getIntent().getIntExtra(EXTRA_AVG, 0);
        int best = getIntent().getIntExtra(EXTRA_BEST, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        int correct = getIntent().getIntExtra(EXTRA_CORRECT, 0);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = TrainingMode.REACTION.label;
        long timestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, 0);

        ((TextView) findViewById(R.id.tvSubtitle)).setText(mode.toUpperCase(Locale.getDefault()) + " · " + difficulty);

        TextView tvAvg = findViewById(R.id.tvAvg);
        TextView tvBest = findViewById(R.id.tvBest);
        TextView tvAccuracy = findViewById(R.id.tvAccuracy);
        tvAvg.setText(avg > 0 ? avg + " ms" : "—");
        tvBest.setText(best > 0 ? best + " ms" : "—");
        int pct = total > 0 ? Math.round((correct * 100f) / total) : 0;
        tvAccuracy.setText(pct + "%");

        ((TextView) findViewById(R.id.tvDetailMode)).setText(mode);
        ((TextView) findViewById(R.id.tvDetailDifficulty)).setText(difficulty != null ? difficulty : "—");
        ((TextView) findViewById(R.id.tvDetailRounds)).setText(correct + " / " + total + " correct");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        ((TextView) findViewById(R.id.tvDetailDate)).setText(timestamp > 0 ? dateFormat.format(new Date(timestamp)) : "—");

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
    }
}
