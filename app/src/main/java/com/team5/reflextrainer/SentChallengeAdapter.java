package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Challenges I sent that the recipient hasn't played yet — nothing to do but wait. */
public class SentChallengeAdapter extends RecyclerView.Adapter<SentChallengeAdapter.VH> {

    private final List<Challenge> items;

    public SentChallengeAdapter(List<Challenge> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge_sent, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Challenge c = items.get(position);
        h.opponent.setText("vs " + c.getToUsername() + "  ·  " + c.getDifficulty());
        h.detail.setText("Your avg: " + c.getFromScore() + " ms  ·  " + c.getRounds() + " rounds");
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView opponent, detail;
        VH(@NonNull View itemView) {
            super(itemView);
            opponent = itemView.findViewById(R.id.tvOpponent);
            detail = itemView.findViewById(R.id.tvChallengeDetail);
        }
    }
}
