package com.github.bgabriel998.softwaredevproject;




import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.giommok.softwaredevproject.FirebaseAccount;
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

import java.util.concurrent.ExecutionException;


public class Button2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private FirebaseAccount account;

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
        account = FirebaseAccount.getAccount(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        updateUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                googleSignIn();
                break;
            case R.id.sign_out_button:
                googleSignOut();
                updateUI();
                break;
        }
    }

    /* The following three methods are necessary in order to let the user sign in */
    /* This is the public method to call in order to authenticate the user */
    public void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        //if(!account.signedIn()) System.exit(1);

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
                account.setGoogleAccount(task.getResult(ApiException.class));
                firebaseAuthWithGoogle();
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w("Google Sign API", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle() {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getGoogleIdToken(), null);
        account.getFirebaseAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Firebase AUTH", "signInWithCredential:success");
                            account.updateFirebaseUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Firebase AUTH", "signInWithCredential:failure", task.getException());
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });
    }



    /* This is the public method to call in order to let the user sign out */
    public void googleSignOut() {
        mGoogleSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();
        account.updateFirebaseUser();
        account.setGoogleAccount(null);
    }

    /* This method contains the necessary UI updates after a sign in or a sign out */
    private void updateUI() {
        /* If an account is logged */
        if (account.isSignedIn()) {
            mStatusTextView.setText("Your name: " + account.getDisplayName() + "\nYour e-mail: " + account.getEmail() + "\nYour family name: " + account.getFamilyName());

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