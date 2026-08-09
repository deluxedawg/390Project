package com.team5.reflextrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChallengesActivity extends AppCompatActivity {

    private final ChallengeManager cm = new ChallengeManager();
    private final FriendManager fm = new FriendManager();
    private RecyclerView rvIncoming, rvPending, rvCompleted;
    private TextView tvNoIncoming, tvNoPending, tvNoCompleted, tvRecord;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        rvIncoming = findViewById(R.id.rvIncoming);
        rvPending = findViewById(R.id.rvPending);
        rvCompleted = findViewById(R.id.rvCompleted);
        tvNoIncoming = findViewById(R.id.tvNoIncoming);
        tvNoPending = findViewById(R.id.tvNoPending);
        tvNoCompleted = findViewById(R.id.tvNoCompleted);
        tvRecord = findViewById(R.id.tvRecord);

        rvIncoming.setLayoutManager(new LinearLayoutManager(this));
        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewChallenge).setOnClickListener(v -> showFriendPicker());

        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        myUid = (me != null) ? me.getUid() : "";
    }

    /** Pick a friend to challenge, then hand off to difficulty/rounds selection. */
    private void showFriendPicker() {
        fm.loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                if (friends.isEmpty()) {
                    toast("Add a friend first");
                    return;
                }

                BottomSheetDialog dialog = new BottomSheetDialog(ChallengesActivity.this);
                View sheet = LayoutInflater.from(ChallengesActivity.this)
                        .inflate(R.layout.dialog_friend_picker, null);

                ((TextView) sheet.findViewById(R.id.tvPickerEyebrow)).setText("NEW CHALLENGE");
                ((TextView) sheet.findViewById(R.id.tvPickerSubtitle)).setText("Choose a friend to challenge");

                RecyclerView rvPicker = sheet.findViewById(R.id.rvFriendPicker);
                rvPicker.setLayoutManager(new LinearLayoutManager(ChallengesActivity.this));
                rvPicker.setAdapter(new FriendPickerAdapter(friends, friend -> {
                    dialog.dismiss();
                    promptChallenge(friend);
                }));

                dialog.setContentView(sheet);
                dialog.show();
            }
            @Override public void onError(String message) { toast(message); }
        });
    }

    private void promptChallenge(UserProfile friend) {
        Intent i = new Intent(this, LevelSelectActivity.class);
        i.putExtra("challengeToUid", friend.getUid());
        i.putExtra("challengeToUsername", friend.getUsername());
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncoming();
        loadPending();
        loadCompleted();
    }

    private void loadIncoming() {
        cm.loadIncoming(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> list) {
                tvNoIncoming.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvIncoming.setAdapter(new IncomingChallengeAdapter(list, c -> {
                    // launch training with the challenge's settings
                    Intent i = new Intent(ChallengesActivity.this, TrainingActivity.class);
                    i.putExtra(LevelSelectActivity.EXTRA_TIMEOUT, c.getTimeoutMs());
                    i.putExtra(LevelSelectActivity.EXTRA_DIFFICULTY, c.getDifficulty());
                    i.putExtra(LevelSelectActivity.EXTRA_ROUNDS, c.getRounds());
                    i.putExtra("challengeId", c.getChallengeId());   // Stage 3 uses this
                    startActivity(i);
                }));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void loadPending() {
        cm.loadSent(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> list) {
                tvNoPending.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvPending.setAdapter(new SentChallengeAdapter(list));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void loadCompleted() {
        cm.loadCompleted(new ChallengeManager.ListCallback() {
            @Override
            public void onResult(List<Challenge> list) {
                tvNoCompleted.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                rvCompleted.setAdapter(new CompletedChallengeAdapter(list, myUid, ChallengesActivity.this::openCompletedChallenge));

                int wins = 0, losses = 0, ties = 0;
                for (Challenge c : list) {
                    String w = c.getWinnerUid();
                    if (w == null || w.isEmpty()) ties++;
                    else if (myUid.equals(w)) wins++;
                    else losses++;
                }
                tvRecord.setText(wins + "W · " + losses + "L" + (ties > 0 ? " · " + ties + "T" : ""));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void openCompletedChallenge(Challenge c) {
        boolean iAmChallenger = myUid.equals(c.getFromUid());
        String opponent = iAmChallenger ? c.getToUsername() : c.getFromUsername();
        int myScore = iAmChallenger ? c.getFromScore() : c.getToScore();
        int theirScore = iAmChallenger ? c.getToScore() : c.getFromScore();
        boolean tie = c.getWinnerUid() == null || c.getWinnerUid().isEmpty();
        boolean won = !tie && myUid.equals(c.getWinnerUid());

        Intent i = new Intent(this, ChallengeResultActivity.class);
        i.putExtra("viewOnly", true);
        i.putExtra("won", won);
        i.putExtra("tie", tie);
        i.putExtra("myScore", myScore);
        i.putExtra("theirScore", theirScore);
        i.putExtra("opponent", opponent);
        startActivity(i);
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}