package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private final MessageManager mm = new MessageManager();
    private final FriendManager fm = new FriendManager();
    private final List<ConversationSummary> conversations = new ArrayList<>();

    private RecyclerView rvConversations;
    private TextView tvNoConversations;
    private ConversationAdapter adapter;
    private ListenerRegistration conversationsRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        rvConversations = findViewById(R.id.rvConversations);
        tvNoConversations = findViewById(R.id.tvNoConversations);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        adapter = new ConversationAdapter(conversations, myUid, this::openChat);
        rvConversations.setAdapter(adapter);

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewMessage).setOnClickListener(v -> showFriendPicker());

        // Android 15+ draws edge-to-edge by default; without this the top content
        // renders under the status bar.
        View root = findViewById(R.id.inboxRoot);
        int rootBasePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), rootBasePaddingTop + topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    /** Pick a friend to start (or resume) a chat with, without going through the Friends tab. */
    private void showFriendPicker() {
        fm.loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                if (friends.isEmpty()) {
                    toast("Add a friend first");
                    return;
                }

                BottomSheetDialog dialog = new BottomSheetDialog(InboxActivity.this);
                View sheet = LayoutInflater.from(InboxActivity.this)
                        .inflate(R.layout.dialog_friend_picker, null);

                RecyclerView rvPicker = sheet.findViewById(R.id.rvFriendPicker);
                rvPicker.setLayoutManager(new LinearLayoutManager(InboxActivity.this));
                rvPicker.setAdapter(new FriendPickerAdapter(friends, friend -> {
                    dialog.dismiss();
                    openChat(friend);
                }));

                dialog.setContentView(sheet);
                dialog.show();
            }
            @Override public void onError(String message) { toast(message); }
        });
    }

    private void openChat(UserProfile friend) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(ChatActivity.EXTRA_OTHER_UID, friend.getUid());
        i.putExtra(ChatActivity.EXTRA_OTHER_USERNAME, friend.getUsername());
        i.putExtra(ChatActivity.EXTRA_OTHER_AVATAR_ID, friend.getAvatarId());
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        conversationsRegistration = mm.listenForConversations(new MessageManager.ConversationsListener() {
            @Override
            public void onResult(List<ConversationSummary> result) {
                conversations.clear();
                conversations.addAll(result);
                tvNoConversations.setVisibility(conversations.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.notifyDataSetChanged();
            }
            @Override public void onError(String message) { toast(message); }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (conversationsRegistration != null) {
            conversationsRegistration.remove();
            conversationsRegistration = null;
        }
    }

    private void openChat(ConversationSummary conversation) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(ChatActivity.EXTRA_OTHER_UID, conversation.getOtherUid());
        i.putExtra(ChatActivity.EXTRA_OTHER_USERNAME, conversation.getOtherUsername());
        i.putExtra(ChatActivity.EXTRA_OTHER_AVATAR_ID, conversation.getOtherAvatarId());
        startActivity(i);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
