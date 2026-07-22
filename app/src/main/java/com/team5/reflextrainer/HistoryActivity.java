package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSessionRepository;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Button back = findViewById(R.id.btnBackHome);
        back.setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvHistory);
        TextView tvNoHistory = findViewById(R.id.tvNoHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvNoHistory.setText("Not signed in.");
            tvNoHistory.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
            return;
        }

        TrainingSessionRepository repo = new TrainingSessionRepository(this);
        repo.getTrainingHistoryForUser(user.getUid(), sessions -> {
            if (sessions.isEmpty()) {
                tvNoHistory.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                tvNoHistory.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                rv.setAdapter(new HistoryAdapter(sessions));
            }
        });
    }
}