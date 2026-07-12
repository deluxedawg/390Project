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

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        TextView tvNoHistory = findViewById(R.id.tvNoHistory);

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvNoHistory.setText("Not signed in.");
            tvNoHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            return;
        }

        TrainingSessionRepository repository = new TrainingSessionRepository(this);

        // DATA-1/DATA-3: load this user's sessions from the database
        repository.getTrainingHistoryForUser(user.getUid(), sessions -> {
            if (sessions.isEmpty()) {
                tvNoHistory.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            } else {
                tvNoHistory.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                rvHistory.setAdapter(new HistoryAdapter(sessions));
            }
        });
    }
}