package ch.epfl.sdp.peakar.user.services;

import android.net.Uri;

public abstract class OtherAccount extends Account {

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
