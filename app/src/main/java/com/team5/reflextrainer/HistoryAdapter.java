package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<SessionCls> sessionList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<SessionCls> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SessionCls session = sessionList.get(position);
        holder.tvDate.setText(dateFormat.format(session.getSessionDate()));
        holder.tvAvg.setText(String.format(Locale.getDefault(), "Avg: %.2f ms", session.getAvgReactionTime()));
        holder.tvBest.setText(String.format(Locale.getDefault(), "Best: %.2f ms", session.getBestReactionTime()));
        holder.tvMistakes.setText(String.format(Locale.getDefault(), "Mistakes: %d", session.getMistakes()));
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvAvg, tvBest, tvMistakes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvSessionDate);
            tvAvg = itemView.findViewById(R.id.tvAvgTime);
            tvBest = itemView.findViewById(R.id.tvBestTime);
            tvMistakes = itemView.findViewById(R.id.tvMistakes);
        }
    }
}
