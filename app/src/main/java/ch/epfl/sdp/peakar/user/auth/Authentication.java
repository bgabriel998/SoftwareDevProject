package ch.epfl.sdp.peakar.user.auth;

import android.content.Context;

/**
 * This class represents an auth service.
 */
public interface Authentication {

    /**
     * Get the reference to the Authentication object.
     */
    static Authentication getInstance() {
        // As FirebaseAuthentication is the only authentication we have implemented
        return FirebaseAuthentication.getInstance();
    }

    /**
     * Perform the authentication with a certain provider. This method blocks the caller until the outcome is obtained.
     * @param authProvider provider for the authentication.
     * @param token token from the provider.
     */
    AuthOutcome authWithProvider(AuthProvider authProvider, String token);

    /**
     * Perform the authentication for an anonymous user. This method blocks the caller until the outcome is obtained.
     */
    AuthOutcome authAnonymously();

    /**
     * Get the authenticated account or a null reference if no auth has been performed.
     * @return
     */
    Account getAuthAccount();

    /**
     * Sign the current authenticated user out, or do nothing if no auth has been performed. This method blocks the caller until the sign out is over.
     * @param context
     */
    void signOut(Context context);

    /**
     * Retrieve the ID of the authenticated account, or "null" if no auth has been performed.
     */
    String getID();
}
