package com.github.bgabriel998.softwaredevproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;

public class ProfileActivity extends AppCompatActivity {

    private static final String  TOOLBAR_TITLE = "Profile";

    // VIEW REFERENCES
    private View submitUsernameButton;
    private View editTextUsername;
    private View signInButton;
    private View signOutButton;
    private View loggedLayout;
    private View loadingView;

    private GoogleSignInClient mGoogleSignInClient;
    private Account account = Account.getAccount();

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("Google Sign API", "signInButton: started callback");
                // The Task returned from this call is always completed, no need to attach a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    firebaseAuthWithGoogle(task.getResult(ApiException.class).getIdToken());
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w("Google Sign API", "signInResult:failed code=" + e.getStatusCode());
                }
            });
    
    private final ActivityResultLauncher<Intent> addFriendLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Retrieve the snack bar message
                    Intent data = result.getData();
                    String message = data.getStringExtra(AddFriendActivity.INTENT_EXTRA_NAME);

                    Log.d("Friend added", "onActivityResult: message: " + message);

                    // Show the snack bar
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup view variables
        submitUsernameButton = findViewById(R.id.submitUsernameButton);
        editTextUsername = findViewById(R.id.editTextUsername);
        signInButton = findViewById(R.id.signInButton);
        signOutButton = findViewById(R.id.signOutButton);
        loggedLayout = findViewById(R.id.loggedLayout);
        loadingView = findViewById(R.id.loadingView);

        // Setup the toolbar
        ToolbarHandler.SetupToolbar(this, TOOLBAR_TITLE);

        // Sign in options (argument for getClient method)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Gets user and calls for setUI
     */
    @Override
    public void onStart() {
        super.onStart();
        loadingView.setVisibility(View.GONE);
        // If the user is not logged
        if(!account.isSignedIn()) setMenuUI();
        else {
            checkUserOnDB();
        }
    }

    /**
     * On sign-in button click
     * @param view
     */
    public void signInButton(View view) {
        Log.d("Google Sign API", "signInButton: pressed");

        // Create a new intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        // Start the intent with the callback
        signInLauncher.launch(signInIntent);

        Log.d("Google Sign API", "signInButton: finished");
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
        String username = ((EditText)editTextUsername).getText().toString();
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
            setMenuUI();
            ((EditText)editTextUsername).getText().clear();
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
                        setMenuUI();
                    }
                });
    }

    /**
     * On add friend button click
     * @param view
     */
    public void addFriendButton(View view) {
        // Create a new intent
        Intent friendButtonIntent = new Intent(this, AddFriendActivity.class);

        // Start the intent with the callback
        addFriendLauncher.launch(friendButtonIntent);
    }

    /**
     * On friends button click
     * @param view
     */
    public void friendsButton(View view) {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    /**
     * This method is called during the sign-in to perform the connection with Firebase
     * @param idToken
     */
    public void firebaseAuthWithGoogle(String idToken) {
        signInButton.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    loadingView.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Firebase AUTH", "signInWithCredential:success");
                        account.synchronizeUserProfile();
                        // Check if the user is already registered on the database
                        checkUserOnDB();
                    } else {
                        Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                    }

                });
    }

    /**
     * Check if the user is on the DB. If so, set the default UI. Otherwise, force a username change.
     */
    public void checkUserOnDB() {
        Database.refRoot.child(Database.CHILD_USERS + account.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setMenuUI();
                }
                else {
                    setUsernameChoiceUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /* UI METHODS */

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    public void setMenuUI() {
        loggedLayout.setVisibility(account.isSignedIn() ? View.VISIBLE : View.GONE);
        signOutButton.setVisibility(account.isSignedIn() ? View.VISIBLE : View.GONE);
        signInButton.setVisibility(account.isSignedIn() ? View.GONE : View.VISIBLE);
        submitUsernameButton.setVisibility(View.GONE);
        editTextUsername.setVisibility(View.GONE);
    }

    /**
     * Set what is visible on UI after a username change is requested or required
     */
    public void setUsernameChoiceUI() {
        loggedLayout.setVisibility(View.GONE);
        signOutButton.setVisibility(View.GONE);
        signInButton.setVisibility(View.GONE);
        submitUsernameButton.setVisibility(View.VISIBLE);
        editTextUsername.setVisibility(View.VISIBLE);
    }
}