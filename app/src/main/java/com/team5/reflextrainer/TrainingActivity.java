package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSessionRepository;
import com.team5.reflextrainer.hardware.ESPBluetoothManager;
import com.team5.reflextrainer.hardware.SensorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrainingActivity extends AppCompatActivity implements ESPBluetoothManager.Listener {

    // ===== TEMP: tap-to-react instead of the physical sensor =====
    private static final boolean SIMULATION_MODE = false;

    private TextView tvInstruction, tvResult, tvProgress;
    private Button btnStartRound;

    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    private final Random random = new Random();
    private final Handler mainhandler = new Handler(Looper.getMainLooper());

    private static final int NUM_TARGETS = 4;
    private static final String TRAINING_MODE = "reflex_buttons";

    private int timeoutMs = 1000;
    private String difficulty = "Medium";
    private int totalRounds = 10;

    // ---- challenge context ----
    private String challengeId = null;            // playing an incoming challenge
    private String challengeToUid = null;         // creating a new challenge
    private String challengeToUsername = null;

    // ---- session state ----
    private int currentRound = 0;
    private final List<Integer> reactionTimes = new ArrayList<>();
    private int correctCount = 0;

    // ---- simulation state ----
    private boolean waitingForTap = false;
    private long roundStartMs = 0L;
    private Runnable simTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        timeoutMs = getIntent().getIntExtra(LevelSelectActivity.EXTRA_TIMEOUT, 1000);
        difficulty = getIntent().getStringExtra(LevelSelectActivity.EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = "Medium";
        totalRounds = getIntent().getIntExtra(LevelSelectActivity.EXTRA_ROUNDS, 10);

        challengeId = getIntent().getStringExtra("challengeId");
        challengeToUid = getIntent().getStringExtra("challengeToUid");
        challengeToUsername = getIntent().getStringExtra("challengeToUsername");

        Button back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish());

        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        tvProgress = findViewById(R.id.tvProgress);
        btnStartRound = findViewById(R.id.btnStartRound);
        btnStartRound.setOnClickListener(v -> onStartPressed());

        sessionRepository = new TrainingSessionRepository(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user != null) ? user.getUid() : "unknown_user";

        if (SIMULATION_MODE) {
            tvResult.setClickable(true);
            tvResult.setOnClickListener(v -> onSimTap());
            tvInstruction.setClickable(true);
            tvInstruction.setOnClickListener(v -> onSimTap());
        }

        tvProgress.setText(difficulty + " · " + totalRounds + " rounds");

        if (challengeToUsername != null) {
            tvInstruction.setText("Challenging " + challengeToUsername + " — press Start");
        } else if (challengeId != null) {
            tvInstruction.setText("Challenge round — press Start");
        } else {
            tvInstruction.setText("Press Start to begin");
        }
    }

    // ===================== session control =====================

    private void onStartPressed() {
        currentRound = 0;
        correctCount = 0;
        reactionTimes.clear();
        tvResult.setText("");
        beginNextRound();
    }

    private void beginNextRound() {
        if (currentRound >= totalRounds) {
            finishSession();
            return;
        }
        currentRound++;
        tvProgress.setText("Round " + currentRound + " / " + totalRounds);
        btnStartRound.setEnabled(false);

        if (SIMULATION_MODE) {
            startSimRound();
        } else {
            if (!ESPBluetoothManager.getInstance().isConnected()) {
                tvInstruction.setText("Sensor not connected");
                btnStartRound.setEnabled(true);
                return;
            }
            tvInstruction.setText("Ready...");
            byte target = pickRandomTarget();
            ESPBluetoothManager.getInstance().sendStartChallenge(target, timeoutMs);
        }
    }

    /** Called once a round produces a result (from tap or sensor). */
    private void recordRound(byte outcome, int reactionMs) {
        String outcomeText;
        if (outcome == SensorMessage.OUTCOME_CORRECT) {
            outcomeText = "Correct! " + reactionMs + " ms";
            reactionTimes.add(reactionMs);
            correctCount++;
        } else if (outcome == SensorMessage.OUTCOME_WRONG_BTN) {
            outcomeText = "Wrong!";
        } else if (outcome == SensorMessage.OUTCOME_TIMEOUT) {
            outcomeText = "Too Slow!";
        } else {
            outcomeText = "Error";
        }
        tvResult.setText(outcomeText);

        mainhandler.postDelayed(this::beginNextRound, 900);
    }

    private void finishSession() {
        int bestCalc = Integer.MAX_VALUE;
        int sum = 0;
        for (int t : reactionTimes) {
            sum += t;
            if (t < bestCalc) bestCalc = t;
        }
        final int avg = reactionTimes.isEmpty() ? 0 : sum / reactionTimes.size();
        final int best = reactionTimes.isEmpty() ? 0 : bestCalc;

        sessionRepository.saveSession(currentUserId, avg, best, totalRounds, correctCount, difficulty);

        if (avg > 0) {
            new LeaderboardManager().submitScore(avg);
        }

        // creating a new challenge -> send my score to the friend
        if (challengeToUid != null && avg > 0) {
            new ChallengeManager().sendChallenge(
                    challengeToUid, challengeToUsername,
                    difficulty, timeoutMs, totalRounds, avg,
                    new ChallengeManager.ActionCallback() {
                        @Override public void onDone() { }
                        @Override public void onError(String m) { }
                    });
        }

        // playing an incoming challenge -> wait for the outcome, then show the result screen
        if (challengeId != null) {
            tvInstruction.setText("Checking result...");
            new ChallengeManager().completeChallengeWithResult(challengeId, avg,
                    new ChallengeManager.ResultCallback() {
                        @Override
                        public void onResult(boolean won, boolean tie, int myScore,
                                             int theirScore, String opponent) {
                            Intent i = new Intent(TrainingActivity.this, ChallengeResultActivity.class);
                            i.putExtra("won", won);
                            i.putExtra("tie", tie);
                            i.putExtra("myScore", myScore);
                            i.putExtra("theirScore", theirScore);
                            i.putExtra("opponent", opponent);
                            i.putExtra("avg", avg);
                            i.putExtra("best", best);
                            i.putExtra("total", totalRounds);
                            i.putExtra("correct", correctCount);
                            i.putExtra("difficulty", difficulty);
                            i.putIntegerArrayListExtra("rounds", new ArrayList<>(reactionTimes));
                            startActivity(i);
                            finish();
                        }
                        @Override
                        public void onError(String m) {
                            goToSummary(avg, best);
                        }
                    });
            return;
        }

        goToSummary(avg, best);
    }

    private void goToSummary(int avg, int best) {
        Intent i = new Intent(this, SummaryActivity.class);
        i.putExtra("avg", avg);
        i.putExtra("best", best);
        i.putExtra("total", totalRounds);
        i.putExtra("correct", correctCount);
        i.putExtra("difficulty", difficulty);
        i.putIntegerArrayListExtra("rounds", new ArrayList<>(reactionTimes));
        if (challengeToUsername != null && avg > 0) {
            i.putExtra("challengeSentTo", challengeToUsername);
        }
        startActivity(i);
        finish();
    }

    // ===================== simulation =====================

    private void startSimRound() {
        int delay = 800 + random.nextInt(1500);
        tvInstruction.setText("Wait...");
        waitingForTap = false;

        mainhandler.postDelayed(() -> {
            tvInstruction.setText("TAP NOW!");
            waitingForTap = true;
            roundStartMs = System.currentTimeMillis();

            simTimeout = () -> {
                if (waitingForTap) {
                    waitingForTap = false;
                    recordRound(SensorMessage.OUTCOME_TIMEOUT, 0);
                }
            };
            mainhandler.postDelayed(simTimeout, timeoutMs);
        }, delay);
    }

    private void onSimTap() {
        if (!SIMULATION_MODE || !waitingForTap) return;
        waitingForTap = false;
        if (simTimeout != null) mainhandler.removeCallbacks(simTimeout);
        int elapsed = (int) (System.currentTimeMillis() - roundStartMs);
        roundStartMs = 0;
        recordRound(SensorMessage.OUTCOME_CORRECT, elapsed);
    }

    // ===================== real sensor =====================

    private byte pickRandomTarget() {
        if (random.nextInt(5) == 0) return SensorMessage.TARGET_SHAKE_IMU;
        return (byte) random.nextInt(NUM_TARGETS);
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        if (SIMULATION_MODE) return;
        mainhandler.post(() -> {
            if (!connected) {
                tvInstruction.setText("Sensor Disconnected");
            }
        });
    }

    @Override
    public void onMessage(SensorMessage message) {
        mainhandler.post(() -> handleMessage(message));
    }

    private void handleMessage(SensorMessage message) {
        if (message.response == SensorMessage.RESP_ACK) {
            tvInstruction.setText(describeTarget(message.targetId) + "!");
            return;
        }
        if (message.response == SensorMessage.RESP_RESULT) {
            recordRound(message.targetId, message.reactionTimeMs);
        }
    }

    private String describeTarget(byte targetId) {
        if (targetId == SensorMessage.TARGET_SHAKE_IMU) return "SHAKE IT";
        return "Press button " + targetId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SIMULATION_MODE) ESPBluetoothManager.getInstance().setListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainhandler.removeCallbacksAndMessages(null);
    }
}