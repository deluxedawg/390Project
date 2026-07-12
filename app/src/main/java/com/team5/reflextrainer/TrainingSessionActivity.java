package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Random;

public class TrainingSessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        Button btnStartTrainingSession = findViewById(R.id.btnStartTrainingSession);
        btnStartTrainingSession.setOnClickListener(v -> {
            // Create a SessionCls object with dummy data
            Random random = new Random();
            double avg = 200 + random.nextDouble() * 100;
            double best = 150 + random.nextDouble() * 50;
            int mistakes = random.nextInt(5);
            
            SessionCls newSession = new SessionCls(avg, best, 5.0, mistakes, new Date());
            
            // Add it to the SessionManager
            SessionManager.getInstance().addSession(newSession);
            
            Toast.makeText(this, "Session Saved! Check History.", Toast.LENGTH_SHORT).show();
        });

        Button btnStopTraining = findViewById(R.id.btnStopTraining);
        btnStopTraining.setOnClickListener(v -> {
            // Simply finish the activity to go back to MainActivity
            finish();
        });
    }
}