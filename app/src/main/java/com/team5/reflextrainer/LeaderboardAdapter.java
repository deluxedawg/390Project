package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> entries;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry e = entries.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(e.getDisplayName());
        holder.tvScore.setText(String.format(Locale.US, "%d ms", e.getBestReactionMs()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank  = itemView.findViewById(R.id.tvRank);
            tvName  = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}