package com.team5.reflextrainer;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fatigue Training: 3 rounds of (exercise prompt -> reaction-time test), then an in-place
 * result screen. Saved to History like any other mode, tagged with {@link TrainingMode#FATIGUE}
 * and the body-part category as its "difficulty" detail.
 */
public class FatigueTrainingActivity extends AppCompatActivity {

    private static final int NUM_ROUNDS = 3;
    private static final int REACT_ATTEMPTS_PER_ROUND = 5;
    private static final int REACT_TIMEOUT_MS = 1500;
    private static final int REACT_FEEDBACK_DELAY_MS = 700;
    private static final int MISS = -1;

    private enum Phase { EXERCISE, REACT_WAIT, REACT_GO, REACT_FEEDBACK, RESULT }
    private Phase phase = Phase.EXERCISE;

    private View groupExercise, groupResult;
    private TextView tvRoundProgress, tvExerciseName, tvExerciseDetail, tvCountdown, tvReactBox, tvReactAttempt;
    private Button btnExerciseDone, btnDone, btnBack;
    private TextView tvAvgResult, tvTrendResult;
    private TextView tvRound1Result, tvRound2Result, tvRound3Result;
    private BarChart chartFatigue;
    private View cardDemo;
    private VideoView videoDemo;
    private ImageView imageDemo;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private FatigueExercises.Category category;
    private List<FatigueExercise> rounds;
    private final List<Integer> reactionTimes = new ArrayList<>();   // one averaged score per checkpoint
    private final List<Integer> reactAttemptTimes = new ArrayList<>();   // raw attempts within the current checkpoint
    private int currentRound = 0;
    private int reactAttempt = 0;
    private int totalAttempts = 0;   // across all checkpoints, for the saved session's totalRounds
    private int totalHits = 0;       // across all checkpoints, for the saved session's correctRounds

    private CountDownTimer countDownTimer;
    private long reactStartMs;
    private Runnable reactTimeoutRunnable;

    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fatigue_training);

        String categoryName = getIntent().getStringExtra(FatigueCategoryActivity.EXTRA_CATEGORY);
        category = categoryName != null
                ? FatigueExercises.Category.valueOf(categoryName) : FatigueExercises.Category.CARDIO;
        rounds = FatigueExercises.pickThree(category, random);

        sessionRepository = new TrainingSessionRepository(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user != null) ? user.getUid() : null;

        groupExercise = findViewById(R.id.groupExercise);
        groupResult = findViewById(R.id.groupResult);
        tvRoundProgress = findViewById(R.id.tvRoundProgress);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvExerciseDetail = findViewById(R.id.tvExerciseDetail);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvReactBox = findViewById(R.id.tvReactBox);
        tvReactAttempt = findViewById(R.id.tvReactAttempt);
        cardDemo = findViewById(R.id.cardDemo);
        videoDemo = findViewById(R.id.videoDemo);
        imageDemo = findViewById(R.id.imageDemo);
        btnExerciseDone = findViewById(R.id.btnExerciseDone);
        btnDone = findViewById(R.id.btnDone);
        btnBack = findViewById(R.id.btnBack);
        tvAvgResult = findViewById(R.id.tvAvgResult);
        tvTrendResult = findViewById(R.id.tvTrendResult);
        tvRound1Result = findViewById(R.id.tvRound1Result);
        tvRound2Result = findViewById(R.id.tvRound2Result);
        tvRound3Result = findViewById(R.id.tvRound3Result);
        chartFatigue = findViewById(R.id.chartFatigue);

        tvReactBox.setOnClickListener(v -> onReactTap());
        btnBack.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> finish());

        beginRound();
    }

    // ===================== exercise phase =====================

    private void beginRound() {
        phase = Phase.EXERCISE;
        FatigueExercise exercise = rounds.get(currentRound);

        tvRoundProgress.setText("ROUND " + (currentRound + 1) + " OF " + NUM_ROUNDS);
        tvExerciseName.setText(exercise.name);
        tvReactBox.setVisibility(View.GONE);
        tvReactAttempt.setVisibility(View.GONE);
        showDemo(exercise.demoAsset);

        if (exercise.timed) {
            tvExerciseDetail.setText("Keep going for " + exercise.seconds + "s");
            btnExerciseDone.setVisibility(View.GONE);
            tvCountdown.setVisibility(View.VISIBLE);
            startCountdown(exercise.seconds);
        } else {
            tvExerciseDetail.setText("Do " + exercise.reps + " reps");
            tvCountdown.setVisibility(View.GONE);
            btnExerciseDone.setVisibility(View.VISIBLE);
            btnExerciseDone.setOnClickListener(v -> startReactionTest());
        }
    }

    private void startCountdown(int seconds) {
        tvCountdown.setText(String.valueOf(seconds));
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long msLeft) {
                tvCountdown.setText(String.valueOf(Math.round(msLeft / 1000f)));
            }
            @Override
            public void onFinish() {
                startReactionTest();
            }
        }.start();
    }

    // ===================== exercise demo clip =====================

    /**
     * Shows a looping demo clip above the exercise prompt: assets/exercises/{@code slug}.gif
     * (via Glide) if present, else assets/exercises/{@code slug}.mp4 (via VideoView), else
     * nothing at all. Missing files are a graceful fallback, not an error — demo clips are
     * supplied separately, most exercises won't have one yet.
     */
    private void showDemo(String slug) {
        hideDemo();
        if (slug == null) return;

        if (demoAssetExists(slug + ".gif")) {
            cardDemo.setVisibility(View.VISIBLE);
            imageDemo.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asGif()
                    .load("file:///android_asset/exercises/" + slug + ".gif")
                    .into(imageDemo);
        } else if (demoAssetExists(slug + ".mp4")) {
            cardDemo.setVisibility(View.VISIBLE);
            videoDemo.setVisibility(View.VISIBLE);
            videoDemo.setVideoURI(Uri.parse("file:///android_asset/exercises/" + slug + ".mp4"));
            videoDemo.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.setVolume(0f, 0f);
            });
            videoDemo.start();
        }
    }

    private void hideDemo() {
        cardDemo.setVisibility(View.GONE);
        videoDemo.setVisibility(View.GONE);
        videoDemo.stopPlayback();
        imageDemo.setVisibility(View.GONE);
        Glide.with(this).clear(imageDemo);
    }

    private boolean demoAssetExists(String fileName) {
        AssetManager assets = getAssets();
        try (java.io.InputStream ignored = assets.open("exercises/" + fileName)) {
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    // ===================== reaction-time test phase =====================

    /** Kicks off the 5-attempt reaction-test block for the current checkpoint. */
    private void startReactionTest() {
        hideDemo();
        tvExerciseDetail.setText("");
        tvCountdown.setVisibility(View.GONE);
        btnExerciseDone.setVisibility(View.GONE);
        reactAttempt = 0;
        reactAttemptTimes.clear();
        startNextReactAttempt();
    }

    private void startNextReactAttempt() {
        phase = Phase.REACT_WAIT;
        tvReactAttempt.setVisibility(View.VISIBLE);
        tvReactAttempt.setText("REACTION TEST — ATTEMPT " + (reactAttempt + 1) + " OF " + REACT_ATTEMPTS_PER_ROUND);

        tvReactBox.setVisibility(View.VISIBLE);
        tvReactBox.setText("Wait...");
        tvReactBox.setTextColor(getColor(R.color.danger));

        int delay = 800 + random.nextInt(1500);
        handler.postDelayed(() -> {
            phase = Phase.REACT_GO;
            tvReactBox.setText("TAP NOW!");
            tvReactBox.setTextColor(getColor(R.color.accent));
            reactStartMs = System.currentTimeMillis();

            reactTimeoutRunnable = () -> {
                if (phase == Phase.REACT_GO) recordReactAttempt(MISS);
            };
            handler.postDelayed(reactTimeoutRunnable, REACT_TIMEOUT_MS);
        }, delay);
    }

    private void onReactTap() {
        if (phase != Phase.REACT_GO) return;
        if (reactTimeoutRunnable != null) handler.removeCallbacks(reactTimeoutRunnable);
        int elapsed = (int) (System.currentTimeMillis() - reactStartMs);
        recordReactAttempt(elapsed);
    }

    /** Records one of the 5 raw attempts, shows brief feedback, then moves to the next attempt or checkpoint. */
    private void recordReactAttempt(int ms) {
        phase = Phase.REACT_FEEDBACK;
        reactAttemptTimes.add(ms);
        totalAttempts++;
        if (ms != MISS) totalHits++;
        tvReactBox.setText(ms == MISS ? "Too slow!" : ms + " ms");
        tvReactBox.setTextColor(getColor(ms == MISS ? R.color.danger : R.color.accent));

        reactAttempt++;
        handler.postDelayed(() -> {
            if (reactAttempt < REACT_ATTEMPTS_PER_ROUND) {
                startNextReactAttempt();
            } else {
                finishReactionCheckpoint();
            }
        }, REACT_FEEDBACK_DELAY_MS);
    }

    /** Averages this checkpoint's 5 attempts (excluding misses) into one score for the round. */
    private void finishReactionCheckpoint() {
        int sum = 0, hits = 0;
        for (int t : reactAttemptTimes) {
            if (t != MISS) { sum += t; hits++; }
        }
        reactionTimes.add(hits > 0 ? sum / hits : MISS);

        currentRound++;
        if (currentRound < NUM_ROUNDS) {
            beginRound();
        } else {
            showResults();
        }
    }

    // ===================== results =====================

    private void showResults() {
        phase = Phase.RESULT;
        groupExercise.setVisibility(View.GONE);
        groupResult.setVisibility(View.VISIBLE);
        btnDone.setVisibility(View.VISIBLE);

        int sum = 0, hitCount = 0, best = 0;
        for (int t : reactionTimes) {
            if (t != MISS) {
                sum += t;
                hitCount++;
                if (best == 0 || t < best) best = t;
            }
        }
        int avg = hitCount > 0 ? sum / hitCount : 0;
        tvAvgResult.setText(hitCount > 0 ? avg + " ms avg" : "—");

        Integer first = reactionTimes.get(0) != MISS ? reactionTimes.get(0) : null;
        Integer last = reactionTimes.get(NUM_ROUNDS - 1) != MISS ? reactionTimes.get(NUM_ROUNDS - 1) : null;
        if (first != null && last != null) {
            int delta = last - first;
            tvTrendResult.setText(delta > 0
                    ? delta + "ms slower by round " + NUM_ROUNDS + " — fatigue is catching up"
                    : delta < 0
                            ? (-delta) + "ms faster by round " + NUM_ROUNDS + " — held steady under fatigue"
                            : "Steady the whole way through");
        } else {
            tvTrendResult.setText("Not enough hits to show a trend");
        }

        TextView[] rows = { tvRound1Result, tvRound2Result, tvRound3Result };
        for (int i = 0; i < NUM_ROUNDS; i++) {
            int t = reactionTimes.get(i);
            String result = (t == MISS) ? "All 5 missed" : t + " ms avg";
            rows[i].setText("Round " + (i + 1) + " · " + rounds.get(i).name + " — " + result);
        }

        setupChart();

        if (currentUserId != null) {
            sessionRepository.saveSession(currentUserId, avg, best, totalAttempts, totalHits,
                    category.label, TrainingMode.FATIGUE.label);
        }
        if (avg > 0) {
            new LeaderboardManager("fatigue_leaderboard").submitScore(avg);
        }
    }

    private void setupChart() {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < reactionTimes.size(); i++) {
            int t = reactionTimes.get(i);
            entries.add(new BarEntry(i, t == MISS ? 0 : t));
        }

        BarDataSet set = new BarDataSet(entries, "Reaction time (ms)");
        int[] colors = new int[reactionTimes.size()];
        for (int i = 0; i < reactionTimes.size(); i++) {
            colors[i] = (reactionTimes.get(i) == MISS)
                    ? Color.parseColor("#FF4557") : Color.parseColor("#29FF88");
        }
        set.setColors(colors);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        chartFatigue.setData(data);

        chartFatigue.getDescription().setEnabled(false);
        chartFatigue.getLegend().setEnabled(false);
        chartFatigue.setTouchEnabled(false);
        chartFatigue.setDrawGridBackground(false);
        chartFatigue.setExtraBottomOffset(4f);

        XAxis xAxis = chartFatigue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#8C96A6"));
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "R" + (Math.round(value) + 1);
            }
        });

        YAxis left = chartFatigue.getAxisLeft();
        left.setTextColor(Color.parseColor("#8C96A6"));
        left.setGridColor(Color.parseColor("#22FFFFFF"));
        left.setAxisMinimum(0f);
        chartFatigue.getAxisRight().setEnabled(false);

        chartFatigue.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) countDownTimer.cancel();
        videoDemo.stopPlayback();
    }
}
