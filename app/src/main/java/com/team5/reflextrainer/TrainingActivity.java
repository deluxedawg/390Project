package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TrainingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        Button back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finish()); // returns to Home
    }
}