package com.team5.reflextrainer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

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

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        if (user != null) {
            tvWelcome.setText("Logged in as: " + user.getEmail());
        }

        // UI-2: sensor connection indicator
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        updateSensorStatus(SensorStatus.DISCONNECTED);

        // navigation (UI-1)
        findViewById(R.id.btnStartTraining).setOnClickListener(v ->
                startActivity(new Intent(this, TrainingSessionActivity.class)));

        findViewById(R.id.btnViewHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.btnConnectedDevices).setOnClickListener(v ->
                startActivity(new Intent(this, ConnectedDevicesActivity.class)));

        tvSensorStatus.setOnClickListener(v ->
                startActivity(new Intent(this, ConnectedDevicesActivity.class)));

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * UI-2: update the sensor connection indicator.
     * Safe to call from any thread (Bluetooth callbacks run off the UI thread).
     */
    public void updateSensorStatus(SensorStatus status) {
        runOnUiThread(() -> {
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
        });
    }
}