package ch.epfl.sdp.peakar.user.services.providers.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Collections;

import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.general.remote.RemoteOutcome;
import ch.epfl.sdp.peakar.general.remote.RemoteResource;
import ch.epfl.sdp.peakar.points.CountryHighPoint;
import ch.epfl.sdp.peakar.points.POIPoint;
import ch.epfl.sdp.peakar.user.outcome.ProfileOutcome;
import ch.epfl.sdp.peakar.user.services.AccountData;
import ch.epfl.sdp.peakar.user.services.AuthAccount;
import ch.epfl.sdp.peakar.user.services.AuthService;

/**
 * This class extends AuthAccount to handle operations on a Firebase Realtime Database
 */
public class FirebaseAuthAccount extends AuthAccount implements RemoteResource {
    private static String currentID = null;
    private static FirebaseAuthAccount instance = null;
    private static DatabaseReference dbRefUser = null;

    /**
     * Create a new account instance.
     * @param newID ID of the authenticated user
     */
    private FirebaseAuthAccount(String newID) {
        dbRefUser = Database.refRoot.child(Database.CHILD_USERS + newID);
        currentID = newID;
    }

    /**
     * Get a Firebase account instance.
     * WARNING: you SHOULD NOT call this method. To get an account you should call <code>Account.getInstance(String authID)</code>.
     * @param authID id of the user.
     */
    protected static FirebaseAuthAccount getInstance(String authID) {
        // If the auth ID has not changed, return the same instance
        if(authID.equals(currentID)) return instance;

        Log.d("ACCOUNT", "getInstance: creating a new instance");
        // Otherwise, create a new instance. On completion, return it
        instance = new FirebaseAuthAccount(authID);
        return instance;
    }

    @Override
    public RemoteOutcome retrieveData() {
        AccountData newAccountData = new AccountData();
        RemoteOutcome remoteOutcome = FirebaseAccountDataFactory.retrieveAccountData(newAccountData, dbRefUser);
        accountData = newAccountData;
        return remoteOutcome;
    }

    /* GETTERS */

    @Override
    public void init() {
        if(getUsername().equals(USERNAME_BEFORE_REGISTRATION)) {
            Task<DataSnapshot> checkTask = dbRefUser.get();
            try {
                Tasks.await(checkTask);

                DataSnapshot data = checkTask.getResult();
                assert data != null;

                // If the account is registered but is not loaded, load it
                if (data.exists()) retrieveData();
            } catch (Exception ignored) {
            }
        }
    }

    /* SETTERS */

    @Override
    public ProfileOutcome changeUsername(String newUsername) {
        String username = getUsername();
        Log.d("Account", "changeUsername: current username = " + username);

        // If the username is not valid
        if(!AuthAccount.checkUsernameValidity(newUsername)) return ProfileOutcome.INVALID;

        // If the username is the current one
        if(newUsername.equals(username)) return ProfileOutcome.USERNAME_CURRENT;

        // Finally, check if it is available
        Task<DataSnapshot> checkTask = Database.refRoot.child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(newUsername).get();

        try {
            // Wait for task to finish
            Tasks.await(checkTask);

            // Get the obtained data
            DataSnapshot data = checkTask.getResult();
            assert data != null;

            // Check if it was already used
            if(data.exists()) return ProfileOutcome.USERNAME_USED;

            // If not, register the new username
            Task<Void> changeTask = dbRefUser.child(Database.CHILD_USERNAME).setValue(newUsername);

            // If the user was not registered yet, register him by adding the missing fields on the DB.
            if(getUsername().equals(USERNAME_BEFORE_REGISTRATION)) {
                // Add the photo
                Task<Void> addPhotoUrl = dbRefUser.child(Database.CHILD_PHOTO_URL).setValue(AuthService.getInstance().getPhotoUrl().toString());
                try {
                    // Wait for task to finish
                    Tasks.await(addPhotoUrl);
                    Log.d("Account", "changeUsername - addPhotoUrl: added");
                } catch (Exception e) {
                    Log.d("Account", "changeUsername - addPhotoUrl: failed");
                    return ProfileOutcome.FAIL;
                }
            }

            try {
                // Wait for task to finish
                Tasks.await(changeTask);

                // Change username locally
                return super.changeUsername(newUsername);

            } catch (Exception e) {
                Log.d("Account", "changeUsername - changeTask: failed");
                return ProfileOutcome.FAIL;
            }

        } catch (Exception e) {
            Log.d("Account", "changeUsername - checkTask: failed");
            return ProfileOutcome.FAIL;
        }

    }

    @Override
    public void setScore(long newScore) {
        // Set local value
        super.setScore(newScore);

        // Set remote value
        Database.setChild(Database.CHILD_USERS + currentID,
                Collections.singletonList(Database.CHILD_SCORE),
                Collections.singletonList(newScore));
    }

    @Override
    public ProfileOutcome addFriend(String friendUsername) {
        String username = getUsername();

        Log.d("ACCOUNT", "addFriend: current username: " + getUsername());
        Log.d("ACCOUNT", "addFriend: friend username: " + friendUsername);

        // If the friend username is not valid
        if(!AuthAccount.checkUsernameValidity(friendUsername)) return ProfileOutcome.INVALID;

        // If the friend username is the current one
        if(friendUsername.equals(username)) return ProfileOutcome.FRIEND_CURRENT;

        // Finally, check if the user exists
        Task<DataSnapshot> checkExistenceTask = Database.refRoot.child(Database.CHILD_USERS).orderByChild(Database.CHILD_USERNAME).equalTo(friendUsername).get();

        try {
            Log.d("ACCOUNT", "addFriend - checkExistenceTask: ongoing");

            // Wait for the search to end
            Tasks.await(checkExistenceTask);

            // Get the obtained data
            DataSnapshot data = checkExistenceTask.getResult();
            assert data != null;

            // Check if the friend exists
            if(!data.exists()) return ProfileOutcome.FRIEND_NOT_PRESENT;

            Log.d("ACCOUNT", "addFriend: friend is on DB");

            // Get the friend ID
            String tempID = null;
            for(DataSnapshot searchedUser : data.getChildren()) {
                tempID = searchedUser.getKey();
            }
            assert tempID != null;
            Log.d("ACCOUNT", "addFriend: FRIEND ID: " + tempID);

            final String friendID = tempID;

            // Check if the friend is already a user's friend
            if(getFriends().stream().anyMatch(x -> x.hasID(friendID))) return ProfileOutcome.FRIEND_ALREADY_ADDED;

            Log.d("ACCOUNT", "addFriend: adding friend");
            Log.d("ACCOUNT", "addFriend: adding friend - current username: " + username);

            // Otherwise, add the friend remotely
            Task<Void> addTask = dbRefUser.child(Database.CHILD_FRIENDS).child(friendID).setValue("");

            // Wait for task to finish
            Tasks.await(addTask);

            // Create a friend item
            FirebaseFriendItem newFriendItem = new FirebaseFriendItem(friendID);

            // Add the friend locally
            addFriend(newFriendItem);

            return ProfileOutcome.FRIEND_ADDED;
        } catch (Exception e) {
            Log.d("ACCOUNT", "addFriend - checkExistenceTask: failed");
            return ProfileOutcome.FAIL;
        }
    }

    @Override
    public void removeFriend(String friendID) {
        // Remove listener
        getFriends().stream().filter(x -> x.hasID(friendID)).map(x -> (FirebaseFriendItem)x).forEach(FirebaseFriendItem::removeListener);

        // Remove remotely, in an asynchronous way as there is no need to retrieve the information for now
        new Thread(() -> dbRefUser.child(Database.CHILD_FRIENDS).child(friendID).removeValue()).start();

        // Remove locally
        super.removeFriend(friendID);
    }

    @Override
    public void setDiscoveredCountryHighPoint(CountryHighPoint entry){
        if(!getDiscoveredCountryHighPoint().containsKey(entry.getCountryName())) {
            // Add remotely
            Database.setChildObject(Database.CHILD_USERS + currentID + "/" +
                    Database.CHILD_COUNTRY_HIGH_POINT, entry);

            // Add locally
            super.setDiscoveredCountryHighPoint(entry);
        }
    }

    @Override
    public void setDiscoveredPeakHeights(int badge) {
        if(!getDiscoveredPeakHeights().contains(badge)){
            // Add remotely
            Database.setChildObject(Database.CHILD_USERS + currentID + "/" +
                    Database.CHILD_DISCOVERED_PEAKS_HEIGHTS, Collections.singletonList(badge));

            // Add locally
            super.setDiscoveredPeakHeights(badge);
        }
    }

    @Override
    public void setDiscoveredPeaks(ArrayList<POIPoint> newDiscoveredPeaks) {
        // Add remotely
        Database.setChildObjectList(Database.CHILD_USERS + currentID + "/" +
                        Database.CHILD_DISCOVERED_PEAKS,
                new ArrayList<>(newDiscoveredPeaks));
        super.setDiscoveredPeaks(newDiscoveredPeaks);
    }
}
