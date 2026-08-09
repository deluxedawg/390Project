package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team5.reflextrainer.data.TrainingMode;
import com.team5.reflextrainer.data.TrainingSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface Listener { void onOpen(TrainingSession session); }

    private final List<TrainingSession> sessions;
    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<TrainingSession> sessions, Listener listener) {
        this.sessions = sessions;
        this.listener = listener;
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
        String mode = s.getMode() != null ? s.getMode() : TrainingMode.REACTION.label;
        h.tvMode.setText(mode.toUpperCase(Locale.getDefault()) + " · " + s.getDifficulty());
        h.tvMode.setTextColor(colorForMode(h.itemView, mode));
        h.tvDate.setText(dateFormat.format(new Date(s.getTimestamp()))
                + "  ·  " + s.getCorrectRounds() + "/" + s.getTotalRounds() + " correct");
        h.tvReaction.setText("avg " + s.getAvgReactionMs() + " ms");
        h.itemView.setOnClickListener(v -> listener.onOpen(s));
    }

    /** Matches the mode colors used on Mode Select / Fatigue Category (accent/amber/danger). */
    private int colorForMode(View anchor, String mode) {
        int colorRes;
        if (TrainingMode.RHYTHM.label.equalsIgnoreCase(mode)) {
            colorRes = R.color.color_set;
        } else if (TrainingMode.FATIGUE.label.equalsIgnoreCase(mode)) {
            colorRes = R.color.danger;
        } else {
            colorRes = R.color.accent;
        }
        return anchor.getContext().getColor(colorRes);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMode, tvDate, tvReaction;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMode = itemView.findViewById(R.id.tvMode);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }
}