package com.team5.reflextrainer;

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

import java.util.Random;


public class TrainingActivity extends AppCompatActivity implements ESPBluetoothManager.Listener  {

    private TextView tvInstruction;
    private TextView tvResult;
    private Button btnStartRound;

    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    private final Random random = new Random();
    private final Handler mainhandler = new Handler(Looper.getMainLooper());

    private static final int TIMEOUT_MS = 5000;
    private static final int NUM_TARGETS = 4;

    private static final String TRAINING_MODE = "reflex_buttons";
    private static final String DIFFICULTY = "normal"; // we cahnge this later


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        Button back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish()); // returns to Home

        tvInstruction = findViewById(R.id.tvInstruction);
        tvResult = findViewById(R.id.tvResult);
        btnStartRound = findViewById(R.id.btnStartRound);
        btnStartRound.setOnClickListener(v-> startRound());

        sessionRepository = new TrainingSessionRepository(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (user!=null)? user.getUid():"unknown_user";
    }

    @Override
    protected void onResume(){
        super.onResume();
        ESPBluetoothManager.getInstance().setListener(this);
    }

    private void startRound(){
        if(!ESPBluetoothManager.getInstance().isConnected()){
            tvInstruction.setText("Sensor not connected");
            return;
        }

        btnStartRound.setEnabled(false);
        tvResult.setText("");
        tvInstruction.setText("Ready...");

        byte target = pickRandomTarget();
        ESPBluetoothManager.getInstance().sendStartChallenge(target, TIMEOUT_MS);
    }

    private byte pickRandomTarget(){
        if (random.nextInt(5) == 0) {
            return SensorMessage.TARGET_SHAKE_IMU;
        }
        return (byte) random.nextInt(NUM_TARGETS);
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        mainhandler.post(()->{
            if(!connected){
                tvInstruction.setText("Sensor Disconnected");
                btnStartRound.setEnabled(false);
            }   else{
                btnStartRound.setEnabled(true);
            }
        });
    }
    @Override
    public void onMessage(SensorMessage message) {
        mainhandler.post(()-> handleMessage(message));
    }


    private void handleMessage(SensorMessage message) {
        if (message.response == SensorMessage.RESP_ACK) {
            String targetDesc = describeTarget(message.targetId);
            tvInstruction.setText(targetDesc + "!");
            return;
        }

        if (message.response == SensorMessage.RESP_RESULT) {
            String outcomeText;
            switch (message.targetId) {
                case SensorMessage.OUTCOME_CORRECT:
                    outcomeText = "Correct!" + message.reactionTimeMs + "ms";
                    break;
                case SensorMessage.OUTCOME_WRONG_BTN:
                    outcomeText = "Wrong!" + message.reactionTimeMs + "ms";
                    break;
                case SensorMessage.OUTCOME_TIMEOUT:
                    outcomeText = "Too Slow!";
                    break;
                default:
                    outcomeText = "Error unknown result";
            }

            tvResult.setText(outcomeText);
            tvInstruction.setText("Press Start for the next round");
            btnStartRound.setEnabled(true);

            sessionRepository.saveTrainingSession(currentUserId,message.reactionTimeMs,TRAINING_MODE,DIFFICULTY);
        }
    }


    private String describeTarget(byte targetId) {
        if(targetId == SensorMessage.TARGET_SHAKE_IMU){
            return "SHAKE IT";
        }
        return "Press button " + targetId;
    }


}