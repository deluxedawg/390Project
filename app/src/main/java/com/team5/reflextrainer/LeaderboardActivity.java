package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        RecyclerView rv = findViewById(R.id.rvLeaderboard);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
        new LeaderboardManager().submitScore(250);   // TEMP - remove after testing
        new LeaderboardManager().loadLeaderboard(new LeaderboardManager.LeaderboardCallback() {
            @Override
            public void onResult(List<LeaderboardEntry> entries) {
                if (entries.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                } else {
                    rv.setAdapter(new LeaderboardAdapter(entries));
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(LeaderboardActivity.this,
                        "Could not load leaderboard: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}