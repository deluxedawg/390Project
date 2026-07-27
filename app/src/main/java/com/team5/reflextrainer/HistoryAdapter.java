package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.reflextrainer.data.TrainingSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<TrainingSession> sessions;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<TrainingSession> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        TrainingSession s = sessions.get(position);
        h.tvDifficulty.setText(s.getDifficulty());
        h.tvDate.setText(dateFormat.format(new Date(s.getTimestamp()))
                + "  ·  " + s.getCorrectRounds() + "/" + s.getTotalRounds() + " correct");
        h.tvReaction.setText("avg " + s.getAvgReactionMs() + " ms");
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDifficulty, tvDate, tvReaction;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }
}