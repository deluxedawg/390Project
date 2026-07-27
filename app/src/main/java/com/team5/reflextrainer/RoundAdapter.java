package com.team5.reflextrainer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class RoundAdapter extends RecyclerView.Adapter<RoundAdapter.VH> {

    private final List<Integer> times;
    private final int best;

    public RoundAdapter(List<Integer> times, int best) {
        this.times = times;
        this.best = best;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_round, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        int t = times.get(position);
        h.num.setText("Round " + (position + 1));
        h.time.setText(t + " ms");
        // highlight the best round
        h.time.setTextColor(t == best
                ? Color.parseColor("#00E5A0")
                : Color.parseColor("#F5F7FA"));
    }

    @Override
    public int getItemCount() {
        return times.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView num, time;
        VH(@NonNull View itemView) {
            super(itemView);
            num = itemView.findViewById(R.id.tvRoundNum);
            time = itemView.findViewById(R.id.tvRoundTime);
        }
    }
}
