package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        findViewById(R.id.cardReaction).setOnClickListener(v ->
                startActivity(new Intent(this, LevelSelectActivity.class)));

        findViewById(R.id.cardRhythm).setOnClickListener(v ->
                startActivity(new Intent(this, RhythmActivity.class)));

        findViewById(R.id.cardFatigue).setOnClickListener(v ->
                startActivity(new Intent(this, FatigueCategoryActivity.class)));

        findViewById(R.id.cardMemory).setOnClickListener(v ->
                startActivity(new Intent(this, SimonSaysActivity.class)));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
    }
}