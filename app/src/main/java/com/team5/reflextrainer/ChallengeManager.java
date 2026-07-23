package com.team5.reflextrainer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChallengeManager {

    private static final String COLLECTION = "challenges";
    private static final String PROFILES = "profiles";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ActionCallback {
        void onDone();
        void onError(String message);
    }

    public interface ListCallback {
        void onResult(List<Challenge> challenges);
        void onError(String message);
    }

    /** Send a challenge to a friend using my score at the given settings. */
    public void sendChallenge(String toUid, String toUsername, String difficulty,
                              int timeoutMs, int rounds, int myScore,
                              ActionCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        db.collection(PROFILES).document(me.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String myName = (doc.exists() && doc.getString("username") != null)
                            ? doc.getString("username") : me.getEmail();

                    String id = db.collection(COLLECTION).document().getId();
                    Challenge c = new Challenge(id, me.getUid(), myName,
                            toUid, toUsername, difficulty, timeoutMs, rounds,
                            myScore, System.currentTimeMillis());

                    db.collection(COLLECTION).document(id).set(c)
                            .addOnSuccessListener(x -> callback.onDone())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Challenges sent TO me that I haven't played yet. */
    public void loadIncoming(ListCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        db.collection(COLLECTION)
                .whereEqualTo("toUid", me.getUid())
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(q -> {
                    List<Challenge> list = new ArrayList<>();
                    q.forEach(d -> list.add(d.toObject(Challenge.class)));
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Everything involving me that's finished — my record. */
    public void loadCompleted(ListCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        List<Challenge> all = new ArrayList<>();

        db.collection(COLLECTION)
                .whereEqualTo("fromUid", me.getUid())
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(sent -> {
                    sent.forEach(d -> all.add(d.toObject(Challenge.class)));

                    db.collection(COLLECTION)
                            .whereEqualTo("toUid", me.getUid())
                            .whereEqualTo("status", "completed")
                            .get()
                            .addOnSuccessListener(recv -> {
                                recv.forEach(d -> all.add(d.toObject(Challenge.class)));
                                callback.onResult(all);
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Record my result and decide the winner. */
    public void completeChallenge(String challengeId, int myScore, ActionCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        db.collection(COLLECTION).document(challengeId).get()
                .addOnSuccessListener(doc -> {
                    Challenge c = doc.toObject(Challenge.class);
                    if (c == null) { callback.onError("Challenge not found"); return; }

                    int fromScore = c.getFromScore();
                    String winner;
                    if (myScore <= 0)               winner = c.getFromUid();   // no valid result
                    else if (myScore < fromScore)   winner = me.getUid();      // lower ms wins
                    else if (myScore > fromScore)   winner = c.getFromUid();
                    else                            winner = "";               // tie

                    Map<String, Object> update = new HashMap<>();
                    update.put("toScore", myScore);
                    update.put("status", "completed");
                    update.put("winnerUid", winner);

                    db.collection(COLLECTION).document(challengeId).update(update)
                            .addOnSuccessListener(x -> callback.onDone())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface ResultCallback {
        void onResult(boolean won, boolean tie, int myScore, int theirScore, String opponent);
        void onError(String message);
    }

    /** Record my result, decide the winner, and report the outcome back. */
    public void completeChallengeWithResult(String challengeId, int myScore, ResultCallback callback) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { callback.onError("Not signed in"); return; }

        db.collection(COLLECTION).document(challengeId).get()
                .addOnSuccessListener(doc -> {
                    Challenge c = doc.toObject(Challenge.class);
                    if (c == null) { callback.onError("Challenge not found"); return; }

                    int fromScore = c.getFromScore();
                    String winner;
                    boolean won = false, tie = false;
                    if (myScore <= 0)               { winner = c.getFromUid(); }
                    else if (myScore < fromScore)   { winner = me.getUid(); won = true; }
                    else if (myScore > fromScore)   { winner = c.getFromUid(); }
                    else                            { winner = ""; tie = true; }

                    Map<String, Object> update = new HashMap<>();
                    update.put("toScore", myScore);
                    update.put("status", "completed");
                    update.put("winnerUid", winner);

                    final boolean fWon = won, fTie = tie;
                    db.collection(COLLECTION).document(challengeId).update(update)
                            .addOnSuccessListener(x -> callback.onResult(
                                    fWon, fTie, myScore, fromScore, c.getFromUsername()))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}