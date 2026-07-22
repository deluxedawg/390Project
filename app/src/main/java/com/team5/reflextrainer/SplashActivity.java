package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MS = 1500;   // short — just a brand flash

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent next;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                next = new Intent(this, MainActivity.class);   // already logged in
            } else {
                next = new Intent(this, LoginActivity.class);  // needs to log in
            }
            startActivity(next);
            finish();   // remove splash from back stack
        }, SPLASH_MS);
    }
}