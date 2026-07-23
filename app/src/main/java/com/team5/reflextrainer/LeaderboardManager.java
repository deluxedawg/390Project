package com.team5.reflextrainer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardManager {

    private static final String COLLECTION = "leaderboard";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Submit a reaction time. Only overwrites the stored score if this one is
     * faster (lower), so the leaderboard holds each user's best reaction time.
     */
    public void submitScore(int reactionMs) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || reactionMs <= 0) return;   // ignore invalid times

        String uid = user.getUid();
        String name = (user.getEmail() != null) ? user.getEmail() : "Anonymous";

        db.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    boolean shouldWrite = true;
                    if (snapshot.exists()) {
                        Long existing = snapshot.getLong("bestReactionMs");
                        if (existing != null && existing <= reactionMs) {
                            shouldWrite = false;   // stored time is already faster
                        }
                    }
                    if (shouldWrite) {
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("displayName", name);
                        entry.put("bestReactionMs", reactionMs);
                        db.collection(COLLECTION).document(uid).set(entry);
                    }
                });
    }

    public interface LeaderboardCallback {
        void onResult(List<LeaderboardEntry> entries);
        void onError(String message);
    }

    /** Read the top scores, fastest reaction first. */
    public void loadLeaderboard(LeaderboardCallback callback) {
        db.collection(COLLECTION)
                .orderBy("bestReactionMs", Query.Direction.ASCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(query -> {
                    List<LeaderboardEntry> list = new ArrayList<>();
                    query.forEach(doc -> list.add(doc.toObject(LeaderboardEntry.class)));
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Load only the entries for the given set of uids (friends + me). */
    public void loadFriendsLeaderboard(java.util.Set<String> allowedUids, LeaderboardCallback callback) {
        db.collection(COLLECTION)
                .orderBy("bestReactionMs", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<LeaderboardEntry> list = new ArrayList<>();
                    query.forEach(doc -> {
                        if (allowedUids.contains(doc.getId())) {     // doc id == uid
                            list.add(doc.toObject(LeaderboardEntry.class));
                        }
                    });
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface ScoreCallback {
        void onResult(int bestAvgMs);   // 0 if none yet
        void onError(String message);
    }

    /** Fetch my own best average from the leaderboard. */
    public void getMyBestScore(ScoreCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.onError("Not signed in"); return; }

        db.collection(COLLECTION).document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getLong("bestReactionMs") != null) {
                        callback.onResult(doc.getLong("bestReactionMs").intValue());
                    } else {
                        callback.onResult(0);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}