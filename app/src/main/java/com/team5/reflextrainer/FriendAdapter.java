package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.VH> {

    public interface Listener { void onChallenge(UserProfile friend); }

    private final List<UserProfile> friends;
    private final Listener listener;

    public FriendAdapter(List<UserProfile> friends, Listener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserProfile f = friends.get(position);
        h.name.setText(f.getUsername());
        h.avatar.setImageResource(Avatars.resFor(f.getAvatarId()));
        h.challenge.setOnClickListener(v -> listener.onChallenge(f));
    }

    @Override
    public int getItemCount() { return friends.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        ImageView avatar;
        View challenge;
        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvFriendName);
            avatar = itemView.findViewById(R.id.ivFriendAvatar);
            challenge = itemView.findViewById(R.id.btnChallenge);
        }
    }
}
