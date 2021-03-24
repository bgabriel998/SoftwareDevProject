package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
        setUI();
    }

    public void signInButton(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void signOutButton(View view) {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(Task<Void> task) {
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
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Firebase AUTH", "signInWithCredential:success");
                            if(task.getResult().getAdditionalUserInfo().isNewUser()) {
                                chooseUsername();
                                isUsernameUsed(account.getEmail().substring(0, account.getEmail().indexOf('@')).replaceAll(".", ""));
                            }
                            setUI();
                        } else {
                            Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                            setUI();
                        }

                    }
                });
    }

    /**
     * This method is called when the user correctly ended the registration phase
     * @param username
     */
    private void registerUser(String username) {
        Database.setChild("users/" + account.getId(), Arrays.asList("email", "username"), Arrays.asList(account.getEmail(), username));
    }

    /**
     * This method is called when the user submits his username, in order to check if it's still free
     * @param username
     */
    private void isUsernameUsed(String username) {
        Database.isPresent("users", "username", username, () -> usernameAlreadyPresent() , () -> registerUser(username));
    }

    /**
     * This method is called when the desired username is already present
     */
    private void usernameAlreadyPresent() {
        // Notify the user that the chosen username is already used

        // Debug message
        Log.d("Database isPresent", "Username exists");

        // Let the user choose a new username
        chooseUsername();
    }

    /**
     * This method is called after the first Firebase authentication in order to let the user choose his username
     */
    private void chooseUsername() {
        // Let the user choose a username
    }

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    private void setUI(){
        findViewById(R.id.signInButton).setVisibility(account.isSignedIn() ? View.GONE : View.VISIBLE);
        findViewById(R.id.signOutButton).setVisibility(account.isSignedIn() ? View.VISIBLE : View.GONE);
    }
}