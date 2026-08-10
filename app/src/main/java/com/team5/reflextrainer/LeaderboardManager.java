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

    private final String collection;
    private final boolean higherIsBetter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Reaction-time leaderboard: lower score wins. */
    public LeaderboardManager() {
        this("leaderboard", false);
    }

    /** Time-based leaderboard in the given collection: lower score wins. */
    public LeaderboardManager(String collection) {
        this(collection, false);
    }

    /**
     * @param collection     Firestore collection backing this leaderboard
     * @param higherIsBetter true for score types like Simon Says' longest sequence,
     *                       where a bigger number is the better result
     */
    public LeaderboardManager(String collection, boolean higherIsBetter) {
        this.collection = collection;
        this.higherIsBetter = higherIsBetter;
    }

    /**
     * Submit a score. Only overwrites the stored score if this one is better
     * (per {@link #higherIsBetter}), so the leaderboard holds each user's best result.
     */
    public void submitScore(int score) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || score <= 0) return;   // ignore invalid scores

        String uid = user.getUid();

        db.collection(collection).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    boolean shouldWrite = true;
                    if (snapshot.exists()) {
                        Long existing = snapshot.getLong("bestReactionMs");
                        if (existing != null) {
                            boolean existingIsBetterOrEqual = higherIsBetter
                                    ? existing >= score
                                    : existing <= score;
                            if (existingIsBetterOrEqual) shouldWrite = false;
                        }
                    }
                    if (shouldWrite) {
                        db.collection("profiles").document(uid).get()
                                .addOnSuccessListener(profileDoc -> {
                                    String name = (profileDoc.exists() && profileDoc.getString("username") != null)
                                            ? profileDoc.getString("username")
                                            : (user.getEmail() != null ? user.getEmail() : "Anonymous");

                                    int avatarId = 0;
                                    Long avatarLong = profileDoc.getLong("avatarId");
                                    if (avatarLong != null) avatarId = avatarLong.intValue();

                                    Map<String, Object> entry = new HashMap<>();
                                    entry.put("displayName", name);
                                    entry.put("bestReactionMs", score);
                                    entry.put("avatarId", avatarId);
                                    db.collection(collection).document(uid).set(entry);
                                });
                    }
                });
    }

    public interface LeaderboardCallback {
        void onResult(List<LeaderboardEntry> entries);
        void onError(String message);
    }

    private Query.Direction sortDirection() {
        return higherIsBetter ? Query.Direction.DESCENDING : Query.Direction.ASCENDING;
    }

    /** Read the top scores, best result first. */
    public void loadLeaderboard(LeaderboardCallback callback) {
        db.collection(collection)
                .orderBy("bestReactionMs", sortDirection())
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
        db.collection(collection)
                .orderBy("bestReactionMs", sortDirection())
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

    /** Fetch my own best score from the leaderboard. */
    public void getMyBestScore(ScoreCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { callback.onError("Not signed in"); return; }

        db.collection(collection).document(user.getUid()).get()
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