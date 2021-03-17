package com.github.giommok.softwaredevproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import static androidx.core.app.ActivityCompat.startActivityForResult;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {
    private static FirebaseAccount account = null;
    private Activity signInActivity;


    private FirebaseAccount(Activity currentActivity) {
        signInActivity = currentActivity;
    }

    public static FirebaseAccount getAccount(Activity currentActivity) {
        if (account == null)
            account = new FirebaseAccount(currentActivity);

        return account;
    }


    @Override
    public boolean isSignedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        return true;
    }

    @Override
    public String getProviderId() {
        return FirebaseAuth.getInstance().getCurrentUser().getProviderId();
    }

    @Override
    public String getDisplayName() {
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    @Override
    public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    @Override
    public String getId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public Uri getPhotoUrl() {
        return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
    }

}
