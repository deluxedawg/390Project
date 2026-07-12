package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());

        findViewById(R.id.tvGoRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    // Session routing: if already logged in, skip straight to Home
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null) {
            goToHome();
        }
    }

    // AUTH-2.2
    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(View.GONE);
                    android.util.Log.e("AUTH", "login complete, success=" + task.isSuccessful(),
                            task.getException());   // <-- add this line
                    if (task.isSuccessful()) {
                        goToHome();
                    } else {
                        Toast.makeText(this, "Invalid email or password",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}