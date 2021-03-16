package com.github.giommok.softwaredevproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.core.app.ActivityCompat.startActivityForResult;

interface Account {

    /* returns true if the account is signed in, false otherwise */
    public boolean isSignedIn();

    public String getProviderId();

    public String getDisplayName();

    public String getEmail();

    public String getFamilyName();

    public String getId();

    public String getGoogleIdToken();

    public Uri getPhotoUrl();

}

/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {
    private static FirebaseAccount account = null;
    private GoogleSignInAccount googleAccount;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private Activity signInActivity;


    private FirebaseAccount(Activity currentActivity) {
        signInActivity = currentActivity;
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        googleAccount = GoogleSignIn.getLastSignedInAccount(signInActivity);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

    }

    public static FirebaseAccount getAccount(Activity currentActivity)
    {
        if (account == null)
            account = new FirebaseAccount(currentActivity);

        return account;
    }

    public void setGoogleAccount(GoogleSignInAccount account) {
        this.googleAccount = account;

    }

    public void updateFirebaseUser() {
        firebaseUser = firebaseAuth.getCurrentUser();
    }

    @Override
    public boolean isSignedIn() {
        if (firebaseUser == null) return false;
        return true;
    }

    @Override
    public String getProviderId() {
        return firebaseUser.getProviderId();
    }

    @Override
    public String getDisplayName() {
        return firebaseUser.getDisplayName();
    }

    @Override
    public String getEmail() {
        return firebaseUser.getEmail();
    }

    @Override
    public String getFamilyName() {
        return googleAccount.getFamilyName();
    }

    @Override
    public String getId() {
        return firebaseUser.getUid();
    }

    @Override
    public String getGoogleIdToken() {
        return googleAccount.getIdToken();
    }

    @Override
    public Uri getPhotoUrl() {
        return googleAccount.getPhotoUrl();
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }
}
