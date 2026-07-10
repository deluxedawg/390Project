package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextView tvSensorStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        updateSensorStatus(SensorStatus.DISCONNECTED); // default until sensor connects


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

    // the UI-2 method
    public void updateSensorStatus(SensorStatus status) {
        switch (status) {
            case CONNECTED:
                tvSensorStatus.setText("Sensor: Connected");
                tvSensorStatus.setTextColor(Color.parseColor("#2E7D32")); // green
                break;
            case CONNECTING:
                tvSensorStatus.setText("Sensor: Connecting...");
                tvSensorStatus.setTextColor(Color.parseColor("#F9A825")); // amber
                break;
            case DISCONNECTED:
            default:
                tvSensorStatus.setText("Sensor: Disconnected");
                tvSensorStatus.setTextColor(Color.parseColor("#D32F2F")); // red
                break;
        }
    }
}