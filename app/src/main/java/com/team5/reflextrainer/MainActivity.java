package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.team5.reflextrainer.hardware.ESPBluetoothManager;
import com.team5.reflextrainer.hardware.SensorMessage;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements ESPBluetoothManager.Listener {

    private TextView tvSensorStatus;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), results -> {
        boolean allGranted = true;
        for (Boolean granted : results.values()) {
            if (!granted) allGranted = false;
        }
        if(allGranted) {
            connectToSensor();
        } else {
            updateSensorStatus(SensorStatus.DISCONNECTED);
        }
    });

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

        Button startTraining = findViewById(R.id.btnStart);
        startTraining.setOnClickListener(v ->
                startActivity(new Intent(this, LevelSelectActivity.class)));

        Button viewHistory = findViewById(R.id.btnViewHistory);
        viewHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        Button leaderboard = findViewById(R.id.btnLeaderboard);
        leaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        Button profile = findViewById(R.id.btnProfile);
        profile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        Button challenges = findViewById(R.id.btnChallenges);
        challenges.setOnClickListener(v ->
                startActivity(new Intent(this, ChallengesActivity.class)));

        ESPBluetoothManager.getInstance().setListener(this);
        checkPermissionsAndConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!ESPBluetoothManager.getInstance().isConnected()){
            checkPermissionsAndConnect();
        }
    }

    private void checkPermissionsAndConnect() {
        java.util.List<String> needed = new java.util.ArrayList<>();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            needed.add(Manifest.permission.BLUETOOTH_CONNECT);
            needed.add(Manifest.permission.BLUETOOTH_SCAN);
        }   else {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        java.util.List<String> missing = new java.util.ArrayList<>();
        for(String perm : needed) {
            if(ContextCompat.checkSelfPermission(this, perm)!= PackageManager.PERMISSION_GRANTED){
                missing.add(perm);
            }
        }

        if(missing.isEmpty()){
            connectToSensor();
        }   else {
            permissionLauncher.launch(missing.toArray(new String[0]));
        }
    }


    private void connectToSensor() {
        try {
            android.bluetooth.BluetoothManager systemBtManager =
                    (android.bluetooth.BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = (systemBtManager != null) ? systemBtManager.getAdapter() : null;

            if (adapter == null || !adapter.isEnabled()) {
                updateSensorStatus(SensorStatus.DISCONNECTED);
                return;
            }

            // double-check the connect permission is actually granted before touching bonded devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                updateSensorStatus(SensorStatus.DISCONNECTED);
                return;
            }

            updateSensorStatus(SensorStatus.CONNECTING);
            ESPBluetoothManager.getInstance().connect(adapter);

        } catch (SecurityException e) {
            // permission missing or revoked at the OS level -> just show disconnected
            updateSensorStatus(SensorStatus.DISCONNECTED);
        } catch (Exception e) {
            // any other Bluetooth failure (no adapter, emulator, etc.) -> disconnected, don't crash
            updateSensorStatus(SensorStatus.DISCONNECTED);
        }
    }

    @Override
    public void onConnectionChanged(boolean connected, boolean connecting) {
        runOnUiThread(()->{
            if (connected) updateSensorStatus(SensorStatus.CONNECTED);
            else if (connecting) updateSensorStatus(SensorStatus.CONNECTING);
            else updateSensorStatus(SensorStatus.DISCONNECTED);
        });
    }

    @Override
    public void onMessage(SensorMessage message) {

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