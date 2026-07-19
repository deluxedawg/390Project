package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etUsername, etEmail, etPassword, etConfirm;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();

            if (validate(username, email, pass, confirm)) {
                register(username, email, pass);
            }
        });
    }

    // AUTH-1.3: validate before ever touching Firebase
    private boolean validate(String username, String email, String pass, String confirm) {
        if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            toast("Please fill in all fields");
            return false;
        }
        if (username.length() < 3) {
            toast("Username must be at least 3 characters");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email");
            return false;
        }
        if (pass.length() < 6) {                 // Firebase minimum
            toast("Password must be at least 6 characters");
            return false;
        }
        if (!pass.equals(confirm)) {
            toast("Passwords do not match");
            return false;
        }
        return true;
    }

    // AUTH-1.2: create the account
    private void register(String username, String email, String pass) {
        progress.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // account created AND signed in automatically
                        String uid = auth.getCurrentUser().getUid();
                        new ProfileManager().saveProfile(uid, username, email);

                        startActivity(new Intent(this, MainActivity.class));
                        finishAffinity(); // clear login/register from back stack
                    } else {
                        Exception e = task.getException();
                        String msg;
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            msg = "That email is already registered";
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            msg = "Password is too weak";
                        } else {
                            msg = "Registration failed: "
                                    + (e != null ? e.getMessage() : "unknown error");
                        }
                        toast(msg);
                    }
                });
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}