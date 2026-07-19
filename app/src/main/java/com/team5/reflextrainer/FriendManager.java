package com.team5.reflextrainer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendManager {

    private static final String PROFILES = "profiles";
    private static final String REQUESTS = "friendRequests";
    private static final String FRIENDS  = "friends";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ==================== PHASE 2: send a request ====================

    public interface SendCallback {
        void onSuccess(String toUsername);
        void onError(String message);
    }

    /** Look up a user by username and send them a friend request. */
    public void sendFriendRequest(String targetUsername, SendCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            callback.onError("Not signed in");
            return;
        }

        // 1. find the target profile by username
        db.collection(PROFILES)
                .whereEqualTo("username", targetUsername)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onError("No user found with that username");
                        return;
                    }

                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) query.getDocuments().get(0);
                    UserProfile target = doc.toObject(UserProfile.class);

                    if (target.getUid().equals(me.getUid())) {
                        callback.onError("You can't add yourself");
                        return;
                    }

                    // 2. get my own username, then write the request
                    db.collection(PROFILES).document(me.getUid()).get()
                            .addOnSuccessListener(myDoc -> {
                                String myUsername = (myDoc.exists() && myDoc.getString("username") != null)
                                        ? myDoc.getString("username") : me.getEmail();

                                FriendRequest req = new FriendRequest(
                                        me.getUid(), myUsername,
                                        target.getUid(), target.getUsername(),
                                        "pending");

                                // request id = fromUid_toUid, prevents duplicate requests
                                String reqId = me.getUid() + "_" + target.getUid();
                                db.collection(REQUESTS).document(reqId).set(req)
                                        .addOnSuccessListener(x -> callback.onSuccess(target.getUsername()))
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== PHASE 3: incoming requests ====================

    public interface RequestsCallback {
        void onResult(List<FriendRequest> requests);
        void onError(String message);
    }

    /** Load pending requests sent TO me. */
    public void loadIncomingRequests(RequestsCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            callback.onError("Not signed in");
            return;
        }

        db.collection(REQUESTS)
                .whereEqualTo("toUid", me.getUid())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    List<FriendRequest> list = new ArrayList<>();
                    query.forEach(doc -> list.add(doc.toObject(FriendRequest.class)));
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== PHASE 3: accept / reject ====================

    public interface ActionCallback {
        void onDone();
        void onError(String message);
    }

    /** Accept a request: create a friendship on both sides, then remove the request. */
    public void acceptRequest(FriendRequest req, ActionCallback callback) {
        String reqId = req.getFromUid() + "_" + req.getToUid();

        // friend entry in MY (the receiver's) list -> the sender becomes my friend
        Map<String, Object> forMe = new HashMap<>();
        forMe.put("uid", req.getFromUid());
        forMe.put("username", req.getFromUsername());

        // friend entry in THEIR (the sender's) list -> I become their friend
        Map<String, Object> forThem = new HashMap<>();
        forThem.put("uid", req.getToUid());
        forThem.put("username", req.getToUsername());

        db.collection(FRIENDS).document(req.getToUid())
                .collection("list").document(req.getFromUid()).set(forMe)
                .addOnSuccessListener(a ->
                        db.collection(FRIENDS).document(req.getFromUid())
                                .collection("list").document(req.getToUid()).set(forThem)
                                .addOnSuccessListener(b ->
                                        db.collection(REQUESTS).document(reqId).delete()
                                                .addOnSuccessListener(c -> callback.onDone())
                                                .addOnFailureListener(e -> callback.onError(e.getMessage())))
                                .addOnFailureListener(e -> callback.onError(e.getMessage())))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Reject a request: just delete it. */
    public void rejectRequest(FriendRequest req, ActionCallback callback) {
        String reqId = req.getFromUid() + "_" + req.getToUid();
        db.collection(REQUESTS).document(reqId).delete()
                .addOnSuccessListener(x -> callback.onDone())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== PHASE 3: friends list ====================

    public interface FriendsCallback {
        void onResult(List<UserProfile> friends);
        void onError(String message);
    }

    /** Load my accepted friends. */
    public void loadFriends(FriendsCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) {
            callback.onError("Not signed in");
            return;
        }

        db.collection(FRIENDS).document(me.getUid()).collection("list")
                .get()
                .addOnSuccessListener(query -> {
                    List<UserProfile> list = new ArrayList<>();
                    query.forEach(doc -> {
                        UserProfile p = new UserProfile();
                        p.setUid(doc.getString("uid"));
                        p.setUsername(doc.getString("username"));
                        list.add(p);
                    });
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}