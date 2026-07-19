package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.VH> {

    public interface Listener {
        void onAccept(FriendRequest req);
        void onReject(FriendRequest req);
    }

    private final List<FriendRequest> requests;
    private final Listener listener;

    public RequestAdapter(List<FriendRequest> requests, Listener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FriendRequest req = requests.get(position);
        h.name.setText(req.getFromUsername());
        h.accept.setOnClickListener(v -> listener.onAccept(req));
        h.reject.setOnClickListener(v -> listener.onReject(req));
    }

    @Override
    public int getItemCount() { return requests.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        View accept, reject;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvRequestName);
            accept = itemView.findViewById(R.id.btnAccept);
            reject = itemView.findViewById(R.id.btnReject);
        }
    }
}