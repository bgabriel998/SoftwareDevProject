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

import static androidx.core.app.ActivityCompat.startActivityForResult;

interface Account {

    /* returns true if the account is signed in, false otherwise */
    public boolean isSignedIn();

    public String getDisplayName();

    public String getEmail();

    public String getFamilyName();

    public String getId();

    public Uri getPhotoUrl();

}

/* Singleton class containing the only possible account connected */
public class GoogleAccount implements Account {
    private static GoogleAccount account = null;
    private GoogleSignInAccount googleAccount;
    private Activity signInActivity;


    private GoogleAccount(Activity currentActivity) {
        signInActivity = currentActivity;
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        googleAccount = GoogleSignIn.getLastSignedInAccount(signInActivity);

    }

    public static GoogleAccount getAccount(Activity currentActivity)
    {
        if (account == null)
            account = new GoogleAccount(currentActivity);

        return account;
    }

    public void setGoogleAccount(GoogleSignInAccount account) {
        this.googleAccount = account;
    }

    @Override
    public boolean isSignedIn() {
        if (googleAccount == null) return false;
        return true;
    }

    @Override
    public String getDisplayName() {
        return googleAccount.getDisplayName();
    }

    @Override
    public String getEmail() {
        return googleAccount.getEmail();
    }

    @Override
    public String getFamilyName() {
        return googleAccount.getFamilyName();
    }

    @Override
    public String getId() {
        return googleAccount.getId();
    }

    @Override
    public Uri getPhotoUrl() {
        return googleAccount.getPhotoUrl();
    }
}
