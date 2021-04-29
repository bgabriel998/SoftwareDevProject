package com.github.bgabriel998.softwaredevproject.user.friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.github.bgabriel998.softwaredevproject.user.account.Account;
import com.github.bgabriel998.softwaredevproject.database.Database;
import com.github.bgabriel998.softwaredevproject.user.account.FirebaseAccount;
import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.utils.ToolbarHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

public class AddFriendActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Add friends";

    // INTENT CONSTANT
    public static final String INTENT_EXTRA_NAME = "FRIEND_ADDED";

    // VIEW REFERENCES
    private View editTextFriend;

    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Set up view variables
        editTextFriend = findViewById(R.id.editTextFriend);

        // Setup the toolbar
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the account reference
        account = FirebaseAccount.getAccount();
    }

    /**
     * On submit of add friend UI button click
     * @param view
     */
    public void submitFriendButton(View view) {
        String username = ((EditText)editTextFriend).getText().toString();
        String currentUsername = account.getUsername();
        Log.d("FRIENDS SIZE", "onSubmit: " + account.getFriends().size());

        boolean found = false;
        for(FriendItem friend: account.getFriends()) {
            if(friend.hasUsername(username)) found = true;
        }

        // First, check if the username is valid, it's not the current user or it's already in the current user's friends
        if(!Account.isValid(username) || username.equals(currentUsername) || found) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), !Account.isValid(username) ? R.string.invalid_username : ( !found ? R.string.add_current_username : R.string.friend_already_added), Snackbar.LENGTH_LONG);
            snackbar.show();

            // Clean the edit text field
            ((EditText)editTextFriend).getText().clear();

            return;
        }

        // Then, check if the user is registered and if so add the friend
        checkUserExistsAndAdd(username);
    }

    /* Check that the user exists on the DB and, if so, add it to the friends of the current user  */
    private void checkUserExistsAndAdd(String username) {
        Database.refRoot.child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot user: snapshot.getChildren()) {
                        String friendUid = user.getKey();
                        String INTENT_EXTRA_VALUE = username + " " + getResources().getString(R.string.friend_added);
                        Database.setChild(Database.CHILD_USERS + account.getId() + Database.CHILD_FRIENDS, Collections.singletonList(friendUid), Collections.singletonList(""));

                        // Get the intent that started the activity
                        Intent intent = getIntent();

                        // Put extra to the intent so a message can be shown in profile activity
                        intent.putExtra(INTENT_EXTRA_NAME, INTENT_EXTRA_VALUE);
                        setResult(RESULT_OK, intent);

                        // Destroy this activity
                        finish();
                    }
                }
                else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.friend_not_present_db, Snackbar.LENGTH_LONG);
                    snackbar.show();
                    ((EditText)editTextFriend).getText().clear();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}