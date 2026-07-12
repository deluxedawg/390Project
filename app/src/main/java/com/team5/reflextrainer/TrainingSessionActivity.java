package com.team5.reflextrainer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSessionRepository;

public class TrainingSessionActivity extends AppCompatActivity {

    private TextView tvTimer, tvStatus;
    private Button btnStart, btnStop, btnBack;

    private TrainingSessionRepository repository;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTimeMs = 0L;
    private boolean running = false;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            long elapsed = SystemClock.elapsedRealtime() - startTimeMs;
            tvTimer.setText(HistoryAdapter.formatDuration(elapsed));
            handler.postDelayed(this, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        repository = new TrainingSessionRepository(this);

        tvTimer  = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnStop  = findViewById(R.id.btnStop);
        btnBack  = findViewById(R.id.btnBack);

        btnStart.setOnClickListener(v -> startSession());
        btnStop.setOnClickListener(v -> stopSession());

        btnBack.setOnClickListener(v -> {
            if (running) {
                Toast.makeText(this, "Stop the session before leaving",
                        Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });
    }

    /** FUNC-1 */
    private void startSession() {
        if (running) return;
        running = true;
        startTimeMs = SystemClock.elapsedRealtime();

        tvStatus.setText("Training in progress");
        btnStart.setEnabled(false);
        btnStop.setEnabled(true);

        handler.post(tick);
    }

    /** FUNC-2 + DATA-2 */
    private void stopSession() {
        if (!running) return;
        running = false;
        handler.removeCallbacks(tick);

        long elapsed = SystemClock.elapsedRealtime() - startTimeMs;
        tvTimer.setText(HistoryAdapter.formatDuration(elapsed));
        tvStatus.setText("Session terminated");

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Not signed in — session not saved",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // persist to the local database, tagged with this user's uid
        repository.saveTrainingSession(user.getUid(), elapsed, "Terminated");

        Toast.makeText(this,
                "Session saved (" + HistoryAdapter.formatDuration(elapsed) + ")",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(tick);
    }
}