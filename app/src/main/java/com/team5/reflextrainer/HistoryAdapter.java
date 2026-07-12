package com.team5.reflextrainer;

import android.graphics.Color;
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

    private final List<TrainingSession> sessionList;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<TrainingSession> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrainingSession session = sessionList.get(position);

        holder.tvDate.setText(dateFormat.format(new Date(session.getTimestamp())));
        holder.tvDuration.setText(formatDuration(session.getDurationMs()));

        if ("Terminated".equals(session.getStatus())) {
            holder.tvStatus.setText("Terminated \u2014 stopped before completion");
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvStatus.setText("Completed");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static String formatDuration(long ms) {
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis  = ms % 1000;
        return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDuration, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate     = itemView.findViewById(R.id.tvSessionDate);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
        }
    }
}