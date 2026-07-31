package com.team5.reflextrainer;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RhythmActivity extends AppCompatActivity {

    private TextView tvPhase, tvInstruction, tvFeedback;
    private Button btnAction;
    private RingView ringView;

    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int drumSoundId;

    private BeatTrack track;
    private List<Long> beatTimes;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // window (ms) before a beat during which the ring is visible / a tap counts
    private static final long RING_LEAD = 900;

    private int nextBeatIndex = 0;
    private boolean autoPlayDrums = false;       // true in listen phase
    private boolean scoring = false;             // true in replicate phase
    private final List<Long> offsets = new ArrayList<>();

    private boolean[] beatScored;

    private enum Phase { IDLE, LISTEN, REPLICATE, DONE }
    private Phase phase = Phase.IDLE;
    private Runnable onPhaseComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rhythm);

        tvPhase = findViewById(R.id.tvPhase);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnAction = findViewById(R.id.btnAction);
        ringView = findViewById(R.id.ringView);

        ringView.setOnClickListener(v -> onTap());

        // our locked-in track: 110 BPM, first beat at 4450ms, 32 beats
        track = new BeatTrack("Track 1", 110, 4450, 55, R.raw.track1);
        beatTimes = track.beatTimes();
        beatScored = new boolean[beatTimes.size()];

        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        drumSoundId = soundPool.load(this, R.raw.drum_hit, 1);

        btnAction.setOnClickListener(v -> startListen());
    }

    // ---------------- LISTEN ----------------

    private void startListen() {
        phase = Phase.LISTEN;
        autoPlayDrums = true;
        scoring = false;
        tvPhase.setText("LISTEN");
        tvInstruction.setText("Feel the beat — drums are shown for you");
        tvFeedback.setText("");
        btnAction.setEnabled(false);

        playTrack(() -> runOnUiThread(this::readyToReplicate));
    }

    private void readyToReplicate() {
        stopTrack();
        phase = Phase.REPLICATE;
        tvPhase.setText("YOUR TURN");
        tvInstruction.setText("Tap the ring on every beat");
        btnAction.setText("Start Tapping");
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(v -> startReplicate());
    }

    // ---------------- REPLICATE ----------------

    private void startReplicate() {
        phase = Phase.REPLICATE;
        autoPlayDrums = false;
        scoring = true;
        offsets.clear();
        for (int i = 0; i < beatScored.length; i++) beatScored[i] = false;
        tvFeedback.setText("");
        btnAction.setEnabled(false);

        playTrack(() -> runOnUiThread(this::finishSession));
    }

    // ---------------- playback + beat watcher ----------------

    private void playTrack(Runnable onComplete) {
        nextBeatIndex = 0;
        onPhaseComplete = onComplete;
        mediaPlayer = MediaPlayer.create(this, track.rawResId);
        mediaPlayer.start();
        handler.post(beatWatcher);
    }

    private void stopTrack() {
        handler.removeCallbacks(beatWatcher);
        if (mediaPlayer != null) {
            try { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        ringView.clearRing();
    }

    private final Runnable beatWatcher = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) return;
            long pos;
            try { pos = mediaPlayer.getCurrentPosition(); }
            catch (Exception e) { return; }

            while (nextBeatIndex < beatTimes.size()
                    && pos >= beatTimes.get(nextBeatIndex)) {
                if (autoPlayDrums) playDrum();
                nextBeatIndex++;
            }

            // once we're past the last beat (+ a little tail), end the phase
            long lastBeat = beatTimes.get(beatTimes.size() - 1);
            if (pos >= lastBeat + 600) {
                if (onPhaseComplete != null) onPhaseComplete.run();
                return;   // stop the watcher
            }

            updateRing(pos);
            handler.postDelayed(this, 16);
        }
    };

    private void updateRing(long pos) {
        long nextBeat = -1;
        for (long b : beatTimes) {
            if (b >= pos) { nextBeat = b; break; }
        }
        if (nextBeat < 0) { ringView.clearRing(); return; }

        long remaining = nextBeat - pos;
        if (remaining <= RING_LEAD) {
            ringView.setProgress(remaining / (float) RING_LEAD);
        } else {
            ringView.clearRing();
        }
    }

    // ---------------- tapping / scoring ----------------

    private void onTap() {
        if (!scoring || mediaPlayer == null) return;

        playDrum();

        long pos;
        try { pos = mediaPlayer.getCurrentPosition(); }
        catch (Exception e) { return; }

        int nearest = -1;
        long bestDist = Long.MAX_VALUE;
        for (int i = 0; i < beatTimes.size(); i++) {
            if (beatScored[i]) continue;
            long d = Math.abs(beatTimes.get(i) - pos);
            if (d < bestDist) { bestDist = d; nearest = i; }
        }
        if (nearest < 0) return;

        if (bestDist <= RING_LEAD) {
            beatScored[nearest] = true;
            offsets.add(bestDist);
            showTapFeedback(bestDist);
        }
    }

    private void showTapFeedback(long offset) {
        String txt;
        if (offset <= 40)       txt = "PERFECT";
        else if (offset <= 90)  txt = "GREAT";
        else if (offset <= 160) txt = "GOOD";
        else                    txt = "OFF";
        tvFeedback.setText(txt + "  " + offset + "ms");
    }

    // ---------------- result ----------------

    private void finishSession() {
        stopTrack();
        phase = Phase.DONE;
        scoring = false;

        int avg;
        if (offsets.isEmpty()) {
            avg = 0;
        } else {
            long sum = 0;
            for (long o : offsets) sum += o;
            avg = (int) (sum / offsets.size());
        }

        int hits = offsets.size();
        tvPhase.setText("RESULT");
        tvInstruction.setText(hits + " / " + beatTimes.size() + " beats hit");
        if (avg > 0) {
            tvFeedback.setText("Avg " + avg + "ms off");
            new RhythmLeaderboardManager().submitScore(avg);
        } else {
            tvFeedback.setText("No beats hit");
        }

        btnAction.setText("Done");
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(v -> finish());
        ringView.clearRing();
    }

    private void playDrum() {
        soundPool.play(drumSoundId, 1f, 1f, 1, 0, 1f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        stopTrack();
        if (soundPool != null) soundPool.release();
    }
}
