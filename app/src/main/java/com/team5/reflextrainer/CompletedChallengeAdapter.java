package com.team5.reflextrainer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompletedChallengeAdapter
        extends RecyclerView.Adapter<CompletedChallengeAdapter.VH> {

    private final List<Challenge> items;
    private final String myUid;

    public CompletedChallengeAdapter(List<Challenge> items, String myUid) {
        this.items = items;
        this.myUid = myUid;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge_completed, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Challenge c = items.get(position);
        boolean iAmChallenger = myUid.equals(c.getFromUid());

        String opponent = iAmChallenger ? c.getToUsername() : c.getFromUsername();
        int myScore = iAmChallenger ? c.getFromScore() : c.getToScore();
        int theirScore = iAmChallenger ? c.getToScore() : c.getFromScore();

        h.opponent.setText("vs " + opponent + "  ·  " + c.getDifficulty());
        h.scores.setText(myScore + " ms  vs  " + theirScore + " ms");

        if (c.getWinnerUid() == null || c.getWinnerUid().isEmpty()) {
            h.outcome.setText("TIE");
            h.outcome.setTextColor(Color.parseColor("#8A94A6"));
        } else if (myUid.equals(c.getWinnerUid())) {
            h.outcome.setText("WON");
            h.outcome.setTextColor(Color.parseColor("#00E5A0"));
        } else {
            h.outcome.setText("LOST");
            h.outcome.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView opponent, scores, outcome;
        VH(@NonNull View itemView) {
            super(itemView);
            opponent = itemView.findViewById(R.id.tvOpponent);
            scores = itemView.findViewById(R.id.tvScores);
            outcome = itemView.findViewById(R.id.tvOutcome);
        }
    }
}
