package ch.epfl.sdp.peakar.user.services;

import android.net.Uri;

import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseOtherAccount;

/**
 * This class represents the local account model of an non-authenticated user.
 * For each database provider:
 * 1. extend this class implementing RemoteResource interface to handle the interaction with the specific Database.
 * 2. implement the modifiers methods that need interaction with the specific Database.
 */
public abstract class OtherAccount extends Account {

    /**
     * Get an account instance.
     * @param userID id of the user.
     */
    public static OtherAccount getInstance(String userID) {
        return FirebaseOtherAccount.getInstance(userID);
    }

    /**
     * Get the ID of the user.
     */
    public abstract String getUserID();

    /**
     * Get the photo url of the user.
     */
    public Uri getPhotoUrl() {
        return accountData.getPhotoUrl();
    }
}
