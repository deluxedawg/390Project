package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        TextView tvNoHistory = findViewById(R.id.tvNoHistory);

        List<SessionCls> sessions = SessionManager.getInstance().getSessions();

        if (sessions.isEmpty()) {
            tvNoHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            tvNoHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            rvHistory.setAdapter(new HistoryAdapter(sessions));
        }

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());
    }
}