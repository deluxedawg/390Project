package com.team5.reflextrainer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ChallengeResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_result);

        boolean won = getIntent().getBooleanExtra("won", false);
        boolean tie = getIntent().getBooleanExtra("tie", false);
        int myScore = getIntent().getIntExtra("myScore", 0);
        int theirScore = getIntent().getIntExtra("theirScore", 0);
        String opponent = getIntent().getStringExtra("opponent");
        if (opponent == null) opponent = "your opponent";

        TextView tvOutcome = findViewById(R.id.tvOutcome);
        TextView tvOpponentLine = findViewById(R.id.tvOpponentLine);
        TextView tvMyScore = findViewById(R.id.tvMyScore);
        TextView tvTheirScore = findViewById(R.id.tvTheirScore);
        TextView tvTheirLabel = findViewById(R.id.tvTheirLabel);

        if (tie) {
            tvOutcome.setText("TIE");
            tvOutcome.setTextColor(Color.parseColor("#8A94A6"));
            tvOpponentLine.setText("Dead even with " + opponent);
        } else if (won) {
            tvOutcome.setText("YOU WON");
            tvOutcome.setTextColor(Color.parseColor("#00E5A0"));
            tvOpponentLine.setText("You beat " + opponent);
        } else {
            tvOutcome.setText("YOU LOST");
            tvOutcome.setTextColor(Color.parseColor("#FF5252"));
            tvOpponentLine.setText(opponent + " was faster");
        }

        tvMyScore.setText(myScore > 0 ? myScore + " ms" : "—");
        tvTheirScore.setText(theirScore + " ms");
        tvTheirLabel.setText(opponent.toUpperCase());

        // carry the session stats forward to the summary
        final int avg = getIntent().getIntExtra("avg", myScore);
        final int best = getIntent().getIntExtra("best", 0);
        final int total = getIntent().getIntExtra("total", 0);
        final int correct = getIntent().getIntExtra("correct", 0);
        final String difficulty = getIntent().getStringExtra("difficulty");
        final ArrayList<Integer> rounds = getIntent().getIntegerArrayListExtra("rounds");

        findViewById(R.id.btnDetails).setOnClickListener(v -> {
            Intent i = new Intent(this, SummaryActivity.class);
            i.putExtra("avg", avg);
            i.putExtra("best", best);
            i.putExtra("total", total);
            i.putExtra("correct", correct);
            i.putExtra("difficulty", difficulty);
            i.putIntegerArrayListExtra("rounds", rounds == null ? new ArrayList<>() : rounds);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        });
    }
}