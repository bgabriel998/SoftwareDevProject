package ch.epfl.sdp.peakar.user.friends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.user.auth.Authentication;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Friends";

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
    }

    /**
     * Fetch ListView and setup it up with a friend item list adapter.
     */
    private void fillFriendsList(){
        ListView friendsListView = findViewById(R.id.friends_list_view);

        List<FriendItem> friendsList = Authentication.getInstance().getAuthAccount().getFriends();

        // Create the list adapter
        FriendsListAdapter listAdapter = new FriendsListAdapter(this,
                R.layout.friend_item,
                friendsList);

        // Set the adapter
        friendsListView.setAdapter(listAdapter);

        // Add listener for clicking on item
        friendsListView.setOnItemClickListener((parent, view, position, id) ->
                switchToFriendItemActivity((FriendItem)friendsListView.getItemAtPosition(position)));
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