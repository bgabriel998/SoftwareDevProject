package com.github.bgabriel998.softwaredevproject;

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

import java.util.Arrays;

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
            Database.isPresent("users", "email", account.getEmail(), this::setUI, this::setUsernameChoiceUI);
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
        // First, check if the username is valid
        if(Account.isValid(username)) {
            // Then, check if it is already used by the user
            if(username.equals(currentUsername)) {
                Log.d("CURRENT_USERNAME", "onSubmit: EQUAL_CASE");
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.current_username, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            // Finally, check if it is available
            else Database.isPresent("users", "username", username, () -> usernameAlreadyPresent(username), () -> registerUser(username));
        }
        // Display that the username is not valid
        else {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.invalid_username , Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /**
     * On sign-out button click
     * @param view
     */
    public void signOutButton(View view) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(Task<Void> task) {
                        account.synchronizeUsername();
                        setUI();
                    }
                });
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
                            account.synchronizeUsername();
                            // Check if the user is already registered on the database
                            Database.isPresent("users", "email", account.getEmail(), () -> setUI(), () -> setUsernameChoiceUI());
                        } else {
                            Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    /**
     * This method is called when the user correctly ended the registration phase
     * @param username
     */
    public void registerUser(String username) {
        // Notify the user that the username has changed
        Database.setChild("users/" + account.getId(), Arrays.asList("email", "username"), Arrays.asList(account.getEmail(), username));
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.available_username , Snackbar.LENGTH_LONG);
        snackbar.show();
        setUI();
    }

    /**
     * This method is called when the desired username is already present
     * @param username
     */
    public void usernameAlreadyPresent(String username) {
        // Notify the user that the chosen username is already used
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.already_existing_username , Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /* UI METHODS */

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    public void setUI() {
        if (account.isSignedIn()) setLoggedUI();
        else setNotLoggedUI();
    }

    /**
     * Sets what is visible on UI if the user is logged
     */
    public void setLoggedUI(){
        setVisibilitySignInUI(true);
        hideChangeUsernameButtons();
    }

    /**
     * Sets what is visible on UI if the user is not logged
     */
    public void setNotLoggedUI(){
        setVisibilitySignInUI(false);
        hideChangeUsernameButtons();
    }

    /**
     * Sets visibility for sign-in related buttons
     * @param logged
     */
    public void setVisibilitySignInUI(Boolean logged) {
        findViewById(R.id.signInButton).setVisibility(logged ? View.GONE : View.VISIBLE);
        findViewById(R.id.signOutButton).setVisibility(logged ? View.VISIBLE : View.GONE);
        findViewById(R.id.changeUsernameButton).setVisibility(logged ? View.VISIBLE : View.GONE);
    }

    /**
     * Hides the username change buttons
     */
    public void hideChangeUsernameButtons() {
        findViewById(R.id.submitUsernameButton).setVisibility(View.GONE);
        findViewById(R.id.editTextUsername).setVisibility(View.GONE);
    }

    /**
     * Sets what is visible on UI after a username change is requested or required
     */
    public void setUsernameChoiceUI() {
        findViewById(R.id.signInButton).setVisibility(View.GONE);
        findViewById(R.id.signOutButton).setVisibility(View.GONE);
        findViewById(R.id.changeUsernameButton).setVisibility(View.GONE);
        findViewById(R.id.submitUsernameButton).setVisibility(View.VISIBLE);
        findViewById(R.id.editTextUsername).setVisibility(View.VISIBLE);
    }
}