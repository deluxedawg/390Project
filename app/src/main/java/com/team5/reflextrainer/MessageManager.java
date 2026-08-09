package com.team5.reflextrainer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {

    private static final String CONVERSATIONS = "conversations";
    private static final String MESSAGES = "messages";
    private static final String PROFILES = "profiles";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Deterministic thread id: the two uids sorted, so both users converge on the same doc. */
    public static String conversationId(String uidA, String uidB) {
        return uidA.compareTo(uidB) < 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public interface ActionCallback {
        void onDone();
        void onError(String message);
    }

    public interface MessagesListener {
        void onMessages(List<Message> messages);
        void onError(String message);
    }

    public interface ConversationsListener {
        void onResult(List<ConversationSummary> conversations);
        void onError(String message);
    }

    /** Send a message, creating/updating the conversation doc and appending to its messages subcollection. */
    public void sendMessage(String otherUid, String otherUsername, int otherAvatarId,
                             String text, ActionCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        String convId = conversationId(me.getUid(), otherUid);
        long now = System.currentTimeMillis();

        db.collection(PROFILES).document(me.getUid()).get()
                .addOnSuccessListener(myDoc -> {
                    String myUsername = (myDoc.exists() && myDoc.getString("username") != null)
                            ? myDoc.getString("username") : me.getEmail();
                    Long myAvatarLong = myDoc.getLong("avatarId");
                    int myAvatarId = (myAvatarLong != null) ? myAvatarLong.intValue() : 0;

                    Map<String, Object> names = new HashMap<>();
                    names.put(me.getUid(), myUsername);
                    names.put(otherUid, otherUsername);

                    Map<String, Object> avatars = new HashMap<>();
                    avatars.put(me.getUid(), myAvatarId);
                    avatars.put(otherUid, otherAvatarId);

                    Map<String, Object> conv = new HashMap<>();
                    conv.put("participants", Arrays.asList(me.getUid(), otherUid));
                    conv.put("names", names);
                    conv.put("avatars", avatars);
                    conv.put("lastMessageText", text);
                    conv.put("lastMessageTimestamp", now);
                    conv.put("lastSenderUid", me.getUid());

                    db.collection(CONVERSATIONS).document(convId).set(conv, SetOptions.merge())
                            .addOnSuccessListener(x -> db.collection(CONVERSATIONS).document(convId)
                                    .update(FieldPath.of("unread", otherUid), FieldValue.increment(1),
                                            FieldPath.of("unread", me.getUid()), 0)
                                    .addOnSuccessListener(y -> appendMessage(convId, me.getUid(), myUsername, text, now, callback))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage())))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void appendMessage(String convId, String myUid, String myUsername, String text,
                                long now, ActionCallback callback) {
        String msgId = db.collection(CONVERSATIONS).document(convId).collection(MESSAGES).document().getId();
        Message message = new Message(msgId, myUid, myUsername, text, now);
        db.collection(CONVERSATIONS).document(convId).collection(MESSAGES).document(msgId).set(message)
                .addOnSuccessListener(z -> callback.onDone())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Real-time message stream for one conversation, oldest first. Remove the registration in onStop/onDestroy. */
    public ListenerRegistration listenForMessages(String conversationId, MessagesListener listener) {
        return db.collection(CONVERSATIONS).document(conversationId).collection(MESSAGES)
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { listener.onError(error.getMessage()); return; }
                    if (snapshots == null) return;
                    List<Message> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message m = doc.toObject(Message.class);
                        if (m != null) list.add(m);
                    }
                    listener.onMessages(list);
                });
    }

    /** Real-time inbox: every conversation I'm part of, most recent first. Remove the registration in onStop/onDestroy. */
    @SuppressWarnings("unchecked")
    public ListenerRegistration listenForConversations(ConversationsListener listener) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { listener.onError("Not signed in"); return null; }
        String myUid = me.getUid();

        return db.collection(CONVERSATIONS)
                .whereArrayContains("participants", myUid)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { listener.onError(error.getMessage()); return; }
                    if (snapshots == null) return;

                    List<ConversationSummary> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants == null || participants.size() < 2) continue;
                        String otherUid = participants.get(0).equals(myUid) ? participants.get(1) : participants.get(0);

                        Map<String, Object> names = (Map<String, Object>) doc.get("names");
                        Map<String, Object> avatars = (Map<String, Object>) doc.get("avatars");
                        Map<String, Object> unread = (Map<String, Object>) doc.get("unread");

                        String otherUsername = (names != null && names.get(otherUid) != null)
                                ? names.get(otherUid).toString() : "";
                        int otherAvatarId = (avatars != null && avatars.get(otherUid) instanceof Number)
                                ? ((Number) avatars.get(otherUid)).intValue() : 0;
                        int unreadCount = (unread != null && unread.get(myUid) instanceof Number)
                                ? ((Number) unread.get(myUid)).intValue() : 0;

                        String lastMessageText = doc.getString("lastMessageText");
                        Long lastTimestamp = doc.getLong("lastMessageTimestamp");
                        String lastSenderUid = doc.getString("lastSenderUid");

                        list.add(new ConversationSummary(doc.getId(), otherUid, otherUsername, otherAvatarId,
                                lastMessageText != null ? lastMessageText : "",
                                lastTimestamp != null ? lastTimestamp : 0,
                                lastSenderUid != null ? lastSenderUid : "",
                                unreadCount));
                    }
                    listener.onResult(list);
                });
    }

    /** Clear my unread badge for this conversation. */
    public void markRead(String conversationId, ActionCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }
        db.collection(CONVERSATIONS).document(conversationId)
                .update(FieldPath.of("unread", me.getUid()), 0)
                .addOnSuccessListener(x -> callback.onDone())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
