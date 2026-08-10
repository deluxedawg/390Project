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
    private final LeaderboardManager fatigueLm = new LeaderboardManager("fatigue_leaderboard");
    private final LeaderboardManager simonLm = new LeaderboardManager("simon_leaderboard", true);

    private enum Mode { REACTION, RHYTHM, FATIGUE, SIMON }
    private Mode mode = Mode.REACTION;
    private boolean friendsScope = false; // false = Global, true = Friends

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rv = findViewById(R.id.rvLeaderboard);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        // two rows of two buttons (instead of cramming four into one row) so
        // labels render in full; the rows are kept mutually exclusive in code
        MaterialButtonToggleGroup toggleModeRow1 = findViewById(R.id.toggleModeRow1);
        MaterialButtonToggleGroup toggleModeRow2 = findViewById(R.id.toggleModeRow2);
        toggleModeRow1.check(R.id.modeReaction);
        toggleModeRow1.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            toggleModeRow2.clearChecked();
            mode = (id == R.id.modeRhythm) ? Mode.RHYTHM : Mode.REACTION;
            load();
        });
        toggleModeRow2.addOnButtonCheckedListener((g, id, checked) -> {
            if (!checked) return;
            toggleModeRow1.clearChecked();
            mode = (id == R.id.modeSimon) ? Mode.SIMON : Mode.FATIGUE;
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
        switch (mode) {
            case RHYTHM: rhythmLm.loadLeaderboard(cb()); break;
            case FATIGUE: fatigueLm.loadLeaderboard(cb()); break;
            case SIMON: simonLm.loadLeaderboard(cb()); break;
            default: reactionLm.loadLeaderboard(cb());
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

                switch (mode) {
                    case RHYTHM: rhythmLm.loadFriendsLeaderboard(uids, cb()); break;
                    case FATIGUE: fatigueLm.loadFriendsLeaderboard(uids, cb()); break;
                    case SIMON: simonLm.loadFriendsLeaderboard(uids, cb()); break;
                    default: reactionLm.loadFriendsLeaderboard(uids, cb());
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
            String unit = (mode == Mode.SIMON) ? "rounds" : "ms";
            rv.setAdapter(new LeaderboardAdapter(entries, unit));
        }
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}