package com.team5.reflextrainer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class AddFriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        TextInputEditText etUsername = findViewById(R.id.etFriendUsername);
        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        findViewById(R.id.btnSendRequest).setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            new FriendManager().sendFriendRequest(username, new FriendManager.SendCallback() {
                @Override
                public void onSuccess(String toUsername) {
                    Toast.makeText(AddFriendActivity.this,
                            "Friend request sent to " + toUsername, Toast.LENGTH_SHORT).show();
                    etUsername.setText("");
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(AddFriendActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}