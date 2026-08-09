package com.team5.reflextrainer;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private static final String COLLECTION = "profiles";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Create or update a user's profile, keyed by uid. */
    public void saveProfile(String uid, String username, String email,
                             boolean researchConsent, double heightCm, double weightKg) {
        UserProfile profile = new UserProfile(uid, username, email);
        profile.setResearchConsent(researchConsent);
        if (researchConsent) {
            profile.setHeightCm(heightCm);
            profile.setWeightKg(weightKg);
        }
        db.collection(COLLECTION).document(uid).set(profile);
    }

    public interface ProfileCallback {
        void onResult(UserProfile profile);
        void onError(String message);
    }

    public interface ActionCallback {
        void onDone();
        void onError(String message);
    }

    /** Partial update: only touches username + avatarId, leaves uid/email untouched. */
    public void updateUsernameAndAvatar(String uid, String username, int avatarId, ActionCallback callback) {
        db.collection(COLLECTION).document(uid)
                .update("username", username, "avatarId", avatarId)
                .addOnSuccessListener(v -> callback.onDone())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Store this device's FCM token so a Cloud Function can push notifications to it. */
    public void updateFcmToken(String uid, String token) {
        db.collection(COLLECTION).document(uid).update("fcmToken", token);
    }

    /**
     * Partial update for the BMI-research opt-in. Turning consent off clears the stored
     * height/weight rather than leaving stale values behind under a false flag.
     */
    public void updateResearchData(String uid, boolean researchConsent, double heightCm, double weightKg,
                                    ActionCallback callback) {
        db.collection(COLLECTION).document(uid)
                .update("researchConsent", researchConsent,
                        "heightCm", researchConsent ? heightCm : 0,
                        "weightKg", researchConsent ? weightKg : 0)
                .addOnSuccessListener(v -> callback.onDone())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Load the profile for a given uid. */
    public void loadProfile(String uid, ProfileCallback callback) {
        db.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onResult(snapshot.toObject(UserProfile.class));
                    } else {
                        callback.onError("Profile not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface BadgesCallback {
        void onResult(List<Integer> badgeIndices);
    }

    /**
     * Badge indices already earned and durably recorded for this account (empty if none yet).
     * This is the floor achievement screens seed from so a badge, once earned, survives things
     * like a reinstall wiping the local training-session history it was originally computed from.
     */
    public void loadEarnedBadges(String uid, BadgesCallback callback) {
        db.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    UserProfile profile = snapshot.exists() ? snapshot.toObject(UserProfile.class) : null;
                    List<Integer> badges = (profile != null && profile.getEarnedBadges() != null)
                            ? profile.getEarnedBadges() : new ArrayList<>();
                    callback.onResult(badges);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /** Adds newly-earned badge indices to the persisted set. No-op if there's nothing new. */
    public void addEarnedBadges(String uid, List<Integer> newIndices) {
        if (newIndices.isEmpty()) return;
        db.collection(COLLECTION).document(uid)
                .update("earnedBadges", FieldValue.arrayUnion(newIndices.toArray()));
    }
}