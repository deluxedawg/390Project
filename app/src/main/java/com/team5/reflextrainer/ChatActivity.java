package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_OTHER_UID = "otherUid";
    public static final String EXTRA_OTHER_USERNAME = "otherUsername";
    public static final String EXTRA_OTHER_AVATAR_ID = "otherAvatarId";

    private final MessageManager mm = new MessageManager();
    private final List<Message> messages = new ArrayList<>();

    private RecyclerView rvMessages;
    private TextView tvNoMessages;
    private EditText etMessage;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ListenerRegistration messagesRegistration;

    private String myUid;
    private String otherUid;
    private String otherUsername;
    private int otherAvatarId;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUid = getIntent().getStringExtra(EXTRA_OTHER_UID);
        otherUsername = getIntent().getStringExtra(EXTRA_OTHER_USERNAME);
        otherAvatarId = getIntent().getIntExtra(EXTRA_OTHER_AVATAR_ID, 0);
        conversationId = MessageManager.conversationId(myUid, otherUid);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Android 15+ draws edge-to-edge by default; without this the header (and its back
        // button) renders under the status bar and becomes unclickable in that region.
        View header = findViewById(R.id.chatHeader);
        int headerBasePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), headerBasePaddingTop + topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ((ImageView) findViewById(R.id.ivChatAvatar)).setImageResource(Avatars.resFor(otherAvatarId));
        ((TextView) findViewById(R.id.tvChatUsername)).setText(otherUsername);

        tvNoMessages = findViewById(R.id.tvNoMessages);
        rvMessages = findViewById(R.id.rvMessages);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messages, myUid);
        rvMessages.setAdapter(adapter);

        etMessage = findViewById(R.id.etMessage);
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesRegistration = mm.listenForMessages(conversationId, new MessageManager.MessagesListener() {
            @Override
            public void onMessages(List<Message> result) {
                messages.clear();
                messages.addAll(result);
                tvNoMessages.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    rvMessages.scrollToPosition(messages.size() - 1);
                }
            }
            @Override public void onError(String message) { toast(message); }
        });
        mm.markRead(conversationId, new MessageManager.ActionCallback() {
            @Override public void onDone() { }
            @Override public void onError(String message) { }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messagesRegistration != null) {
            messagesRegistration.remove();
            messagesRegistration = null;
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        mm.sendMessage(otherUid, otherUsername, otherAvatarId, text, new MessageManager.ActionCallback() {
            @Override public void onDone() { etMessage.setText(""); }
            @Override public void onError(String message) { toast(message); }
        });
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
