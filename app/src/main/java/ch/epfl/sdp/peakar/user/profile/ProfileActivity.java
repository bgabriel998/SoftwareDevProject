package ch.epfl.sdp.peakar.user.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.user.services.Account;
import ch.epfl.sdp.peakar.user.services.AuthProvider;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.utils.ToolbarHandler;
import ch.epfl.sdp.peakar.user.friends.AddFriendActivity;
import ch.epfl.sdp.peakar.user.friends.FriendsActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.snackbar.Snackbar;

import static ch.epfl.sdp.peakar.user.services.AuthProvider.*;

public class ProfileActivity extends AppCompatActivity {

    // CONSTANTS
    private static final String  TOOLBAR_TITLE = "Profile";

    // AUTHENTICATION
    private AuthService authService;

    // VIEW REFERENCES
    private View submitUsernameButton;
    private View editTextUsername;
    private View signInButton;
    private View signOutButton;
    private View loggedLayout;
    private View loadingView;

    // Google Sign In Result Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {

                    // Handle the auth UI
                    handleAuth(GOOGLE, GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class).getIdToken());

                } catch (ApiException e) {

                    // The ApiException status code indicates the detailed failure reason.
                    Log.w("Google Sign API", "signInResult:failed code=" + e.getStatusCode());

                }
            });

    // Add friend result launcher
    private final ActivityResultLauncher<Intent> addFriendLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Retrieve the snack bar message
                    Intent data = result.getData();
                    assert data != null;
                    String message = data.getStringExtra(AddFriendActivity.INTENT_EXTRA_NAME);

                    Log.d("Friend added", "onActivityResult: message: " + message);

                    // Show the initial menu
                    setMenuUI();

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

        authService = AuthService.getInstance();
    }

    /**
     * Gets user and calls for setUI
     */
    @Override
    protected void onStart() {
        super.onStart();
        loadingView.setVisibility(View.GONE);
        // If the user is not logged
        if(authService.getAuthAccount() == null) setMenuUI();
        else {
            // If there is a user already logged, check if the user is already registered
            if(authService.getAuthAccount().getUsername().equals(Account.USERNAME_BEFORE_REGISTRATION)) setUsernameChoiceUI();
            else setMenuUI();
        }
    }

    /**
     * On sign-in button click
     * @param view
     */
    public void signInButton(View view) {
        // Set the options for Google Sign In intent
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        // Create a new Google Sign In intent
        Intent googleSignInIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();

        // Start the intent with the callback
        googleSignInLauncher.launch(googleSignInIntent);
    }

    /**
     * Handle the UI before and after an auth task runs
     */
    private void handleAuth(AuthProvider provider, String token) {

        // Update the view with the loading interface
        signInButton.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);

        // Start a new thread that will handle the auth process
        Thread authThread = new Thread(){
            @Override
            public void run() {
                // Start the authentication using the auth service
                RemoteOutcome authResult = authService.authWithProvider(provider, token);

                // Update the view on the UI thread
                runOnUiThread(() -> {
                    // Remove the loading circle
                    loadingView.setVisibility(View.GONE);

                    // Handle the auth result
                    if(authResult == RemoteOutcome.NOT_FOUND) setUsernameChoiceUI();
                    else {
                        setMenuUI();
                        Log.d("ProfileActivity", "handleAuth: registered");
                    }
                });
            }
        };
        authThread.start();
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
        String newUsername = ((EditText)editTextUsername).getText().toString();

        // Start a new thread that will handle the process
        Thread changeThread = new Thread(){
            @Override
            public void run() {
                // Change the username and wait for the task to end
                ProfileOutcome result = authService.getAuthAccount().changeUsername(newUsername);

                // Update the view on the UI thread
                runOnUiThread(() -> {
                    ((EditText)editTextUsername).getText().clear();

                    // If username has changed, get back to the initial UI
                    if(result == ProfileOutcome.USERNAME_CHANGED || result == ProfileOutcome.USERNAME_REGISTERED) setMenuUI();

                    // Display the message
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), result.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
            }
        };
        changeThread.start();
    }

    /**
     * On sign-out button click
     * @param view
     */
    public void signOutButton(View view) {
        authService.signOut(this);
        setMenuUI();
    }

    /**
     * On add friend button click
     * @param view
     */
    public void addFriendButton(View view) {
        Log.d("ProfileActivity", "addFriendButton: current username - " + authService.getAuthAccount().getUsername());
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

    /* UI METHODS */

    /**
     * Sets what is visible on UI based on if the user is signed in or not.
     */
    public void setMenuUI() {
        boolean signedIn = authService.getAuthAccount() != null;

        loggedLayout.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        signOutButton.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        signInButton.setVisibility(signedIn ? View.GONE : View.VISIBLE);
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