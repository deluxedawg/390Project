package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IncomingChallengeAdapter
        extends RecyclerView.Adapter<IncomingChallengeAdapter.VH> {

    public interface Listener { void onPlay(Challenge c); }

    private final List<Challenge> items;
    private final Listener listener;

    public IncomingChallengeAdapter(List<Challenge> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge_incoming, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Challenge c = items.get(position);
        h.name.setText(c.getFromUsername() + " challenged you");
        h.detail.setText("Beat " + c.getFromScore() + " ms avg · "
                + c.getDifficulty() + " · " + c.getRounds() + " rounds");
        h.play.setOnClickListener(v -> listener.onPlay(c));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, detail;
        View play;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvChallenger);
            detail = itemView.findViewById(R.id.tvChallengeDetail);
            play = itemView.findViewById(R.id.btnPlay);
        }
    }
}
