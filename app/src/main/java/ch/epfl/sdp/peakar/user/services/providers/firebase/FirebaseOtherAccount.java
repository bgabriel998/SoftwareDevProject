package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.Hashtable;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.user.services.AccountData;
import ch.epfl.sdp.peakar.user.services.OtherAccount;

/**
 * This class extends Account implementing RemoteResource in order to handle DB interaction.
 */
public class FirebaseOtherAccount extends OtherAccount implements RemoteResource {
    protected static final Hashtable<String, FirebaseOtherAccount> loadedAccounts = new Hashtable<>();
    private final String userID;
    private final DatabaseReference dbRefUser;

    /**
     * Create a new account instance.
     * @param newID ID of the authenticated user
     */
    private FirebaseOtherAccount(String newID) {
        dbRefUser = Database.refRoot.child(Database.CHILD_USERS +  newID);
        userID = newID;
    }

    /**
     * Get a Firebase account instance.
     * WARNING: you SHOULD NOT call this method. To get an account you should call <code>OtherAccount.getInstance(String authID)</code>.
     * @param userID id of the user.
     */
    public static FirebaseOtherAccount getInstance(String userID) {
        // If the user account was already loaded, return the same instance
        FirebaseOtherAccount instance = loadedAccounts.get(userID);

        if(instance == null) {
            // Otherwise, create a new instance. On completion, return it
            Log.d("ACCOUNT", "getInstance: creating a new instance");
            instance = new FirebaseOtherAccount(userID);
            loadedAccounts.put(userID, instance);
            instance.retrieveData();
        }

        return instance;
    }

    @Override
    public String getUserID() {
        return userID;
    }

    @Override
    public RemoteOutcome retrieveData() {
        AccountData newAccountData = new AccountData();
        RemoteOutcome remoteOutcome = FirebaseAccountDataFactory.retrieveAccountData(newAccountData, dbRefUser);
        accountData = newAccountData;
        return remoteOutcome;
    }
}
