package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendPickerAdapter extends RecyclerView.Adapter<FriendPickerAdapter.VH> {

    public interface Listener { void onPick(UserProfile friend); }

    private final List<UserProfile> friends;
    private final Listener listener;

    public FriendPickerAdapter(List<UserProfile> friends, Listener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_picker, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserProfile f = friends.get(position);
        h.username.setText(f.getUsername());
        h.avatar.setImageResource(Avatars.resFor(f.getAvatarId()));
        h.itemView.setOnClickListener(v -> listener.onPick(f));
    }

    @Override
    public int getItemCount() { return friends.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivPickerAvatar);
            username = itemView.findViewById(R.id.tvPickerUsername);
        }
    }
}
