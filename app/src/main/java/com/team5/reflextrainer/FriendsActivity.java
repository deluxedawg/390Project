package com.team5.reflextrainer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private final FriendManager fm = new FriendManager();
    private RecyclerView rvRequests, rvFriends;
    private TextView tvNoRequests, tvNoFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        rvRequests = findViewById(R.id.rvRequests);
        rvFriends = findViewById(R.id.rvFriends);
        tvNoRequests = findViewById(R.id.tvNoRequests);
        tvNoFriends = findViewById(R.id.tvNoFriends);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBackHome).setOnClickListener(v -> finish());

        refresh();
    }

    private void refresh() {
        loadRequests();
        loadFriends();
    }

    private void loadRequests() {
        fm.loadIncomingRequests(new FriendManager.RequestsCallback() {
            @Override
            public void onResult(List<FriendRequest> requests) {
                tvNoRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                rvRequests.setAdapter(new RequestAdapter(requests, new RequestAdapter.Listener() {
                    @Override
                    public void onAccept(FriendRequest req) {
                        fm.acceptRequest(req, new FriendManager.ActionCallback() {
                            @Override public void onDone() {
                                Toast.makeText(FriendsActivity.this,
                                        "Added " + req.getFromUsername(), Toast.LENGTH_SHORT).show();
                                refresh();
                            }
                            @Override public void onError(String m) { toast(m); }
                        });
                    }
                    @Override
                    public void onReject(FriendRequest req) {
                        fm.rejectRequest(req, new FriendManager.ActionCallback() {
                            @Override public void onDone() { refresh(); }
                            @Override public void onError(String m) { toast(m); }
                        });
                    }
                }));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void loadFriends() {
        fm.loadFriends(new FriendManager.FriendsCallback() {
            @Override
            public void onResult(List<UserProfile> friends) {
                tvNoFriends.setVisibility(friends.isEmpty() ? View.VISIBLE : View.GONE);
                rvFriends.setAdapter(new FriendAdapter(friends));
            }
            @Override public void onError(String m) { toast(m); }
        });
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}
