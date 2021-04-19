package com.github.bgabriel998.softwaredevproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.Database;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Friends";
    private SwipeRefreshLayout refreshLayout;
    private FriendsListAdapter listAdapter;
    private List<FriendItem> friendsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        // Check if the activity was started after a remove friend action. In this case, show the proper message
        Intent intent = getIntent();
        String message = intent.getStringExtra(FriendItemActivity.INTENT_EXTRA_NAME);
        if(message != null) {
            // Notify the user that a friend has been removed
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
            snackbar.show();
        }


        // Fill the friends list
        fillFriendsList();

        // Set up refresh on scroll
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            List<FriendItem> currentFriends = friendsList;
            updateFriends();
            // Check if friends have been updated. If so, refresh the page
            if(friendsList.size() != currentFriends.size() || currentFriends.stream().anyMatch(x -> friendsList.stream().noneMatch(x::equals))) {
                Log.d("FriendsRefresh", "onRefresh: update");
                fillFriendsList();
            }
            refreshLayout.setRefreshing(false);
        });
    }

    /**
     * Fetch ListView and setup it up with a friend item list adapter.
     */
    private void fillFriendsList(){
        ListView friendsListView = findViewById(R.id.friends_list_view);

        // Remove current friends listeners
        friendsList.forEach(FriendItem::removeListener);

        // Update friends
        updateFriends();

        // Add a listener for each friend
        addFriendsListeners();

        listAdapter = new FriendsListAdapter(this,
                R.layout.friend_item,
                friendsList);

        friendsListView.setAdapter(listAdapter);

        // Add listener for clicking on item
        friendsListView.setOnItemClickListener((parent, view, position, id) ->
                switchToFriendItemActivity((FriendItem)friendsListView.getItemAtPosition(position)));
    }

    /**
     * Set up the friends list
     */
    private void updateFriends() {
        Account account = Account.getAccount();
        friendsList = account.getFriends();
    }

    /**
     * Set up the friends listeners and add a listener for each friend
     */
    private void addFriendsListeners() {
        friendsList.forEach(x -> x.setListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer newScore = snapshot.child("score").getValue(Integer.class);
                x.setUsername(snapshot.child(Database.CHILD_USERNAME).getValue(String.class));
                x.setPoints(newScore == null ? 0 : newScore);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }));
    }

    /**
     * Changes to friend item activity and providing intent with information
     * from the item that was clicked.
     * @param item the given item.
     */
    public void switchToFriendItemActivity(FriendItem item) {
        Intent intent = new Intent(this, FriendItemActivity.class);
        fillIntent(intent, item);
        startActivity(intent);
        finish();
    }

    /**
     * Fills intent with information from item
     * @param intent to fill
     * @param item the given item
     */
    private void fillIntent(Intent intent, FriendItem item) {
        intent.putExtra("username", item.getUsername());
        intent.putExtra("points", item.getPoints());
        intent.putExtra("uid", item.getUid());
    }
}