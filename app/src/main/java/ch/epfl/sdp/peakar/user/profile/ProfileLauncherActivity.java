package ch.epfl.sdp.peakar.user.profile;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AuthProvider;
import ch.epfl.sdp.peakar.user.services.AuthService;

import static ch.epfl.sdp.peakar.user.services.AuthProvider.GOOGLE;

/**
 * Used to launch the profile. If no gmail address is selected, then the Google Sign in will be called
 * else, the Profile Activity gets called. To use this, simply call the activity like any other activity.
 * The activity will be drawn over the current activity but will hav a transparent background.
 */
public class ProfileLauncherActivity extends AppCompatActivity {

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide status-bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(!Database.getInstance().isOnline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(ProfileOutcome.FAIL.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> finish());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            authService = AuthService.getInstance();

            if(isUserSignedIn()){
                new Thread(() -> {
                    AuthService.getInstance().getAuthAccount().init();
                    runOnUiThread(this::launchProfileActivity);
                }).start();

            }
            else{
                // Set the options for Google Sign In intent
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        requestIdToken(getResources().getString(R.string.default_web_client_id)).requestEmail().build();

                // Create a new Google Sign In intent
                Intent googleSignInIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();

                // Start the intent with the callback
                googleSignInLauncher.launch(googleSignInIntent);
            }
        }
    }

    // Google Sign In Result Launcher
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(R.layout.progress);
                Dialog loadingDialog = builder.create();
                loadingDialog.show();
                loadingDialog.setCanceledOnTouchOutside(false);
                loadingDialog.setCancelable(false);

                try {
                    // Handle the auth UI
                    handleAuth(GOOGLE, GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class).getIdToken(), loadingDialog);
                } catch (ApiException e) {
                    // The ApiException status code indicates the detailed failure reason.
                    Log.w("Google Sign API", "signInResult:failed code=" + e.getStatusCode());
                    loadingDialog.dismiss();
                    finish();
                }
            });


    /**
     * Handle the UI before and after an auth task runs
     */
    private void handleAuth(AuthProvider provider, String token, Dialog loadingDialog) {

        // Start a new thread that will handle the auth process
        Thread authThread = new Thread(){
            @Override
            public void run() {
                // Start the authentication using the auth service
                RemoteOutcome authResult = authService.authWithProvider(provider, token);

                // Update the view on the UI thread
                runOnUiThread(() -> {
                    // Handle the auth result
                    if(authResult != RemoteOutcome.FAIL){
                        loadingDialog.dismiss();
                        launchProfileActivity();
                        finish();
                    }
                });
            }
        };
        authThread.start();
    }

    /**
     * Checks is the user is already signed in or not
     * @return True if the user is already signed in
     */
    private boolean isUserSignedIn(){
        return authService.getAuthAccount() != null;
    }

    /**
     * Launches the Profile Activity, if the user is online. Otherwise, force a sign out
     */
    private void launchProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.AUTH_INTENT, true);
        startActivity(intent);
        finish();
    }

}
