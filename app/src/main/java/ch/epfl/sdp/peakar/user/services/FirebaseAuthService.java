package ch.epfl.sdp.peakar.user.services;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;

/**
 * This class describes the Auth service provided by Firebase.
 * It makes use of a FirebaseAccount as this authentication service is always used only if Firebase Realtime Database is available.
 */
public class FirebaseAuthService implements AuthService {
    private static FirebaseAuthService instance;

    // The account reference will be null if no account is authenticated, or != null if an account is authenticated
    private static RemoteAuthAccount authAccount;

    private FirebaseAuthService() {}

    public static FirebaseAuthService getInstance() {
        if(instance == null) {
            instance = new FirebaseAuthService();
            // On class initialization, retrieve any previously logged account and, if necessary, the account data
            authAccount = FirebaseAuth.getInstance().getCurrentUser() != null ? RemoteAuthAccount.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid()) : null;
            if(authAccount != null) new Thread (() -> authAccount.retrieveData()).start();
        }
        return instance;
    }

    @Override
    public RemoteOutcome authWithProvider(AuthProvider authProvider, String token) {

        RemoteOutcome outcome;

        // Get the credential from the provider
        AuthCredential credential = getCredentialFromProvider(authProvider, token);
        assert credential != null;

        // Perform the auth with such credential
        Task<AuthResult> authTask = FirebaseAuth.getInstance().signInWithCredential(credential);

        try {
            // Wait for the auth to finish
            Tasks.await(authTask);

            // Update the account reference
            authAccount = RemoteAuthAccount.getInstance(getID());

            // Retrieve account data
            outcome = authAccount.retrieveData();

            if(outcome == RemoteOutcome.FAIL) outcome = RemoteOutcome.NOT_FOUND;

        } catch (Exception e) {
            Log.d("AUTH", "authWithProvider: " + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause());

            // Notify failure
            return RemoteOutcome.FAIL;
        }

        return outcome;
    }

    /**
     * Perform an anonymous auth.
     */
    public RemoteOutcome authAnonymously() {
        Task<AuthResult> authTask = FirebaseAuth.getInstance().signInAnonymously();

        try {
            // Wait for the auth to finish
            Tasks.await(authTask);

            // Update the account reference
            authAccount = RemoteAuthAccount.getInstance(getID());

            // Retrieve account data
            RemoteResource remoteAccount = authAccount;
            RemoteOutcome outcome = remoteAccount.retrieveData();
            return outcome == RemoteOutcome.FAIL ? RemoteOutcome.NOT_FOUND : outcome;

        } catch (Exception e) {
            // Notify failure
            return RemoteOutcome.FAIL;
        }
    }

    /**
     * According to the provider, retrieve the credential from the given token and return it.
     * @param authProvider provider of the account. This MUST be registered in the AuthProvider enum.
     * @param token token for the connection from the provider.
     */
    private AuthCredential getCredentialFromProvider(AuthProvider authProvider, String token) {
        switch(authProvider) {
            case GOOGLE:
                return GoogleAuthProvider.getCredential(token, null);
            /* Code commented because we still do not have the facebook UI for login
            case FACEBOOK:
                return FacebookAuthProvider.getCredential(token);
            */

            default:
                return null;
        }
    }

    @Override
    public AuthAccount getAuthAccount() {
        return authAccount;
    }

    @Override
    public void signOut(Context context) {
        authAccount = null;
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Task<Void> signOutTask = AuthUI.getInstance().signOut(context);
            try {
                // Wait for the sign out to finish
                Tasks.await(signOutTask);
            } catch (Exception e) {
                Log.d("AUTH", "signOut: failed");
            }
        }
    }

    @Override
    public String getID() {
        try {
            return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        } catch (NullPointerException e) {
            return "null";
        }

    }

    @Override
    public Uri getPhotoUrl() {
        try {
            Uri photoUri = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl();
            return photoUri == null ? Uri.EMPTY : photoUri;
        } catch (NullPointerException e) {
            return Uri.EMPTY;
        }

    }

    /**
     * Force a data retrieval.
     * WARNING: this method SHOULD NOT be used for normal purposes as all the other methods already make sure that data is retrieved correctly.
     */
    public void forceRetrieveData() {
        // Retrieve account data
        Log.d("RegisterUserTest", "forceRetrieveData: ");
        RemoteResource remoteAccount = authAccount;
        if(authAccount != null) remoteAccount.retrieveData();
    }
}
