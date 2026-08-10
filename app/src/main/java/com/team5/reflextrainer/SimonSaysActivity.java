package com.team5.reflextrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSessionRepository;
import com.team5.reflextrainer.hardware.ESPBluetoothManager;
import com.team5.reflextrainer.hardware.SensorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimonSaysActivity extends AppCompatActivity implements ESPBluetoothManager.Listener {

    private static final int NUM_TARGETS = 4;       // buttons 0-3
    private static final int STEP_DISPLAY_MS = 600; // how long each pattern step shows

    private TextView tvRound, tvInstruction;
    private Button btnStart;

    private final List<Byte> sequence = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int displayIndex = 0;
    private int maxSequenceLength = 0;

    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    private boolean acceptingInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simon_says);

        tvRound = findViewById(R.id.tvRound);
        tvInstruction = findViewById(R.id.tvInstruction);
        btnStart = findViewById(R.id.btnStart);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnStart.setOnClickListener(v -> onStartPressed());

        sessionRepository = new TrainingSessionRepository(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user != null) ? user.getUid() : "unknown_user";
    }

    @Override
    protected void onResume() {
        super.onResume();
        ESPBluetoothManager.getInstance().setListener(this);
    }

    private void onStartPressed() {
        sequence.clear();
        maxSequenceLength = 0;
        btnStart.setEnabled(false);
        startNextLevel();
    }

    private void startNextLevel() {
        sequence.add((byte) random.nextInt(NUM_TARGETS));
        tvRound.setText("Round " + sequence.size());
        showPattern();
    }

    private void showPattern() {
        handler.removeCallbacksAndMessages(null);
        acceptingInput = false;
        displayIndex = 0;
        tvInstruction.setText("Watch...");
        handler.postDelayed(this::playNextPatternStep, 400);
    }

    private void playNextPatternStep() {
        if (displayIndex >= sequence.size()) {
            tvInstruction.setText("Your turn!");
            acceptingInput = true;
            tvInstruction.setBackgroundColor(getColor(android.R.color.transparent));
            sendSequenceToDevice();
            return;
        }
        byte step = sequence.get(displayIndex);
        tvInstruction.setText("");
        tvInstruction.setBackgroundColor(colorForStep(step));
        displayIndex++;
        handler.postDelayed(() -> {
            tvInstruction.setBackgroundColor(getColor(android.R.color.transparent));
            handler.postDelayed(this::playNextPatternStep, 200);
        }, STEP_DISPLAY_MS);
    }

    private int colorForStep(byte step) {
        switch (step) {
            case 0: return getColor(android.R.color.holo_green_light);
            case 1: return getColor(android.R.color.holo_blue_light);
            case 2: return getColor(android.R.color.holo_orange_light);
            case 3: return getColor(android.R.color.holo_red_light);
            default: return getColor(android.R.color.transparent);
        }
    }

    private void sendSequenceToDevice() {
        if (!ESPBluetoothManager.getInstance().isConnected()) {
            tvInstruction.setText("Sensor not connected");
            btnStart.setEnabled(true);
            return;
        }
        byte[] seqArray = new byte[sequence.size()];
        for (int i = 0; i < sequence.size(); i++) seqArray[i] = sequence.get(i);
        ESPBluetoothManager.getInstance().sendStartSimon(seqArray);
    }

    @Override
    public void onMessage(SensorMessage message) {
        Log.e("SIMON_DEBUG", "onMessage: response=0x" + Integer.toHexString(message.response & 0xFF)
                + " targetId=" + message.targetId); // ADD
        runOnUiThread(() -> handleMessage(message));
    }

    private void handleMessage(SensorMessage message) {
        if (!acceptingInput) return;
        if (message.response == SensorMessage.RESP_SIMON_PROGRESS) {
            tvInstruction.setText("Correct (" + message.targetId + "/" + sequence.size() + ")");
        }
        if (message.response == SensorMessage.RESP_RESULT) {
            Log.e("SIMON_DEBUG", "RESULT branch: targetId=" + message.targetId + " OUTCOME_CORRECT=" + SensorMessage.OUTCOME_CORRECT + " match=" + (message.targetId == SensorMessage.OUTCOME_CORRECT));
            if (message.targetId == SensorMessage.OUTCOME_CORRECT) {
                maxSequenceLength = sequence.size();
                tvInstruction.setText("Sequence complete!");
                handler.postDelayed(this::startNextLevel, 1000);
            } else {
                endGame();
            }
        }
    }

    private void endGame() {
        tvInstruction.setText("Game Over — reached round " + sequence.size());
        btnStart.setEnabled(true);

        ESPBluetoothManager.getInstance().sendReset();

        sessionRepository.saveSession(
                currentUserId,
                0,                    // avg reaction time - not tracked in this basic version
                maxSequenceLength,    // "best" field repurposed as max sequence reached
                sequence.size(),      // total rounds attempted
                maxSequenceLength,    // correct count = longest successfully completed sequence
                "Cumulative",
                TrainingMode.SIMON_CUMULATIVE.label
        );

        if (maxSequenceLength > 0) {
            new LeaderboardManager("simon_leaderboard", true).submitScore(maxSequenceLength);
        }
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        if (!connected) {
            runOnUiThread(() -> tvInstruction.setText("Sensor disconnected"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        ESPBluetoothManager.getInstance().sendReset();
    }
}