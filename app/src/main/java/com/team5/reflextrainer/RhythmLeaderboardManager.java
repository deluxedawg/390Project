package com.team5.reflextrainer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RhythmLeaderboardManager {

    private static final String COLLECTION = "rhythm_leaderboard";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Submit an average-offset score. Lower is better; keep the user's best (lowest). */
    public void submitScore(int avgOffsetMs) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("profiles").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = (doc.exists() && doc.getString("username") != null)
                            ? doc.getString("username") : user.getEmail();

                    db.collection(COLLECTION).document(user.getUid()).get()
                            .addOnSuccessListener(existing -> {
                                Long prev = existing.exists() ? existing.getLong("bestReactionMs") : null;
                                if (prev == null || avgOffsetMs < prev) {
                                    Map<String, Object> entry = new HashMap<>();
                                    entry.put("displayName", name);
                                    entry.put("bestReactionMs", avgOffsetMs);
                                    db.collection(COLLECTION).document(user.getUid()).set(entry);
                                }
                            });
                });
    }

    /** Global rhythm leaderboard. Uses the SHARED callback type from LeaderboardManager. */
    public void loadLeaderboard(LeaderboardManager.LeaderboardCallback callback) {
        db.collection(COLLECTION)
                .orderBy("bestReactionMs", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<LeaderboardEntry> list = new ArrayList<>();
                    query.forEach(d -> list.add(d.toObject(LeaderboardEntry.class)));
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Friends-only rhythm leaderboard. */
    public void loadFriendsLeaderboard(Set<String> allowedUids,
                                       LeaderboardManager.LeaderboardCallback callback) {
        db.collection(COLLECTION)
                .orderBy("bestReactionMs", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<LeaderboardEntry> list = new ArrayList<>();
                    query.forEach(doc -> {
                        if (allowedUids.contains(doc.getId())) {
                            list.add(doc.toObject(LeaderboardEntry.class));
                        }
                    });
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}