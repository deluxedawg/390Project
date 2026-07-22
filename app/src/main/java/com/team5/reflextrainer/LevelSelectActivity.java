package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;

public class LevelSelectActivity extends AppCompatActivity {

    public static final String EXTRA_TIMEOUT = "timeout_ms";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_ROUNDS = "rounds";

    private MaterialButtonToggleGroup toggleRounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        toggleRounds = findViewById(R.id.toggleRounds);
        toggleRounds.check(R.id.round10);   // default to 10

        findViewById(R.id.cardEasy).setOnClickListener(v -> startTraining(2000, "Easy"));
        findViewById(R.id.cardMedium).setOnClickListener(v -> startTraining(1000, "Medium"));
        findViewById(R.id.cardHard).setOnClickListener(v -> startTraining(500, "Hard"));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
    }

    private int getSelectedRounds() {
        int checked = toggleRounds.getCheckedButtonId();
        if (checked == R.id.round5) return 5;
        if (checked == R.id.round20) return 20;
        return 10;   // default / round10
    }

    private void startTraining(int timeoutMs, String difficulty) {
        Intent i = new Intent(this, TrainingActivity.class);
        i.putExtra(EXTRA_TIMEOUT, timeoutMs);
        i.putExtra(EXTRA_DIFFICULTY, difficulty);
        i.putExtra(EXTRA_ROUNDS, getSelectedRounds());
        startActivity(i);
        finish();
    }
}