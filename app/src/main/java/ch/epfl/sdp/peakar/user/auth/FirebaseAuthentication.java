package ch.epfl.sdp.peakar.user.auth;

import android.content.Context;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

/**
 * This class describes the Auth service provided by Firebase
 */
public class FirebaseAuthentication implements Authentication {
    private static FirebaseAuthentication instance;

    // The account reference will be null if no account is authenticated, or != null if an account is authenticated
    private static Account authAccount = FirebaseAuth.getInstance().getCurrentUser() != null ? Account.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid()) : null;;

    private FirebaseAuthentication() {}

    public static FirebaseAuthentication getInstance() {
        if(instance == null) {
            instance = new FirebaseAuthentication();
        }
        return instance;
    }

    @Override
    public AuthOutcome authWithProvider(AuthProvider authProvider, String token) {

        AuthOutcome outcome = AuthOutcome.FAIL;

        // Get the credential from the provider
        AuthCredential credential = getCredentialFromProvider(authProvider, token);
        assert credential != null;

        // Perform the auth with such credential
        Task<AuthResult> authTask = FirebaseAuth.getInstance().signInWithCredential(credential);

        try {
            // Wait for the auth to finish
            Tasks.await(authTask);

            // Update the account reference
            authAccount = Account.getInstance(getID());

            // Retrieve account data
            outcome = authAccount.retrieveData();

            if(outcome == AuthOutcome.FAIL) outcome = AuthOutcome.NOT_REGISTERED;

        } catch (Exception e) {
            Log.d("AUTH", "authWithProvider: " + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause());

            // Notify failure
            return AuthOutcome.FAIL;
        }

        return outcome;
    }

    /**
     * Perform an anonymous auth.
     */
    public AuthOutcome authAnonymously() {
        Task<AuthResult> authTask = FirebaseAuth.getInstance().signInAnonymously();

        try {
            // Wait for the auth to finish
            Tasks.await(authTask);

            // Update the account reference
            authAccount = Account.getInstance(getID());

            // Retrieve account data
            AuthOutcome outcome = authAccount.retrieveData();
            return outcome == AuthOutcome.FAIL ? AuthOutcome.NOT_REGISTERED : outcome;

        } catch (Exception e) {
            // Notify failure
            return AuthOutcome.FAIL;
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

            case FACEBOOK:
                return FacebookAuthProvider.getCredential(token);

            default:
                return null;
        }
    }

    @Override
    public Account getAuthAccount() {
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

    /**
     * Force a data retrieve.
     * WARNING: this method SHOULD NOT be used for normal purposes as all the other methods already make sure that data is retrieved correctly.
     */
    public void forceRetrieveData() {
        // Retrieve account data
        if(authAccount != null) authAccount.retrieveData();
    }
}
