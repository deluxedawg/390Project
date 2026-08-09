package com.team5.reflextrainer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team5.reflextrainer.hardware.ESPBluetoothManager;
import com.team5.reflextrainer.hardware.SensorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simon Says / Memory Mode: The app shows a sequence of 4 colors,
 * and the user must repeat them in the correct order.
 */
public class SimonSaysActivity extends AppCompatActivity implements ESPBluetoothManager.Listener {

    // Set to true to use the on-screen buttons instead of the physical sensor
    private static final boolean SIMULATION_MODE = true;
    
    private TextView tvInstruction, tvResult, tvProgress;
    private Button btnStartGame;
    private View simulationLayout;
    
    // Colored simulation buttons (Green, Red, Yellow, Blue)
    private Button[] simButtons;

    private final List<Byte> sequence = new ArrayList<>();
    private int userStep = 0;
    private boolean isDisplayingSequence = false;
    private boolean isGameOver = false;

    private final Random random = new Random();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simon_says);

        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        tvProgress = findViewById(R.id.tvProgress);
        btnStartGame = findViewById(R.id.btnStartGame);
        simulationLayout = findViewById(R.id.simulationLayout);

        // Buttons for indices 0, 1, 2, 3
        simButtons = new Button[] {
            findViewById(R.id.btnSim0),
            findViewById(R.id.btnSim1),
            findViewById(R.id.btnSim2),
            findViewById(R.id.btnSim3)
        };

        btnStartGame.setOnClickListener(v -> startGame());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (SIMULATION_MODE) {
            setupSimulationButtons();
        } else {
            simulationLayout.setVisibility(View.GONE);
        }
    }

    private void startGame() {
        sequence.clear();
        isGameOver = false;
        btnStartGame.setVisibility(View.GONE);
        tvResult.setText("");
        tvProgress.setText("Score: 0");
        addNewStep();
    }

    private void addNewStep() {
        // Pick random target: 0-3 for the 4 colors
        byte next = (byte) random.nextInt(4);
        sequence.add(next);
        displaySequence();
    }

    private void displaySequence() {
        isDisplayingSequence = true;
        userStep = 0;
        tvInstruction.setText("Watch the sequence...");
        tvInstruction.setTextColor(getColor(R.color.danger));
        tvResult.setText("...");
        
        for (int i = 0; i < sequence.size(); i++) {
            final int index = i;
            mainHandler.postDelayed(() -> {
                byte target = sequence.get(index);
                blinkButton(target);
                
                // If it's the last item in the sequence, let the user start
                if (index == sequence.size() - 1) {
                    mainHandler.postDelayed(() -> {
                        isDisplayingSequence = false;
                        tvInstruction.setText("Repeat the sequence!");
                        tvInstruction.setTextColor(getColor(R.color.color_set));
                        tvResult.setText("?");
                    }, 1000);
                }
            }, (i + 1) * 1000);
        }
    }

    private void blinkButton(byte targetId) {
        if (targetId >= 0 && targetId < 4) {
            Button b = simButtons[targetId];
            // Highlight
            b.setAlpha(1.0f);
            // Dim after 600ms
            mainHandler.postDelayed(() -> b.setAlpha(0.4f), 600);
        }
    }

    private void handleInput(byte input) {
        if (isDisplayingSequence || isGameOver) return;

        blinkButton(input);

        if (input == sequence.get(userStep)) {
            userStep++;
            
            if (userStep == sequence.size()) {
                // Completed the whole sequence correctly
                tvResult.setText("Excellent!");
                tvResult.setTextColor(getColor(R.color.accent));
                tvProgress.setText("Score: " + sequence.size());
                mainHandler.postDelayed(this::addNewStep, 1200);
            }
        } else {
            gameOver();
        }
    }

    private void gameOver() {
        isGameOver = true;
        tvInstruction.setText("Wrong! Game Over");
        tvInstruction.setTextColor(getColor(R.color.danger));
        tvResult.setText("Final Score: " + (sequence.size() - 1));
        btnStartGame.setVisibility(View.VISIBLE);
        btnStartGame.setText("Try Again");
    }

    private void setupSimulationButtons() {
        for (int i = 0; i < 4; i++) {
            final byte index = (byte) i;
            simButtons[i].setOnClickListener(v -> handleInput(index));
        }
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        if (SIMULATION_MODE) return;
        mainHandler.post(() -> {
            if (!connected) {
                tvInstruction.setText("Sensor Disconnected");
            }
        });
    }

    @Override
    public void onMessage(SensorMessage message) {
        // The hardware sends RESP_RESULT when a button is pressed
        if (message.response == SensorMessage.RESP_RESULT) {
            mainHandler.post(() -> handleInput(message.targetId));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!SIMULATION_MODE) {
            ESPBluetoothManager.getInstance().setListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
    }
}