package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etUsername, etEmail, etPassword, etConfirm, etHeight, etWeight;
    private CheckBox cbResearchConsent;
    private View groupResearchData;
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
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        cbResearchConsent = findViewById(R.id.cbResearchConsent);
        groupResearchData = findViewById(R.id.groupResearchData);
        progress = findViewById(R.id.progress);

        cbResearchConsent.setOnCheckedChangeListener((buttonView, isChecked) ->
                groupResearchData.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        findViewById(R.id.tvLearnMore).setOnClickListener(v -> showResearchConsentDialog());

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString();
            String confirm = etConfirm.getText().toString();
            boolean researchConsent = cbResearchConsent.isChecked();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (validate(username, email, pass, confirm) && validateResearchData(researchConsent, heightStr, weightStr)) {
                double heightCm = researchConsent ? Double.parseDouble(heightStr) : 0;
                double weightKg = researchConsent ? Double.parseDouble(weightStr) : 0;
                register(username, email, pass, researchConsent, heightCm, weightKg);
            }
        });

        View btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        EdgeToEdge.applyTopInsetMargin(btnInfo);
    }

    private void showResearchConsentDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.research_consent_title)
                .setMessage(R.string.research_consent_body)
                .setPositiveButton("Got it", null)
                .show();
    }

    // AUTH-1.3b: only enforced when the user has opted in to sharing height/weight
    private boolean validateResearchData(boolean researchConsent, String heightStr, String weightStr) {
        if (!researchConsent) return true;

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            toast("Enter your height and weight, or uncheck the research option");
            return false;
        }
        double height, weight;
        try {
            height = Double.parseDouble(heightStr);
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            toast("Height and weight must be numbers");
            return false;
        }
        if (height < 50 || height > 250) {
            toast("Enter a height between 50 and 250 cm");
            return false;
        }
        if (weight < 20 || weight > 300) {
            toast("Enter a weight between 20 and 300 kg");
            return false;
        }
        return true;
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
    private void register(String username, String email, String pass,
                           boolean researchConsent, double heightCm, double weightKg) {
        progress.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // account created AND signed in automatically
                        String uid = auth.getCurrentUser().getUid();
                        new ProfileManager().saveProfile(uid, username, email, researchConsent, heightCm, weightKg);

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