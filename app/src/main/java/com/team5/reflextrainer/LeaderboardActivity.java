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
    private final LeaderboardManager lm = new LeaderboardManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rv = findViewById(R.id.rvLeaderboard);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleScope);
        toggle.check(R.id.scopeGlobal);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.scopeFriends) loadFriends();
            else loadGlobal();
        });

        loadGlobal();
    }

    private void loadGlobal() {
        lm.loadLeaderboard(new LeaderboardManager.LeaderboardCallback() {
            @Override public void onResult(List<LeaderboardEntry> entries) { show(entries); }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void loadFriends() {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null) { toast("Not signed in"); return; }

        new FriendManager().loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                Set<String> uids = new HashSet<>();
                uids.add(me.getUid());                       // include myself
                for (UserProfile f : friends) uids.add(f.getUid());

                lm.loadFriendsLeaderboard(uids, new LeaderboardManager.LeaderboardCallback() {
                    @Override public void onResult(List<LeaderboardEntry> entries) { show(entries); }
                    @Override public void onError(String m) { toast(m); }
                });
            }
            @Override public void onError(String m) { toast(m); }
        });
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