package com.github.giommok.softwaredevproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static androidx.core.app.ActivityCompat.startActivityForResult;


/* Singleton class containing the only possible account connected */
public class FirebaseAccount implements Account {
    private static String username = "null";
    private static FirebaseAccount account = null;


    public static FirebaseAccount getAccount() {
        if (account == null) {
            account = new FirebaseAccount();
        }
        return account;
    }


    @Override
    public boolean isSignedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
        return true;
    }

    @Override
    public String getProviderId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getProviderId();
        return "null";
    }

    @Override
    public String getDisplayName() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        return "null";
    }

    @Override
    public String getEmail() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        return "null";
    }

    @Override
    public String getId() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return "null";
    }

    @Override
    public Uri getPhotoUrl() {
        if(isSignedIn()) return FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        return Uri.EMPTY;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void synchronizeUsername() {
        DatabaseReference dbRef = Database.refRoot.child("users/" + getId() + "/username");
        ValueEventListener usernameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
                Log.d("SYNCHRONIZE_USERNAME", "onDataChange");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                username = "null";
                Log.d("SYNCHRONIZE_USERNAME", "onCancelled");
            }
        };
        dbRef.addValueEventListener(usernameListener);
    }

}
