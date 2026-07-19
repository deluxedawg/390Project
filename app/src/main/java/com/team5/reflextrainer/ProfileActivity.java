package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddFriend).setOnClickListener(v ->
                startActivity(new Intent(this, AddFriendActivity.class)));

        findViewById(R.id.btnFriends).setOnClickListener(v ->
                startActivity(new Intent(this, FriendsActivity.class)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new ProfileManager().loadProfile(user.getUid(), new ProfileManager.ProfileCallback() {
            @Override
            public void onResult(UserProfile profile) {
                tvUsername.setText(profile.getUsername());
                tvEmail.setText(profile.getEmail());
            }
            @Override
            public void onError(String message) {
                tvUsername.setText("Unknown");
                tvEmail.setText(user.getEmail());   // fall back to auth email
                Toast.makeText(ProfileActivity.this,
                        "Could not load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}