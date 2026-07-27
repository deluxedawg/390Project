package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChallengesActivity extends AppCompatActivity {

    private final ChallengeManager cm = new ChallengeManager();
    private RecyclerView rvIncoming, rvCompleted;
    private TextView tvNoIncoming, tvNoCompleted, tvRecord;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        rvIncoming = findViewById(R.id.rvIncoming);
        rvCompleted = findViewById(R.id.rvCompleted);
        tvNoIncoming = findViewById(R.id.tvNoIncoming);
        tvNoCompleted = findViewById(R.id.tvNoCompleted);
        tvRecord = findViewById(R.id.tvRecord);

        rvIncoming.setLayoutManager(new LinearLayoutManager(this));
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        myUid = (me != null) ? me.getUid() : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncoming();
        loadCompleted();
    }

    private void loadIncoming() {
        cm.loadIncoming(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> list) {
                tvNoIncoming.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvIncoming.setAdapter(new IncomingChallengeAdapter(list, c -> {
                    // launch training with the challenge's settings
                    Intent i = new Intent(ChallengesActivity.this, TrainingActivity.class);
                    i.putExtra(LevelSelectActivity.EXTRA_TIMEOUT, c.getTimeoutMs());
                    i.putExtra(LevelSelectActivity.EXTRA_DIFFICULTY, c.getDifficulty());
                    i.putExtra(LevelSelectActivity.EXTRA_ROUNDS, c.getRounds());
                    i.putExtra("challengeId", c.getChallengeId());   // Stage 3 uses this
                    startActivity(i);
                }));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void loadCompleted() {
        cm.loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> list) {
                tvNoCompleted.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvCompleted.setAdapter(new CompletedChallengeAdapter(list, myUid));

                int wins = 0, losses = 0, ties = 0;
                for (Challenge c : list) {
                    String w = c.getWinnerUid();
                    if (w == null || w.isEmpty()) ties++;
                    else if (myUid.equals(w)) wins++;
                    else losses++;
                }
                tvRecord.setText(wins + "W · " + losses + "L" + (ties > 0 ? " · " + ties + "T" : ""));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}