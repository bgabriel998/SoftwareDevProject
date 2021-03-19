package com.github.giommok.softwaredevproject;

import android.app.Activity;
import android.net.Uri;

public interface Account {


    /**
     * Returns the reference to the user account
     * @return
     */
    public static Account getAccount() {
        // as FirebaseAccount is the only account, getAccount will return FirebaseAccount.getAccount
        return FirebaseAccount.getAccount();
    }

    /**
     * Returns true if the account is signed in and false otherwise
     * @return
     */
    public boolean isSignedIn();

    /**
     * Returns the provider of the Firebase account
     * @return
     */
    public String getProviderId();

    /**
     * Returns the main display name of the user or null if no account is signed in
     * @return
     */
    public String getDisplayName();

    /**
     * Returns the e-mail of the user or null if no account is signed in
     * @return
     */
    public String getEmail();

    /**
     * Returns the ID of the user or null if no account is signed in
     * @return
     */
    public String getId();

    /**
     * Returns the URL of the user profile image
     * @return
     */
    public Uri getPhotoUrl();

}
