package com.team5.reflextrainer;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileManager {

    private static final String COLLECTION = "profiles";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Create or update a user's profile, keyed by uid. */
    public void saveProfile(String uid, String username, String email) {
        UserProfile profile = new UserProfile(uid, username, email);
        db.collection(COLLECTION).document(uid).set(profile);
    }

    public interface ProfileCallback {
        void onResult(UserProfile profile);
        void onError(String message);
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
}