package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvEmpty;

    private final LeaderboardManager reactionLm = new LeaderboardManager();
    private final RhythmLeaderboardManager rhythmLm = new RhythmLeaderboardManager();

    private boolean rhythmMode = false;   // false = Reaction, true = Rhythm
    private boolean friendsScope = false; // false = Global, true = Friends

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rv = findViewById(R.id.rvLeaderboard);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        MaterialButtonToggleGroup toggleMode = findViewById(R.id.toggleMode);
        toggleMode.check(R.id.modeReaction);
        toggleMode.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            rhythmMode = (id == R.id.modeRhythm);
            load();
        });

        MaterialButtonToggleGroup toggleScope = findViewById(R.id.toggleScope);
        toggleScope.check(R.id.scopeGlobal);
        toggleScope.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            friendsScope = (id == R.id.scopeFriends);
            load();
        });

        load();
    }

    private void load() {
        if (friendsScope) {
            loadFriends();
        } else {
            loadGlobal();
        }
    }

    private void loadGlobal() {
        if (rhythmMode) {
            rhythmLm.loadLeaderboard(cb());
        } else {
            reactionLm.loadLeaderboard(cb());
        }
    }

    private void loadFriends() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { toast("Not signed in"); return; }

        new FriendManager().loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                Set<String> uids = new HashSet<>();
                uids.add(me.getUid());
                for (UserProfile f : friends) uids.add(f.getUid());

                if (rhythmMode) {
                    rhythmLm.loadFriendsLeaderboard(uids, cb());
                } else {
                    reactionLm.loadFriendsLeaderboard(uids, cb());
                }
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    // one callback type works for both managers since they share LeaderboardCallback shape
    private LeaderboardManager.LeaderboardCallback cb() {
        return new LeaderboardManager.LeaderboardCallback() {
            @Override public void onResult(List<LeaderboardEntry> entries) { show(entries); }
            @Override public void onError(String m) { toast(m); }
        };
    }

    private void show(List<LeaderboardEntry> entries) {
        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setAdapter(new LeaderboardAdapter(entries));
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}