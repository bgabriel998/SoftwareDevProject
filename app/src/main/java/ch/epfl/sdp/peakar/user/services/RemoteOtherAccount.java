package ch.epfl.sdp.peakar.user.services;

import android.util.Log;

import java.util.Hashtable;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.database.DatabaseReference;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;

/**
 * This class extends Account implementing RemoteResource in order to handle DB interaction.
 */
public class RemoteOtherAccount extends OtherAccount implements RemoteResource {
    protected static final Hashtable<String, RemoteOtherAccount> loadedAccounts = new Hashtable<>();
    private final String userID;
    private final DatabaseReference dbRefUser;

    /**
     * Create a new account instance.
     * @param newID ID of the authenticated user
     */
    private RemoteOtherAccount(String newID) {
        dbRefUser = Database.getInstance().getReference().child(Database.CHILD_USERS +  newID);
        userID = newID;
    }

    /**
     * Get a Firebase account instance.
     * WARNING: you SHOULD NOT call this method. To get an account you should call <code>OtherAccount.getInstance(String authID)</code>.
     * @param userID id of the user.
     */
    public static RemoteOtherAccount getInstance(String userID) {
        // If the user account was already loaded, return the same instance
        RemoteOtherAccount instance = loadedAccounts.get(userID);

        if(instance == null) {
            // Otherwise, create a new instance. On completion, return it
            Log.d("ACCOUNT", "getInstance: creating a new instance");
            instance = new RemoteOtherAccount(userID);
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
        RemoteOutcome remoteOutcome = RemoteAccountDataFactory.retrieveAccountData(newAccountData, dbRefUser);
        accountData = newAccountData;
        return remoteOutcome;
    }
}
