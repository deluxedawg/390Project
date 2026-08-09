package com.team5.reflextrainer;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {

    public interface Listener { void onOpen(ConversationSummary conversation); }

    private final List<ConversationSummary> conversations;
    private final String myUid;
    private final Listener listener;

    public ConversationAdapter(List<ConversationSummary> conversations, String myUid, Listener listener) {
        this.conversations = conversations;
        this.myUid = myUid;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ConversationSummary c = conversations.get(position);
        h.username.setText(c.getOtherUsername());
        h.avatar.setImageResource(Avatars.resFor(c.getOtherAvatarId()));

        String prefix = myUid.equals(c.getLastSenderUid()) ? "You: " : "";
        h.preview.setText(prefix + c.getLastMessageText());

        if (c.getLastMessageTimestamp() > 0) {
            h.time.setText(DateUtils.getRelativeTimeSpanString(
                    c.getLastMessageTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        } else {
            h.time.setText("");
        }

        if (c.getUnreadCount() > 0) {
            h.unreadBadge.setVisibility(View.VISIBLE);
            h.unreadBadge.setText(String.valueOf(c.getUnreadCount()));
        } else {
            h.unreadBadge.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onOpen(c));
    }

    @Override
    public int getItemCount() { return conversations.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username, preview, time, unreadBadge;
        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivConvAvatar);
            username = itemView.findViewById(R.id.tvConvUsername);
            preview = itemView.findViewById(R.id.tvConvPreview);
            time = itemView.findViewById(R.id.tvConvTime);
            unreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
        }
    }
}
