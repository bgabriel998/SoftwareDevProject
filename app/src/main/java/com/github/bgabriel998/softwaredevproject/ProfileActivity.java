package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        //account.synchronizeUsername();
        if(!account.isSignedIn()) setUI();
        else {
            account.synchronizeUsername();
            Database.isPresent("users", "email", account.getEmail(), this::setUI, this::setUsernameChoiceUI);
        }
    }

    public void signInButton(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void changeUsernameButton(View view) {
        setUsernameChoiceUI();
    }

    public void submitUsernameButton(View view) {
        String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        String currentUsername = account.getUsername();
        Log.d("CURRENT_USERNAME", "onSubmit: " + currentUsername);
        if(username.equals(currentUsername)) {
            Log.d("CURRENT_USERNAME", "onSubmit: EQUAL_CASE");
            Toast toast = Toast.makeText(getApplicationContext(), "You can't choose the username you already have!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else Database.isPresent("users", "username", username, () -> usernameAlreadyPresent(username) , () -> registerUser(username));
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
    private void registerUser(String username) {
        // Notify the user that the username has changed
        Database.setChild("users/" + account.getId(), Arrays.asList("email", "username"), Arrays.asList(account.getEmail(), username));
        Toast toast = Toast.makeText(getApplicationContext(), "Your username has changed!", Toast.LENGTH_SHORT);
        toast.show();
        account.synchronizeUsername();
        setUI();
    }

    /**
     * This method is called when the desired username is already present
     */
    private void usernameAlreadyPresent(String username) {
        // Notify the user that the chosen username is already used
        Toast toast = Toast.makeText(getApplicationContext(), "The username " + username + " is already used by another user. Choose a new one.", Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    private void setUI(){
        findViewById(R.id.signInButton).setVisibility(account.isSignedIn() ? View.GONE : View.VISIBLE);
        findViewById(R.id.signOutButton).setVisibility(account.isSignedIn() ? View.VISIBLE : View.GONE);
        findViewById(R.id.changeUsernameButton).setVisibility(account.isSignedIn() ? View.VISIBLE : View.GONE);
        findViewById(R.id.submitUsernameButton).setVisibility(View.GONE);
        findViewById(R.id.editTextUsername).setVisibility(View.GONE);
    }

    private void setUsernameChoiceUI() {
        findViewById(R.id.signInButton).setVisibility(View.GONE);
        findViewById(R.id.signOutButton).setVisibility(View.GONE);
        findViewById(R.id.changeUsernameButton).setVisibility(View.GONE);
        findViewById(R.id.submitUsernameButton).setVisibility(View.VISIBLE);
        findViewById(R.id.editTextUsername).setVisibility(View.VISIBLE);
    }
}