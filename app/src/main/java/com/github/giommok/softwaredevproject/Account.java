package com.github.giommok.softwaredevproject;

import android.app.Activity;
import android.net.Uri;

public interface Account {

    /**
     * @return the reference to the user account
     */
    public static Account getAccount() {
        // as FirebaseAccount is the only account, getAccount will return FirebaseAccount.getAccount
        return FirebaseAccount.getAccount();
    }

    /**
     * @return true if the account is signed in and false otherwise
     */
    public boolean isSignedIn();

    /**
     * @return the provider of the Firebase account
     */
    public String getProviderId();

    /**
     * @return the main display name of the user or null if no account is signed in
     */
    public String getDisplayName();

    /**
     * @return the e-mail of the user or null if no account is signed in
     */
    public String getEmail();

    /**
     * @return the ID of the user or null if no account is signed in
     */
    public String getId();

    /**
     * @return the URL of the user profile image
     */
    public Uri getPhotoUrl();

    /**
     * @return the username of the current account or null if no account is signed in
     */
    public String getUsername();

    /**
     * This method updates the username of the current account
     */
    public void synchronizeUsername();
}
