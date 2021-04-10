package com.github.bgabriel998.softwaredevproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.Database;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Profile";

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup the toolbar
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        // Sign in options (argument for getClient method)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // TODO MOVE TO MAIN MENU ACTIVITY
        FirebaseApp.initializeApp(this);
    }

    /**
     * Gets user and calls for setUI
     */
    @Override
    public void onStart() {
        super.onStart();
        account = FirebaseAccount.getAccount();
        // If the user is not logged
        if(!account.isSignedIn()) setUI();
        else {
            Database.isPresent(Database.CHILD_USERS, Database.CHILD_EMAIL, account.getEmail(), this::setUI, this::setUsernameChoiceUI);
        }
    }

    /**
     * On sign-in button click
     * @param view
     */
    public void signInButton(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * On change username button click
     * @param view
     */
    public void changeUsernameButton(View view) {
        setUsernameChoiceUI();
    }

    /**
     * On submit of change username UI button click
     * @param view
     */
    public void submitUsernameButton(View view) {
        String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        String currentUsername = account.getUsername();
        Log.d("CURRENT_USERNAME", "onSubmit: " + currentUsername);
        // First, check if the username is valid or if it is already used by the user
        if(!Account.isValid(username) || username.equals(currentUsername)) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), !Account.isValid(username) ? R.string.invalid_username : R.string.current_username, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        // Finally, check if it is available
        else Database.isPresent(Database.CHILD_USERS, Database.CHILD_USERNAME, username, () -> {
            // Notify the user that the chosen username is already used
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.already_existing_username , Snackbar.LENGTH_LONG);
            snackbar.show();
        }, () -> {
            // Notify the user that the username has changed
            Database.setChild(Database.CHILD_USERS + account.getId(), Arrays.asList(Database.CHILD_EMAIL, Database.CHILD_USERNAME), Arrays.asList(account.getEmail(), username));
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.available_username , Snackbar.LENGTH_LONG);
            snackbar.show();
            setUI();
        });
    }

    /**
     * On sign-out button click
     * @param view
     */
    public void signOutButton(View view) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(Task<Void> task) {
                        account.synchronizeUserProfile();
                        setUI();
                    }
                });
    }

    /**
     * On add friend button click
     * @param view
     */
    public void addFriendButton(View view) {
        resetUI(true, false, true, true);
        findViewById(R.id.submitFriendButton).setVisibility(View.VISIBLE);
        findViewById(R.id.editTextFriend).setVisibility(View.VISIBLE);
    }

    /**
     * On submit of add friend UI button click
     * @param view
     */
    public void submitFriendButton(View view) {
        String username = ((EditText)findViewById(R.id.editTextFriend)).getText().toString();
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
            return;
        }
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
                        Database.setChild(Database.CHILD_USERS + account.getId() + Database.CHILD_FRIENDS, Collections.singletonList(friendUid), Collections.singletonList(""));
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.friend_added, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
                else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.friend_not_present_db, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * On friends button click
     * @param view
     */
    public void friendsButton(View view) {
        // TODO start friends activity when implemented
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                firebaseAuthWithGoogle(task.getResult(ApiException.class).getIdToken());
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w("Google Sign API", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    /**
     * This method is called during the sign-in to perform the connection with Firebase
     * @param idToken
     */
    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Firebase AUTH", "signInWithCredential:success");
                            account.synchronizeUserProfile();
                            // Check if the user is already registered on the database
                            Database.isPresent(Database.CHILD_USERS, Database.CHILD_EMAIL, account.getEmail(), () -> setUI(), () -> setUsernameChoiceUI());
                        } else {
                            Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    /* UI METHODS */

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    public void setUI() {
        if (account.isSignedIn()) setLoggedUI();
        else {
            resetUI(true, true, true, false);
            findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets what is visible on UI if the user is logged
     */
    public void setLoggedUI(){
        resetUI(true, true, false, true);
        findViewById(R.id.signOutButton).setVisibility(View.VISIBLE);
        findViewById(R.id.changeUsernameButton).setVisibility(View.VISIBLE);
        findViewById(R.id.addFriendButton).setVisibility(View.VISIBLE);
        findViewById(R.id.friendsButton).setVisibility(View.VISIBLE);
    }

    /**
     * Sets what is visible on UI after a username change is requested or required
     */
    public void setUsernameChoiceUI() {
        findViewById(R.id.submitUsernameButton).setVisibility(View.VISIBLE);
        findViewById(R.id.editTextUsername).setVisibility(View.VISIBLE);
        resetUI(false, true, true, true );
    }

    /**
     * Hide all the buttons
     */
    public void resetUI(boolean hideChangeUsername, boolean hideAddFriend, boolean hideMenu, boolean hideSignIn ) {
        if(hideAddFriend) {
            findViewById(R.id.submitFriendButton).setVisibility(View.GONE);
            findViewById(R.id.editTextFriend).setVisibility(View.GONE);
        }
        if(hideChangeUsername) {
            findViewById(R.id.submitUsernameButton).setVisibility(View.GONE);
            findViewById(R.id.editTextUsername).setVisibility(View.GONE);
        }
        if(hideSignIn) findViewById(R.id.signInButton).setVisibility(View.GONE);
        if(hideMenu) {
            findViewById(R.id.signOutButton).setVisibility(View.GONE);
            findViewById(R.id.changeUsernameButton).setVisibility(View.GONE);
            findViewById(R.id.addFriendButton).setVisibility(View.GONE);
            findViewById(R.id.friendsButton).setVisibility(View.GONE);
        }
    }
}