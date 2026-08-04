package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team5.reflextrainer.data.TrainingSessionRepository;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView ivAvatar;
    private View groupProfileView, groupProfileEdit;
    private EditText etUsernameEdit;
    private MaterialCardView[] avatarCards;

    private String currentUsername = "";
    private int currentAvatarId = 0;
    private int selectedAvatarId = 0;

    private final boolean[] earned = new boolean[Achievements.COUNT];
    private TrainingSessionRepository sessionRepository;
    private String currentUserId;

    private boolean profileLoaded, sessionsLoaded, challengesLoaded, friendsLoaded, avatarFixApplied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        groupProfileView = findViewById(R.id.groupProfileView);
        groupProfileEdit = findViewById(R.id.groupProfileEdit);
        etUsernameEdit = findViewById(R.id.etUsernameEdit);

        avatarCards = new MaterialCardView[] {
                findViewById(R.id.avatarCard0), findViewById(R.id.avatarCard1),
                findViewById(R.id.avatarCard2), findViewById(R.id.avatarCard3),
                findViewById(R.id.avatarCard4), findViewById(R.id.avatarCard5),
                findViewById(R.id.avatarCard6), findViewById(R.id.avatarCard7),
                findViewById(R.id.avatarCard8), findViewById(R.id.avatarCard9),
                findViewById(R.id.avatarCard10), findViewById(R.id.avatarCard11),
        };
        for (int i = 0; i < avatarCards.length; i++) {
            int avatarId = i;
            avatarCards[i].setOnClickListener(v -> selectAvatar(avatarId));
        }

        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> enterEditMode());

        findViewById(R.id.btnCancelEdit).setOnClickListener(v -> exitEditMode());
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> saveProfile());

        SwitchMaterial switchAiCoach = findViewById(R.id.switchAiCoach);
        switchAiCoach.setChecked(AiCoachSettings.isEnabled(this));
        switchAiCoach.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AiCoachSettings.setEnabled(this, isChecked);
            Toast.makeText(this,
                    isChecked ? "AI Coach enabled" : "AI Coach disabled",
                    Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddFriend).setOnClickListener(v ->
                startActivity(new Intent(this, AddFriendActivity.class)));

        findViewById(R.id.btnFriends).setOnClickListener(v ->
                startActivity(new Intent(this, FriendsActivity.class)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        currentUserId = user.getUid();
        sessionRepository = new TrainingSessionRepository(this);
        loadAchievements();

        new ProfileManager().loadProfile(user.getUid(), new ProfileManager.ProfileCallback() {
            @Override
            public void onResult(UserProfile profile) {
                currentUsername = profile.getUsername();
                currentAvatarId = profile.getAvatarId();
                tvUsername.setText(currentUsername);
                tvEmail.setText(profile.getEmail());
                ivAvatar.setImageResource(Avatars.resFor(currentAvatarId));
                profileLoaded = true;
                checkEquippedAvatar();
            }
            @Override
            public void onError(String message) {
                tvUsername.setText("Unknown");
                tvEmail.setText(user.getEmail());   // fall back to auth email
                Toast.makeText(ProfileActivity.this,
                        "Could not load profile", Toast.LENGTH_SHORT).show();
                profileLoaded = true;
                checkEquippedAvatar();
            }
        });
    }

    private void enterEditMode() {
        selectedAvatarId = currentAvatarId;
        etUsernameEdit.setText(currentUsername);
        refreshAvatarSelection();
        groupProfileView.setVisibility(View.GONE);
        groupProfileEdit.setVisibility(View.VISIBLE);
    }

    private void exitEditMode() {
        groupProfileEdit.setVisibility(View.GONE);
        groupProfileView.setVisibility(View.VISIBLE);
    }

    private void selectAvatar(int avatarId) {
        if (!Achievements.isAvatarUnlocked(avatarId, earned) && avatarId != currentAvatarId) {
            int requiredBadge = Achievements.AVATAR_REQUIRES_BADGE[avatarId];
            Toast.makeText(this,
                    "Locked — " + Achievements.DESCRIPTIONS[requiredBadge] + " to unlock",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        selectedAvatarId = avatarId;
        refreshAvatarSelection();
    }

    private void refreshAvatarSelection() {
        int ringWidth = (int) (3 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < avatarCards.length; i++) {
            avatarCards[i].setStrokeWidth(i == selectedAvatarId ? ringWidth : 0);
            boolean unlocked = Achievements.isAvatarUnlocked(i, earned) || i == currentAvatarId;
            avatarCards[i].setAlpha(unlocked ? 1f : 0.35f);
        }
    }

    // ===================== achievements (gate the avatar picker) =====================

    private void loadAchievements() {
        if (currentUserId == null) return;
        sessionRepository.getTrainingHistoryForUser(currentUserId, sessions -> {
            Achievements.Streak streak = Achievements.computeStreak(sessions);
            Achievements.computeSessionBadges(sessions, streak.days, earned);
            refreshAvatarSelection();
            sessionsLoaded = true;
            checkEquippedAvatar();
        });
        new ChallengeManager().loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> challenges) {
                int wins = 0;
                for (Challenge c : challenges) {
                    if (currentUserId.equals(c.getWinnerUid())) wins++;
                }
                earned[7] = wins >= 1;
                earned[10] = wins >= 5;
                refreshAvatarSelection();
                challengesLoaded = true;
                checkEquippedAvatar();
            }
            @Override
            public void onError(String message) {
                challengesLoaded = true;   // leave the Challenger/Rival Slayer badges as-is, don't block the check forever
                checkEquippedAvatar();
            }
        });
        new FriendManager().loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                earned[11] = !friends.isEmpty();
                refreshAvatarSelection();
                friendsLoaded = true;
                checkEquippedAvatar();
            }
            @Override
            public void onError(String message) {
                friendsLoaded = true;   // leave the Social Butterfly badge as-is, don't block the check forever
                checkEquippedAvatar();
            }
        });
    }

    /**
     * Once profile + achievement data have both loaded, make sure the avatar the account
     * currently has equipped is actually one they've earned (older accounts, or ones from
     * before this gating existed, could have a locked avatar equipped) — if not, fall back
     * to the default and save that correction.
     */
    private void checkEquippedAvatar() {
        if (avatarFixApplied || !profileLoaded || !sessionsLoaded || !challengesLoaded || !friendsLoaded) return;
        avatarFixApplied = true;
        if (currentAvatarId == 0 || Achievements.isAvatarUnlocked(currentAvatarId, earned)) return;

        currentAvatarId = 0;
        ivAvatar.setImageResource(Avatars.resFor(currentAvatarId));
        refreshAvatarSelection();
        Toast.makeText(this, "Your avatar wasn't unlocked yet, so it's been reset to the default",
                Toast.LENGTH_LONG).show();

        if (!TextUtils.isEmpty(currentUsername)) {
            new ProfileManager().updateUsernameAndAvatar(currentUserId, currentUsername, currentAvatarId,
                    new ProfileManager.ActionCallback() {
                        @Override public void onDone() { }
                        @Override public void onError(String message) { }
                    });
        }
    }

    private void saveProfile() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String newUsername = etUsernameEdit.getText().toString().trim();
        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "Username can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new ProfileManager().updateUsernameAndAvatar(uid, newUsername, selectedAvatarId,
                new ProfileManager.ActionCallback() {
                    @Override
                    public void onDone() {
                        currentUsername = newUsername;
                        currentAvatarId = selectedAvatarId;
                        tvUsername.setText(currentUsername);
                        ivAvatar.setImageResource(Avatars.resFor(currentAvatarId));
                        Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        exitEditMode();
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProfileActivity.this, "Could not save: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
