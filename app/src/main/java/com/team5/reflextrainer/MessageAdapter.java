package com.team5.reflextrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;

    private final List<Message> messages;
    private final String myUid;

    public MessageAdapter(List<Message> messages, String myUid) {
        this.messages = messages;
        this.myUid = myUid;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderUid().equals(myUid) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_SENT ? R.layout.item_message_sent : R.layout.item_message_received;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.text.setText(messages.get(position).getText());
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView text;
        VH(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tvMessageText);
        }
    }
}
