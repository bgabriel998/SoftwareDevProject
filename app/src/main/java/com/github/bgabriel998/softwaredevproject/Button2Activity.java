package com.github.bgabriel998.softwaredevproject;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.github.giommok.softwaredevproject.Account;
import com.github.giommok.softwaredevproject.Database;
import com.github.giommok.softwaredevproject.FirebaseAccount;
import com.github.ravifrancesco.softwaredevproject.POIPoint;
import com.github.ravifrancesco.softwaredevproject.UserPoint;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.bonuspack.location.POI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class Button2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private Account account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button2);

        // Views
        mStatusTextView = findViewById(R.id.sign_in_status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Sign in options (argument for getClient method)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);
        FirebaseApp.initializeApp(this);


    }

    @Override
    public void onStart() {
        super.onStart();
        account = FirebaseAccount.getAccount();
        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }


    /**
     * This is the public method to call in order to authenticate the user 
     */
    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                            updateUI();
                        } else {
                            Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                            updateUI();
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
     * This is the method to call to let the user sign out
     */
    public void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(Task<Void> task) {
                        updateUI();
                    }
                });
    }

    /**
     * This method updates the UI after an operation is performed
     */
    private void updateUI() {
        /* If an account is logged */
        if (account.isSignedIn()) {
            mStatusTextView.setText("Your name: " + account.getDisplayName() + "\nYour e-mail: " + account.getEmail() + "\n");

            /* resizing signed_out_layout */
            LinearLayout layout = findViewById(R.id.signed_out_layout);
            layout.getLayoutParams().height = 0;

            findViewById(R.id.signed_in_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_status).setVisibility(View.VISIBLE);

        } else {

            mStatusTextView.setVisibility(View.GONE);

            /* resizing signed_out_layout */
            LinearLayout layout = findViewById(R.id.signed_out_layout);
            layout.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;

            findViewById(R.id.signed_in_layout).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }
}


