package ch.epfl.sdp.peakar.user.services;

import android.content.Context;

import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;

/**
 * This class represents an auth service.
 */
public interface AuthService {

    /**
     * Get the reference to the Authentication object.
     */
    static AuthService getInstance() {
        // As FirebaseAuthentication is the only authentication we have implemented
        return FirebaseAuthService.getInstance();
    }

    /**
     * Perform the authentication with a certain provider. This method blocks the caller until the outcome is obtained.
     * @param authProvider provider for the authentication.
     * @param token token from the provider.
     */
    RemoteOutcome authWithProvider(AuthProvider authProvider, String token);

    /**
     * Perform the authentication for an anonymous user. This method blocks the caller until the outcome is obtained.
     */
    RemoteOutcome authAnonymously();

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
