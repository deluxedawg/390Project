package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (user != null) {
            tvWelcome.setText("Logged in as: " + user.getEmail());
        }

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        Button startTraining = findViewById(R.id.btnStartTraining);
        startTraining.setOnClickListener(v ->
                startActivity(new Intent(this, TrainingActivity.class)));

        Button viewHistory = findViewById(R.id.btnViewHistory);
        viewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }
}