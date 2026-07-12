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

        Button btnStartTraining = findViewById(R.id.btnStartTraining);
        btnStartTraining.setOnClickListener(v -> startActivity(new Intent(this, TrainingSessionActivity.class)));

        findViewById(R.id.btnViewHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.tvSensorStatus).setOnClickListener(v -> startActivity(new Intent(this, ConnectedDevicesActivity.class)));

        findViewById(R.id.btnConnectedDevices).setOnClickListener(v -> startActivity(new Intent(this, ConnectedDevicesActivity.class)));

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}